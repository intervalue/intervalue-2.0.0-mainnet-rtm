package one.inve.contract.inve;

import one.inve.contract.ethplugin.config.BlockchainConfig;
import one.inve.contract.ethplugin.config.BlockchainNetConfig;
import one.inve.contract.ethplugin.config.Constants;
import one.inve.contract.ethplugin.core.Block;
import one.inve.contract.ethplugin.core.BlockHeader;
import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.ethplugin.core.Transaction;
import one.inve.contract.ethplugin.db.BlockStore;
import one.inve.contract.ethplugin.validator.BlockHeaderValidator;
import one.inve.contract.ethplugin.vm.DataWord;
import one.inve.contract.ethplugin.vm.GasCost;
import one.inve.contract.ethplugin.vm.OpCode;
import one.inve.contract.ethplugin.vm.program.Program.OutOfGasException;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.util.List;

public class INVEConfig implements BlockchainConfig, BlockchainNetConfig {
    private static final GasCost GAS_COST = new GasCost();
    private static final Constants CONSTANTS = new Constants();

    public GasCost getGasCost() {
        return GAS_COST;
    }

    public Constants getConstants() {
        return CONSTANTS;
    }

    public long getTransactionCost(Transaction tx) {
        long nonZeroes = tx.nonZeroDataBytes();
        long zeroVals = ArrayUtils.getLength(tx.getData()) - nonZeroes;

        return (tx.isContractCreation() ? getGasCost().getTRANSACTION_CREATE_CONTRACT() : getGasCost().getTRANSACTION())
        + zeroVals * getGasCost().getTX_ZERO_DATA() + nonZeroes * getGasCost().getTX_NO_ZERO_DATA();
    }

    @Override
    public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
        return null;
    }

    @Override
    public BigInteger getCalcDifficultyMultiplier(BlockHeader curBlock, BlockHeader parent) {
        return null;
    }

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        return false;
    }

    @Override
    public String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx,
            Repository repositoryTrack) {
        return null;
    }

    @Override
    public void hardForkTransfers(Block block, Repository repo) {

    }

    @Override
    public List<Pair<Long, BlockHeaderValidator>> headerValidators() {
        return null;
    }

    @Override
    public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws OutOfGasException {
        return null;
    }

    @Override
    public DataWord getCreateGas(DataWord availableGas) {
        return availableGas;
    }

    @Override
    public boolean eip161() {
        return false;
    }

    @Override
    public Integer getChainId() {
        return null;
    }

    @Override
    public boolean eip198() {
        return false;
    }

    @Override
    public boolean eip206() {
        return false;
    }

    @Override
    public boolean eip211() {
        return false;
    }

    @Override
    public boolean eip212() {
        return false;
    }

    @Override
    public boolean eip213() {
        return false;
    }

    @Override
    public boolean eip214() {
        return false;
    }

    @Override
    public boolean eip658() {
        return false;
    }

    @Override
    public boolean eip145() {
        return false;
    }

    @Override
    public boolean eip1052() {
        return false;
    }

    @Override
    public boolean eip1283() {
        return false;
    }

    @Override
    public boolean eip1014() {
        return false;
    }

    @Override
    public BlockchainConfig getConfigForBlock(long blockNumber) {
        return this;
    }

    @Override
    public Constants getCommonConstants() {
        return null;
    }
}