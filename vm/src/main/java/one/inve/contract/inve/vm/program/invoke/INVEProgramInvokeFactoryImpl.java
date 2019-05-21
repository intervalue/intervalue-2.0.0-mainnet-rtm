package one.inve.contract.inve.vm.program.invoke;

import one.inve.contract.ethplugin.core.Block;
import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.ethplugin.core.Transaction;
import one.inve.contract.ethplugin.db.BlockStore;
import one.inve.contract.ethplugin.util.ByteUtil;
import one.inve.contract.ethplugin.vm.DataWord;
import one.inve.contract.inve.vm.program.INVEProgram;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * @author 肖毅
 * @since 2019-01-16
 */
public class INVEProgramInvokeFactoryImpl implements INVEProgramInvokeFactory {

    private static final Logger logger = LoggerFactory.getLogger("VM");

    @Override
    public INVEProgramInvoke createProgramInvoke(Transaction tx, Block block,
                                                 Repository repository, BlockStore blockStore) {
        return generalInvoke(tx, repository, blockStore);
    }

    @Override
    public INVEProgramInvoke createProgramInvoke(INVEProgram program, DataWord toAddress, DataWord callerAddress,
                                                 DataWord inValue, DataWord inGas,
                                                 BigInteger balanceInt, byte[] dataIn,
                                                 Repository repository, BlockStore blockStore,
                                                 boolean isStaticCall, boolean byTestingSuite) {
        DataWord address = toAddress;
        DataWord origin = program.getOriginAddress();
        DataWord caller = callerAddress;

        DataWord balance = DataWord.of(balanceInt.toByteArray());
        DataWord gasPrice = program.getGasPrice();
        DataWord gas = inGas;
        DataWord callValue = inValue;

        byte[] data = dataIn;
        DataWord timestamp = program.getTimestamp();
        DataWord gasLimit = program.getGasLimit();

        if (logger.isInfoEnabled()) {
            logger.info("Internal call: \n" +
                            "address={}\n" +
                            "origin={}\n" +
                            "caller={}\n" +
                            "balance={}\n" +
                            "gasPrice={}\n" +
                            "gas={}\n" +
                            "callValue={}\n" +
                            "data={}\n" +
                            "timestamp={}\n" +
                            "gaslimit={}\n",
                    ByteUtil.toHexString(address.getLast20Bytes()),
                    ByteUtil.toHexString(origin.getLast20Bytes()),
                    ByteUtil.toHexString(caller.getLast20Bytes()),
                    balance.toString(),
                    gasPrice.longValue(),
                    gas.longValue(),
                    ByteUtil.toHexString(callValue.getNoLeadZeroesData()),
                    ByteUtil.toHexString(data),
                    timestamp.longValue(),
                    gasLimit.bigIntValue());
        }

        return new INVEProgramInvokeImpl(address, origin, caller, balance, gasPrice, gas, callValue,
                data, timestamp, gasLimit,
                repository, program.getCallDeep() + 1, blockStore, isStaticCall, byTestingSuite);
    }


    private INVEProgramInvoke generalInvoke(Transaction tx, Repository repository, BlockStore blockStore) {

        /***         ADDRESS op       ***/
        // YP: Get address of currently executing account.
        byte[] address = tx.isContractCreation() ? tx.getContractAddress() : tx.getReceiveAddress();

        /***         ORIGIN op       ***/
        // YP: This is the sender of original transaction; it is never a contract.
        byte[] origin = tx.getSender();

        /***         CALLER op       ***/
        // YP: This is the address of the account that is directly responsible for this execution.
        byte[] caller = tx.getSender();

        /***         BALANCE op       ***/
        byte[] balance = repository.getBalance(address).toByteArray();

        /***         GASPRICE op       ***/
        byte[] gasPrice = tx.getGasPrice();

        /*** GAS op ***/
        byte[] gas = tx.getGasLimit();

        /***        CALLVALUE op      ***/
        byte[] callValue = tx.getValue() == null ? new byte[]{0} : tx.getValue();

        /***     CALLDATALOAD  op   ***/
        /***     CALLDATACOPY  op   ***/
        /***     CALLDATASIZE  op   ***/
        byte[] data = tx.isContractCreation() ? ByteUtil.EMPTY_BYTE_ARRAY :( tx.getData() == null ? ByteUtil.EMPTY_BYTE_ARRAY : tx.getData() );
//        byte[] data =  tx.getData() == null ? ByteUtil.EMPTY_BYTE_ARRAY : tx.getData() ;
        
        /*** TIMESTAMP  op  ***/
        long timestamp = System.currentTimeMillis();

        /*** GASLIMIT op ***/
        byte[] gaslimit = new byte[]{0};

        return new INVEProgramInvokeImpl(address, origin, caller, balance,
                gasPrice, gas, callValue, data, 
                timestamp, gaslimit, repository, blockStore);
    }

}
