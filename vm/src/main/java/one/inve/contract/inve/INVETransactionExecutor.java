package one.inve.contract.inve;

import one.inve.contract.ethplugin.config.CommonConfig;
import one.inve.contract.ethplugin.config.SystemProperties;
import one.inve.contract.ethplugin.core.*;
import one.inve.contract.ethplugin.db.BlockStore;
import one.inve.contract.ethplugin.util.ByteArraySet;
import one.inve.contract.ethplugin.vm.DataWord;
import one.inve.contract.ethplugin.vm.LogInfo;
import one.inve.contract.ethplugin.vm.PrecompiledContracts;
import one.inve.contract.ethplugin.vm.program.InternalTransaction;
import one.inve.contract.ethplugin.vm.program.ProgramResult;
import one.inve.contract.inve.vm.VM;
import one.inve.contract.inve.vm.hook.VMHook;
import one.inve.contract.inve.vm.program.INVEProgram;
import one.inve.contract.inve.vm.program.invoke.INVEProgramInvoke;
import one.inve.contract.inve.vm.program.invoke.INVEProgramInvokeFactory;
import one.inve.contract.conf.Config;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.isNull;
import static one.inve.contract.ethplugin.util.BIUtil.*;
import static one.inve.contract.ethplugin.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static one.inve.contract.ethplugin.util.ByteUtil.toHexString;
import static org.apache.commons.lang3.ArrayUtils.getLength;
import static org.apache.commons.lang3.ArrayUtils.isEmpty;

/**
 * @author Roman Mandeleil
 * @since 19.12.2014
 */
public class INVETransactionExecutor {

    private static final Logger logger = LoggerFactory.getLogger("contract");

    SystemProperties config;
    CommonConfig commonConfig;
    INVEConfig inveConfig;

    private Transaction tx;
    private Repository track;
    private Repository cacheTrack;
    private BlockStore blockStore;
    private boolean readyToExecute = false;
    private String execError;

    private INVEProgramInvokeFactory programInvokeFactory;

    private INVETransactionReceipt receipt;
    private ProgramResult result = new ProgramResult();
    private Block currentBlock;

    private VM vm;
    private INVEProgram program;

    PrecompiledContracts.PrecompiledContract precompiledContract;

    BigInteger m_endGas = BigInteger.ZERO;
    long basicTxCost = 0;
    List<LogInfo> logs = null;

    private ByteArraySet touchedAccounts = new ByteArraySet();

    boolean localCall = false;
    private final VMHook vmHook;

    // public INVETransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
    //                            INVEProgramInvokeFactory programInvokeFactory, Block currentBlock) {

    //     this(tx, coinbase, track, blockStore, programInvokeFactory, currentBlock, new EthereumListenerAdapter(), VMHook.EMPTY);
    // }

    // public INVETransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
    //                            INVEProgramInvokeFactory programInvokeFactory, Block currentBlock,
    //                            EthereumListener listener) {
    //     this(tx, coinbase,track, blockStore, programInvokeFactory, currentBlock, listener, VMHook.EMPTY);
    // }

    // public INVETransactionExecutor(Transaction tx, byte[] coinbase, Repository track, BlockStore blockStore,
    //                            INVEProgramInvokeFactory programInvokeFactory, Block currentBlock,
    //                            EthereumListener listener, VMHook vmHook) {

    //     this.id = BigInteger.ZERO;
    //     this.tx = tx;
    //     this.coinbase = coinbase;
    //     this.track = track;
    //     this.cacheTrack = track.startTracking();
    //     this.blockStore = blockStore;
    //     this.programInvokeFactory = programInvokeFactory;
    //     this.currentBlock = currentBlock;
    //     this.m_endGas = toBI(tx.getGasLimit());
    //     this.vmHook = isNull(vmHook) ? VMHook.EMPTY : vmHook;

    //     withCommonConfig(CommonConfig.getDefault());
    // }

    public INVETransactionExecutor(Transaction tx, Repository track,
            BlockStore blockStore, INVEProgramInvokeFactory programInvokeFactory, Block currentBlock) {

        this(tx, track, blockStore, programInvokeFactory, currentBlock, VMHook.EMPTY);
    }

