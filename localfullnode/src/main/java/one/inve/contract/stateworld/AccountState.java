package one.inve.contract.stateworld;


import one.inve.contract.ethplugin.util.ByteUtil;
import one.inve.contract.ethplugin.util.RLP;
import one.inve.contract.ethplugin.util.RLPList;

import java.util.Objects;

import static one.inve.contract.ethplugin.crypto.HashUtil.EMPTY_TRIE_HASH;
import static one.inve.contract.ethplugin.util.FastByteComparisons.equal;

/**
 * 账户状态
 * author: 肖毅
 * 2018-11-14
 */
public class AccountState {

    //此账户状态的 RLP 编码
    private byte[] rlpEncoded;
    //账户余额：1 inve = 10^6
    private Long balance;
    //此地址用于发起交易（部署与调用合约）的计数
    private Long nonce;
    //合约类型 0普通账户，1合约账户
    private Integer accountType;
    //此账户合约中存储变量的 MPT 的 root
    private byte[] storageRoot;

    public static final Integer EOA = 0;
    public static final Integer CONTRACT = 1;

    public AccountState(Long _balance, Long _nonce) {
        this(_balance, _nonce, EOA, EMPTY_TRIE_HASH);
    }

    public AccountState(Long _balance, Long _nonce, Integer _accountType) {
        this(_balance, _nonce, _accountType, EMPTY_TRIE_HASH);
    }

    public AccountState(Long _balance, Long _nonce, Integer _accountType, byte[] _storageRoot) {
        this.balance = _balance;
        this.nonce = _nonce;
        _accountType = !Objects.equals(_accountType, CONTRACT) ? EOA : CONTRACT;
        this.accountType = _accountType;
        this.storageRoot =
                _storageRoot == EMPTY_TRIE_HASH ||
                        equal(_storageRoot, EMPTY_TRIE_HASH) ?
                        EMPTY_TRIE_HASH : _storageRoot;
    }

    public AccountState(byte[] _rlpData) {
        this.rlpEncoded = _rlpData;

        RLPList items = (RLPList) RLP.decode2(rlpEncoded).get(0);
        this.balance = ByteUtil.byteArrayToLong(items.get(0).getRLPData());
        this.nonce = ByteUtil.byteArrayToLong(items.get(1).getRLPData());
        this.accountType = ByteUtil.byteArrayToInt(items.get(2).getRLPData());
        this.storageRoot = items.get(3).getRLPData();
    }

    /**
     * 若 rlpEncoded 为 null，则先生成再返回
     * @return RLP 编码
     */
    public byte[] getRlpEncoded() {
        if(rlpEncoded == null) {
            byte[] balance_ = RLP.encodeLong(this.balance);
            byte[] nonce_ = RLP.encodeLong(this.nonce);
            byte[] accountType_ = RLP.encodeInt(this.accountType);
            byte[] storageRoot_ = RLP.encodeElement(this.storageRoot);
            this.rlpEncoded = RLP.encodeList(balance_, nonce_, accountType_, storageRoot_);
        }

        return rlpEncoded;
    }

    public Long getBalance() {
        return balance;
    }

    public Long getNonce() {
        return nonce;
    }

    public Integer getAccountType() {
        return accountType;
    }

    public byte[] getStorageRoot() {
        return storageRoot;
    }

    @Override
    public String toString() {
        String type = Objects.equals(accountType, EOA) ? "EOA" : "CONTRACT";

        return "AccountState{" + "\n" +
                "   balance = " + balance + "\n" +
                "   nonce = " + nonce + "\n" +
                "   accountType = " + type + "\n" +
                "   storageRoot=" + storageRoot + "\n" +
                '}';
    }
}
