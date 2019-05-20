/*
 * Copyright (c) [2016] [ <ether.camp> ]
 * This file is part of the ethereumJ library.
 *
 * The ethereumJ library is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * The ethereumJ library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with the ethereumJ library. If not, see <http://www.gnu.org/licenses/>.
 */
package one.inve.contract.ethplugin.config.blockchain;

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
import one.inve.contract.ethplugin.vm.program.Program;
import org.apache.commons.lang3.tuple.Pair;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static one.inve.contract.ethplugin.util.BIUtil.max;

/**
 * BlockchainForkConfig is also implemented by this class - its (mostly testing) purpose to represent
 * the specific config for all blocks on the chain (kinda constant config).
 *
 * Created by Anton Nashatyrev on 25.02.2016.
 */
public abstract class AbstractConfig implements BlockchainConfig, BlockchainNetConfig {
    private static final GasCost GAS_COST = new GasCost();

    protected Constants constants;
    private List<Pair<Long, BlockHeaderValidator>> headerValidators = new ArrayList<>();

    public AbstractConfig() {
        this(new Constants());
    }

    public AbstractConfig(Constants constants) {
        this.constants = constants;
    }

    @Override
    public Constants getConstants() {
        return constants;
    }

    @Override
    public BlockchainConfig getConfigForBlock(long blockHeader) {
        return this;
    }

    @Override
    public Constants getCommonConstants() {
        return getConstants();
    }

    @Override
    public BigInteger calcDifficulty(BlockHeader curBlock, BlockHeader parent) {
        BigInteger pd = parent.getDifficultyBI();
        BigInteger quotient = pd.divide(getConstants().getDIFFICULTY_BOUND_DIVISOR());

        BigInteger sign = getCalcDifficultyMultiplier(curBlock, parent);

        BigInteger fromParent = pd.add(quotient.multiply(sign));
        BigInteger difficulty = max(getConstants().getMINIMUM_DIFFICULTY(), fromParent);

        int explosion = getExplosion(curBlock, parent);

        if (explosion >= 0) {
            difficulty = max(getConstants().getMINIMUM_DIFFICULTY(), difficulty.add(BigInteger.ONE.shiftLeft(explosion)));
        }

        return difficulty;
    }

    protected int getExplosion(BlockHeader curBlock, BlockHeader parent) {
        int periodCount = (int) (curBlock.getNumber() / getConstants().getEXP_DIFFICULTY_PERIOD());
        return periodCount - 2;
    }

    @Override
    public boolean acceptTransactionSignature(Transaction tx) {
        return Objects.equals(tx.getChainId(), getChainId());
    }

    @Override
    public String validateTransactionChanges(BlockStore blockStore, Block curBlock, Transaction tx,
                                             Repository repository) {
        return null;
    }

    @Override
    public void hardForkTransfers(Block block, Repository repo) {}

    @Override
    public List<Pair<Long, BlockHeaderValidator>> headerValidators() {
        return headerValidators;
    }


    @Override
    public GasCost getGasCost() {
        return GAS_COST;
    }

    @Override
    public DataWord getCallGas(OpCode op, DataWord requestedGas, DataWord availableGas) throws Program.OutOfGasException {
        // modify to apply eip150
        // if (requestedGas.compareTo(availableGas) > 0) {
        //     throw Program.Exception.notEnoughOpGas(op, requestedGas, availableGas);
        // }
        // return requestedGas;
        return availableGas.sub(availableGas.div(DataWord.of(64)));
    }

    @Override
    public DataWord getCreateGas(DataWord availableGas) {
        return availableGas;
    }

    @Override
    public boolean eip161() {
        return true;
    }

    @Override
    public Integer getChainId() {
        return null;
    }

    @Override
    public boolean eip198() {
        return true;
    }

    @Override
    public boolean eip206() {
        return true;    // 调用合约失败 revert 时返还剩余 gas
    }

    @Override
    public boolean eip211() {
        return true;
    }

    @Override
    public boolean eip212() {
        return true;
    }

    @Override
    public boolean eip213() {
        return true;
    }

    @Override
    public boolean eip214() {
        return true;
    }

    @Override
    public boolean eip658() {
        return true;
    }

    @Override
    public boolean eip1052() {
        return true;
    }

    @Override
    public boolean eip145() {
        return true;
    }

    @Override
    public boolean eip1283() {
        return true;
    }

    @Override
    public boolean eip1014() {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }

}