    public INVETransactionExecutor(Transaction tx, Repository track,
            BlockStore blockStore, INVEProgramInvokeFactory programInvokeFactory, Block currentBlock,
            VMHook vmHook) {

        this.tx = tx;
        this.track = track;
        this.cacheTrack = track.startTracking();
        this.blockStore = blockStore;
        this.programInvokeFactory = programInvokeFactory;
        this.currentBlock = currentBlock;
        this.m_endGas = toBI(tx.getGasLimit());
        this.vmHook = isNull(vmHook) ? VMHook.EMPTY : vmHook;
        this.inveConfig = new INVEConfig();

        withCommonConfig(CommonConfig.getDefault());
    }

    public INVETransactionExecutor withCommonConfig(CommonConfig commonConfig) {
        this.commonConfig = commonConfig;
        this.config = commonConfig.systemProperties();
        return this;
    }

    private void execError(String err) {
        logger.warn(err);
        execError = err;
    }

    /**
     * Do all the basic validation, if the executor
     * will be ready to run the transaction at the end
     * set readyToExecute = true
     */
    public void init() {
        
        if (localCall) {
            readyToExecute = true;
            return;
        }
        
        basicTxCost = tx.transactionCost(inveConfig, currentBlock);
        BigInteger txGasLimit = new BigInteger(1, tx.getGasLimit());
        
        if (txGasLimit.compareTo(BigInteger.valueOf(basicTxCost)) < 0) {
            execError(String.format("Not enough gas for transaction execution: Require: %s Got: %s", basicTxCost, txGasLimit));
            return;
        }

        BigInteger reqNonce = track.getNonce(tx.getSender());
        BigInteger txNonce = toBI(tx.getNonce());
        if (isNotEqual(reqNonce, txNonce)) {
            execError(String.format("Invalid nonce: required: %s , tx.nonce: %s", reqNonce, txNonce));
            return;
        }

        BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);
        BigInteger totalCost = toBI(tx.getValue()).add(txGasCost);
        BigInteger senderBalance = track.getBalance(tx.getSender());

        if (!isCovers(senderBalance, totalCost)) {
            execError(String.format("Not enough cash: Require: %s, Sender cash: %s", totalCost, senderBalance));
            return;
        }

       /* if (!inveConfig.acceptTransactionSignature(tx)) {
            execError("Transaction signature not accepted: " + tx.getSignature());
            return;
        }*/

