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
package one.inve.contract.inve;

import one.inve.contract.ethplugin.core.Transaction;
import one.inve.contract.ethplugin.datasource.MemSizeEstimator;
import one.inve.contract.ethplugin.util.*;
import one.inve.contract.ethplugin.vm.LogInfo;
import org.spongycastle.util.BigIntegers;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static one.inve.contract.ethplugin.datasource.MemSizeEstimator.ByteArrayEstimator;
import static one.inve.contract.ethplugin.util.ByteUtil.EMPTY_BYTE_ARRAY;
import static one.inve.contract.ethplugin.util.ByteUtil.toHexString;
import static org.apache.commons.lang3.ArrayUtils.nullToEmpty;

/**
 * The transaction receipt is a tuple of three items
 * comprising the transaction, together with the post-transaction state,
 * and the cumulative gas used in the block containing the transaction receipt
 * as of immediately after the transaction has happened,
 */
public class INVETransactionReceipt {
    private Transaction transaction;

    private byte[] txState = EMPTY_BYTE_ARRAY;
    private List<LogInfo> logInfoList = new ArrayList<>();

    private byte[] gasUsed = EMPTY_BYTE_ARRAY;
    private byte[] executionResult = EMPTY_BYTE_ARRAY;
    private String error = "";

    /* Tx Receipt in encoded form */
    private byte[] rlpEncoded;

    public INVETransactionReceipt() {
    }

    public INVETransactionReceipt(byte[] rlp) {

        RLPList params = RLP.decode2(rlp);
        RLPList receipt = (RLPList) params.get(0);

        RLPItem txStateRLP = (RLPItem) receipt.get(0);
        RLPList logs = (RLPList) receipt.get(1);
        RLPItem gasUsedRLP = (RLPItem) receipt.get(2);
        RLPItem result = (RLPItem) receipt.get(3);

        txState = nullToEmpty(txStateRLP.getRLPData());
        gasUsed = gasUsedRLP.getRLPData();
        executionResult = (executionResult = result.getRLPData()) == null ? EMPTY_BYTE_ARRAY : executionResult;

        if (receipt.size() > 4) {
            byte[] errBytes = receipt.get(4).getRLPData();
            error = errBytes != null ? new String(errBytes, StandardCharsets.UTF_8) : "";
        }

        for (RLPElement log : logs) {
            LogInfo logInfo = new LogInfo(log.getRLPData());
            logInfoList.add(logInfo);
        }

        rlpEncoded = rlp;
    }


    public INVETransactionReceipt(byte[] txState, List<LogInfo> logInfoList) {
        this.txState = txState;
        this.logInfoList = logInfoList;
    }

    public INVETransactionReceipt(final RLPList rlpList) {
        if (rlpList == null || rlpList.size() != 3)
            throw new RuntimeException("Should provide RLPList with txState, logInfoList");

        this.txState = rlpList.get(0).getRLPData();

        List<LogInfo> logInfos = new ArrayList<>();
        for (RLPElement logInfoEl: (RLPList) rlpList.get(3)) {
            LogInfo logInfo = new LogInfo(logInfoEl.getRLPData());
            logInfos.add(logInfo);
        }
        this.logInfoList = logInfos;
    }

    public byte[] getPostTxState() {
        return txState;
    }

    public byte[] getGasUsed() {
        return gasUsed;
    }

    public byte[] getExecutionResult() {
        return executionResult;
    }   //若是合約創建,則存入新合約地址的byte[];否則,存入執行的返回值

    public List<LogInfo> getLogInfoList() {
        return logInfoList;
    }

    public boolean isValid() {
        return ByteUtil.byteArrayToLong(gasUsed) > 0;
    }

    public boolean isSuccessful() {
        return error.isEmpty();
    }

    public String getError() {
        return error;
    }

    /**
     *  Used for Receipt trie hash calculation. Should contain only the following items encoded:
     *  [txState, logInfoList]
     */
    public byte[] getReceiptTrieEncoded() {
        return getEncoded(true);
    }