        readyToExecute = true;
    }

    public void execute() {
        if (!readyToExecute) return;

        if (!localCall) {
            track.increaseNonce(tx.getSender());

            BigInteger txGasLimit = toBI(tx.getGasLimit());
            BigInteger txGasCost = toBI(tx.getGasPrice()).multiply(txGasLimit);
            track.addBalance(tx.getSender(), txGasCost.negate());

            logger.info("Paying: txGasCost: [{}], gasPrice: [{}], gasLimit: [{}]", txGasCost, toBI(tx.getGasPrice()), txGasLimit);
        }

        if (tx.isContractCreation()) {
            create();
        } else {
            call();
        }
    }

    private void call() {
        if (!readyToExecute) return;

        byte[] targetAddress = tx.getReceiveAddress();
        precompiledContract = PrecompiledContracts.getContractForAddress(DataWord.of(targetAddress), inveConfig);

        if (precompiledContract != null) {
            long requiredGas = precompiledContract.getGasForData(tx.getData());

            BigInteger spendingGas = BigInteger.valueOf(requiredGas).add(BigInteger.valueOf(basicTxCost));

            if (!localCall && m_endGas.compareTo(spendingGas) < 0) {
                // no refund
                // no endowment
                execError("Out of Gas calling precompiled contract 0x" + toHexString(targetAddress) +
                        ", required: " + spendingGas + ", left: " + m_endGas);
                m_endGas = BigInteger.ZERO;
                return;
            } else {

                m_endGas = m_endGas.subtract(spendingGas);

                // FIXME: save return for vm trace
                Pair<Boolean, byte[]> out = precompiledContract.execute(tx.getData());

                if (!out.getLeft()) {
                    execError("Error executing precompiled contract 0x" + toHexString(targetAddress));
                    m_endGas = BigInteger.ZERO;
                    return;
                }
            }
        } else {
            byte[] code = track.getCode(targetAddress);
            if (isEmpty(code)) {
                m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
                result.spendGas(basicTxCost);
            } else {
                INVEProgramInvoke programInvoke =
                        programInvokeFactory.createProgramInvoke(tx, currentBlock, cacheTrack, blockStore);

                this.vm = new VM(vmHook);
                this.program = new INVEProgram(track.getCodeHash(targetAddress), code, programInvoke, tx, config, vmHook).withCommonConfig(commonConfig);
            }
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), targetAddress, endowment);

        touchedAccounts.add(targetAddress);
    }

    private void create() {
        byte[] newContractAddress = tx.getContractAddress();

        AccountState existingAddr = cacheTrack.getAccountState(newContractAddress);
        if (existingAddr != null && existingAddr.isContractExist(inveConfig)) {
            execError("Trying to create a contract with existing contract address: " + new String(newContractAddress));
            m_endGas = BigInteger.ZERO;
            return;
        }

        //In case of hashing collisions (for TCK tests only), check for any balance before createAccount()
        BigInteger oldBalance = track.getBalance(newContractAddress);
        cacheTrack.createAccount(tx.getContractAddress());
        cacheTrack.addBalance(newContractAddress, oldBalance);
        cacheTrack.increaseNonce(newContractAddress);

        if (isEmpty(tx.getData())) {
            m_endGas = m_endGas.subtract(BigInteger.valueOf(basicTxCost));
            result.spendGas(basicTxCost);
        } else {
            INVEProgramInvoke programInvoke = programInvokeFactory.createProgramInvoke(tx, currentBlock,
                    cacheTrack, blockStore);

            this.vm = new VM(vmHook);
            this.program = new INVEProgram(tx.getData(), programInvoke, tx, config, vmHook).withCommonConfig(commonConfig);
        }

        BigInteger endowment = toBI(tx.getValue());
        transfer(cacheTrack, tx.getSender(), newContractAddress, endowment);

        touchedAccounts.add(newContractAddress);
    }

    public void go() {
        if (!readyToExecute) return;

        try {
            if (vm != null) {
                // Charge basic cost of the transaction
                program.spendGas(tx.transactionCost(inveConfig, currentBlock), "TRANSACTION COST");
                // play the vm
                vm.play(program);
                result = program.getResult();
                m_endGas = toBI(tx.getGasLimit()).subtract(toBI(program.getResult().getGasUsed()));

                if (tx.isContractCreation() && !result.isRevert()) {
                    int returnDataGasValue = getLength(program.getResult().getHReturn()) *
                            inveConfig.getGasCost().getCREATE_DATA();
                    if (m_endGas.compareTo(BigInteger.valueOf(returnDataGasValue)) < 0) {
                        // Not enough gas to return contract code
                        if (!inveConfig.getConstants().createEmptyContractOnOOG()) {
                            program.setRuntimeFailure(INVEProgram.Exception.notEnoughSpendingGas("No gas to return just created contract",
                                    returnDataGasValue, program));
                            result = program.getResult();
                        }
                        result.setHReturn(EMPTY_BYTE_ARRAY);
                    } else if (getLength(result.getHReturn()) > inveConfig.getConstants().getMAX_CONTRACT_SZIE()) {
                        // Contract size too large
                        program.setRuntimeFailure(INVEProgram.Exception.notEnoughSpendingGas("Contract size too large: " + getLength(result.getHReturn()),
                                returnDataGasValue, program));
                        result = program.getResult();
                        result.setHReturn(EMPTY_BYTE_ARRAY);
                    } else {
                        // Contract successfully created
                        m_endGas = m_endGas.subtract(BigInteger.valueOf(returnDataGasValue));
                        cacheTrack.saveCode(tx.getContractAddress(), result.getHReturn());
                        logger.info("save code to stateworld success, [contract address]: {}",
						    new String(tx.getContractAddress()));
                    }
                }

                if (result.getException() != null || result.isRevert()) {
                    result.getDeleteAccounts().clear();
                    result.getLogInfoList().clear();
                    result.resetFutureRefund();
                    rollback();

                    if (result.getException() != null) {
                        throw result.getException();
                    } else {
                        execError("REVERT opcode executed");
                    }
                } else if(!localCall) { // 若 localCall == true，则表示为 call 调用，只查询不修改状态
                    touchedAccounts.addAll(result.getTouchedAccounts());
                    cacheTrack.commit();
                } else {
                    m_endGas = BigInteger.ZERO;
                    result.getDeleteAccounts().clear();
                    result.getLogInfoList().clear();
                    result.resetFutureRefund();
                    rollback();
                }

            } else {
                cacheTrack.commit();
            }

        } catch (Throwable e) {

            // TODO: catch whatever they will throw on you !!!
//            https://github.com/ethereum/cpp-ethereum/blob/develop/libethereum/Executive.cpp#L241
            rollback();
            m_endGas = BigInteger.ZERO;
            execError(e.getMessage());
        }
    }

    private void rollback() {

        cacheTrack.rollback();

        // remove touched account
        touchedAccounts.remove(
                tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress());
    }

    public TransactionExecutionSummary finalization() {
        if (!readyToExecute) return null;

        TransactionExecutionSummary.Builder summaryBuilder = TransactionExecutionSummary.builderFor(tx)
                .gasLeftover(m_endGas)
                .logs(result.getLogInfoList())
                .result(result.getHReturn());

        if (!localCall && result != null) {
            // Accumulate refunds for suicides
            result.addFutureRefund(result.getDeleteAccounts().size() * inveConfig.
                    getConfigForBlock(0).getGasCost().getSUICIDE_REFUND());
            long gasRefund = Math.min(Math.max(0, result.getFutureRefund()), getGasUsed() / 2);
            // byte[] addr = tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress();
            m_endGas = m_endGas.add(BigInteger.valueOf(gasRefund));
            
            // 生成余额变动 list
            BigInteger fee = toBI(result.getGasUsed()).multiply(toBI(tx.getGasPrice()));
            List<InternalTransferData> internalTransferList = new ArrayList<>();
            if (result.getException() == null) { // 合约执行无异常
                InternalTransferData iTransfer = new InternalTransferData(
                    new String(tx.getSender()), 
                    tx.getReceiveAddress() == null? "" : new String(tx.getReceiveAddress()), 
                    fee, 
                    toBI(tx.getValue()));

                internalTransferList.add(iTransfer);
                for (InternalTransaction it : result.getInternalTransactions()) {
                    iTransfer = new InternalTransferData(new String(it.getSender()), new String(it.getReceiveAddress()),
                            BigInteger.ZERO, toBI(it.getValue()));
                    internalTransferList.add(iTransfer);
                }
            } else { // 合约执行有异常
                InternalTransferData iTransfer = new InternalTransferData(
                    new String(tx.getSender()), 
                    tx.getReceiveAddress() == null? "" : new String(tx.getReceiveAddress()), 
                    fee, 
                    BigInteger.ZERO);
                
                internalTransferList.add(iTransfer);
            }

            summaryBuilder
                    .gasUsed(toBI(result.getGasUsed()))
                    .gasRefund(toBI(gasRefund))
                    .deletedAccounts(result.getDeleteAccounts())
                    .internalTransactions(result.getInternalTransactions())
                    .balanceChanges(internalTransferList == null? new ArrayList<InternalTransferData>() : internalTransferList);

            if (result.getException() != null) {
                summaryBuilder.markAsFailed();
            }
        }

        TransactionExecutionSummary summary = summaryBuilder.build();
        // Refund for gas leftover
        track.addBalance(tx.getSender(), summary.getLeftover().add(summary.getRefund()));
        logger.info("Pay total refund to sender: [{}], refund val: [{}]", toHexString(tx.getSender()), summary.getRefund());

        // 将手续费转给基金会
        track.addBalance(Config.FOUNDATION_ADDRESS.getBytes(), summary.getFee());
        logger.info(
            "Pay fees to FOUNDATION: [{}], value is: [{}]", Config.FOUNDATION_ADDRESS, summary.getFee());
        
        if (result != null) {
            logs = result.getLogInfoList();
            // Traverse list of suicides
            for (DataWord address : result.getDeleteAccounts()) {
                track.delete(address.getLast20Bytes());
            }
        }

        for (byte[] acctAddr : touchedAccounts) {
            AccountState state = track.getAccountState(acctAddr);
            if (state != null && state.isEmpty()) {
                track.delete(acctAddr);
            }
        }
        return summary;
    }

    public INVETransactionExecutor setLocalCall(boolean localCall) {
        this.localCall = localCall;
        return this;
    }


    public INVETransactionReceipt getReceipt() {
        if (receipt == null) {
            receipt = new INVETransactionReceipt();
            receipt.setTransaction(tx);
            receipt.setLogInfoList(getVMLogs());
            receipt.setGasUsed(getGasUsed());
            receipt.setExecutionResult(tx.isContractCreation() ? tx.getContractAddress() : getResult().getHReturn());
            receipt.setError(execError);
            receipt.setTxStatus(receipt.isSuccessful());
        }
        return receipt;
    }

    public List<LogInfo> getVMLogs() {
        return logs;
    }

    public ProgramResult getResult() {
        return result;
    }

    public long getGasUsed() {
        return toBI(tx.getGasLimit()).subtract(m_endGas).longValue();
    }

}