    /**
     * Used for serialization, contains all the receipt data encoded
     */
    public byte[] getEncoded() {
        if (rlpEncoded == null) {
            rlpEncoded = getEncoded(false);
        }

        return rlpEncoded;
    }

    public byte[] getEncoded(boolean receiptTrie) {

        byte[] txStateRLP = RLP.encodeElement(this.txState);

        final byte[] logInfoListRLP;
        if (logInfoList != null) {
            byte[][] logInfoListE = new byte[logInfoList.size()][];

            int i = 0;
            for (LogInfo logInfo : logInfoList) {
                logInfoListE[i] = logInfo.getEncoded();
                ++i;
            }
            logInfoListRLP = RLP.encodeList(logInfoListE);
        } else {
            logInfoListRLP = RLP.encodeList();
        }

        return receiptTrie ?
                RLP.encodeList(txStateRLP, logInfoListRLP):
                RLP.encodeList(txStateRLP, logInfoListRLP,
                        RLP.encodeElement(gasUsed), RLP.encodeElement(executionResult),
                        RLP.encodeElement(error.getBytes(StandardCharsets.UTF_8)));

    }

    public void setPostTxState(byte[] txState) {
        this.txState = txState;
        rlpEncoded = null;
    }

    public void setTxStatus(boolean success) {
        this.txState = success ? new byte[]{1} : new byte[0];
        rlpEncoded = null;
    }

    public boolean hasTxStatus() {
        return txState != null && txState.length <= 1;
    }

    public boolean isTxStatusOK() {
        return txState != null && txState.length == 1 && txState[0] == 1;
    }

    public void setGasUsed(byte[] gasUsed) {
        this.gasUsed = gasUsed;
        rlpEncoded = null;
    }

    public void setGasUsed(long gasUsed) {
        this.gasUsed = BigIntegers.asUnsignedByteArray(BigInteger.valueOf(gasUsed));
        rlpEncoded = null;
    }

    public void setExecutionResult(byte[] executionResult) {
        this.executionResult = executionResult;
        rlpEncoded = null;
    }

    public void setError(String error) {
        this.error = error == null ? "" : error;
    }

    public void setLogInfoList(List<LogInfo> logInfoList) {
        if (logInfoList == null) return;
        this.logInfoList = logInfoList;
        rlpEncoded = null;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Transaction getTransaction() {
        if (transaction == null) throw new NullPointerException("Transaction is not initialized. Use TransactionInfo and BlockStore to setup Transaction instance");
        return transaction;
    }

    @Override
    public String toString() {
        return "TransactionReceipt[" +
                "\n  , " + (hasTxStatus() ? ("txStatus=" + (isTxStatusOK() ? "SUCCESS" : "FAILED"))
                                        : ("txState=" + toHexString(txState))) +
                "\n  , gasUsed=" + BigIntegers.fromUnsignedByteArray(gasUsed) +
                "\n  , error=" + error +
                "\n  , executionResult=" + toHexString(executionResult) +
                "\n  , logs=" + logInfoList +
                ']';
    }

    public long estimateMemSize() {
        return MemEstimator.estimateSize(this);
    }

    public static final MemSizeEstimator<INVETransactionReceipt> MemEstimator = receipt -> {
        if (receipt == null) {
            return 0;
        }
        long logSize = receipt.logInfoList.stream().mapToLong(LogInfo.MemEstimator::estimateSize).sum() + 16;
        return (receipt.transaction == null ? 0 : Transaction.MemEstimator.estimateSize(receipt.transaction)) +
                (receipt.txState == EMPTY_BYTE_ARRAY ? 0 : ByteArrayEstimator.estimateSize(receipt.txState)) +
                (receipt.gasUsed == EMPTY_BYTE_ARRAY ? 0 : ByteArrayEstimator.estimateSize(receipt.gasUsed)) +
                (receipt.executionResult == EMPTY_BYTE_ARRAY ? 0 : ByteArrayEstimator.estimateSize(receipt.executionResult)) +
                ByteArrayEstimator.estimateSize(receipt.rlpEncoded) +
                receipt.error.getBytes().length + 40 +
                logSize;
    };
}
