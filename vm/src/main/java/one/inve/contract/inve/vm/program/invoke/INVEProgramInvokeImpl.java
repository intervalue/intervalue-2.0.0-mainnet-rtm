package one.inve.contract.inve.vm.program.invoke;

import one.inve.contract.ethplugin.core.Repository;
import one.inve.contract.ethplugin.db.BlockStore;
import one.inve.contract.ethplugin.vm.DataWord;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Map;

/**
 * @author Roman Mandeleil
 * @since 03.06.2014
 */
public class INVEProgramInvokeImpl implements INVEProgramInvoke {

    private BlockStore blockStore;
    /**
     * TRANSACTION  env **
     */
    private final DataWord address;
    private final DataWord origin, caller,
            balance, gas, gasPrice, callValue;
    private final long gasLong;

    byte[] msgData;

    /**
     * BLOCK  env **
     */
    private final DataWord timestamp,
            gaslimit;

    private Map<DataWord, DataWord> storage;

    private final Repository repository;
    private boolean byTransaction = true;
    private boolean byTestingSuite = false;
    private int callDeep = 0;
    private boolean isStaticCall = false;

    public INVEProgramInvokeImpl(DataWord address, DataWord origin, DataWord caller, DataWord balance,
                                 DataWord gasPrice, DataWord gas, DataWord callValue, byte[] msgData,
                                 DataWord timestamp,
                                 DataWord gaslimit, Repository repository, int callDeep,
                                 BlockStore blockStore, boolean isStaticCall, boolean byTestingSuite) {

        // Transaction env
        this.address = address;
        this.origin = origin;
        this.caller = caller;
        this.balance = balance;
        this.gasPrice = gasPrice;
        this.gas = gas;
        this.gasLong = this.gas.longValueSafe();
        this.callValue = callValue;
        this.msgData = msgData;

        // last Block env
        this.timestamp = timestamp;
        this.gaslimit = gaslimit;

        this.repository = repository;
        this.byTransaction = false;
        this.callDeep = callDeep;
        this.blockStore = blockStore;
        this.isStaticCall = isStaticCall;
        this.byTestingSuite = byTestingSuite;
    }

    public INVEProgramInvokeImpl(byte[] address, byte[] origin, byte[] caller, byte[] balance,
                                 byte[] gasPrice, byte[] gas, byte[] callValue, byte[] msgData,
                                 long timestamp,
                                 byte[] gaslimit,
                                 Repository repository, BlockStore blockStore,
                                 boolean byTestingSuite) {
        this(address, origin, caller, balance, gasPrice, gas, callValue, msgData, 
                timestamp, gaslimit, repository, blockStore);
        this.byTestingSuite = byTestingSuite;
    }


    public INVEProgramInvokeImpl(byte[] address, byte[] origin, byte[] caller, byte[] balance,
                                 byte[] gasPrice, byte[] gas, byte[] callValue, byte[] msgData,
                                 long timestamp,
                                 byte[] gaslimit,
                                 Repository repository, BlockStore blockStore) {

        // Transaction env
        this.address = DataWord.of(address);
        this.origin = DataWord.of(origin);
        this.caller = DataWord.of(caller);
        this.balance = DataWord.of(balance);
        this.gasPrice = DataWord.of(gasPrice);
        this.gas = DataWord.of(gas);
        this.gasLong = this.gas.longValueSafe();
        this.callValue = DataWord.of(callValue);
        this.msgData = msgData;

        // last Block env
        this.timestamp = DataWord.of(timestamp);
        this.gaslimit = DataWord.of(gaslimit);

        this.repository = repository;
        this.blockStore = blockStore;
    }

    /*           ADDRESS op         */
    public DataWord getOwnerAddress() {
        return address;
    }

    /*           BALANCE op         */
    public DataWord getBalance() {
        return balance;
    }

    /*           ORIGIN op         */
    public DataWord getOriginAddress() {
        return origin;
    }

    /*           CALLER op         */
    public DataWord getCallerAddress() {
        return caller;
    }

    /*           GASPRICE op       */
    public DataWord getMinGasPrice() {
        return gasPrice;
    }

    /*           GAS op       */
    public DataWord getGas() {
        return gas;
    }

    @Override
    public long getGasLong() {
        return gasLong;
    }

    /*          CALLVALUE op    */
    public DataWord getCallValue() {
        return callValue;
    }

    /*****************/
    /***  msg data ***/
    /*****************/
    /* NOTE: In the protocol there is no restriction on the maximum message data,
     * However msgData here is a byte[] and this can't hold more than 2^32-1
     */
    private static BigInteger MAX_MSG_DATA = BigInteger.valueOf(Integer.MAX_VALUE);

    /*     CALLDATALOAD  op   */
    public DataWord getDataValue(DataWord indexData) {

        BigInteger tempIndex = indexData.value();
        int index = tempIndex.intValue(); // possible overflow is caught below
        int size = 32; // maximum datavalue size

        if (msgData == null || index >= msgData.length
                || tempIndex.compareTo(MAX_MSG_DATA) == 1)
            return DataWord.ZERO;
        if (index + size > msgData.length)
            size = msgData.length - index;

        byte[] data = new byte[32];
        System.arraycopy(msgData, index, data, 0, size);
        return DataWord.of(data);
    }

    /*  CALLDATASIZE */
    public DataWord getDataSize() {

        if (msgData == null || msgData.length == 0) return DataWord.ZERO;
        int size = msgData.length;
        return DataWord.of(size);
    }

    /*  CALLDATACOPY */
    public byte[] getDataCopy(DataWord offsetData, DataWord lengthData) {

        int offset = offsetData.intValueSafe();
        int length = lengthData.intValueSafe();

        byte[] data = new byte[length];

        if (msgData == null) return data;
        if (offset > msgData.length) return data;
        if (offset + length > msgData.length) length = msgData.length - offset;

        System.arraycopy(msgData, offset, data, 0, length);

        return data;
    }

    /*     TIMESTAMP op    */
    public DataWord getTimestamp() {
        return timestamp;
    }

    /*     GASLIMIT op    */
    public DataWord getGaslimit() {
        return gaslimit;
    }

    /*  Storage */
    public Map<DataWord, DataWord> getStorage() {
        return storage;
    }

    public Repository getRepository() {
        return repository;
    }

    @Override
    public BlockStore getBlockStore() {
        return blockStore;
    }

    @Override
    public boolean byTransaction() {
        return byTransaction;
    }

    @Override
    public boolean isStaticCall() {
        return isStaticCall;
    }

    @Override
    public boolean byTestingSuite() {
        return byTestingSuite;
    }

    @Override
    public int getCallDeep() {
        return this.callDeep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        INVEProgramInvokeImpl that = (INVEProgramInvokeImpl) o;

        if (byTestingSuite != that.byTestingSuite) return false;
        if (byTransaction != that.byTransaction) return false;
        if (address != null ? !address.equals(that.address) : that.address != null) return false;
        if (balance != null ? !balance.equals(that.balance) : that.balance != null) return false;
        if (callValue != null ? !callValue.equals(that.callValue) : that.callValue != null) return false;
        if (caller != null ? !caller.equals(that.caller) : that.caller != null) return false;
        if (gas != null ? !gas.equals(that.gas) : that.gas != null) return false;
        if (gasPrice != null ? !gasPrice.equals(that.gasPrice) : that.gasPrice != null) return false;
        if (gaslimit != null ? !gaslimit.equals(that.gaslimit) : that.gaslimit != null) return false;
        if (!Arrays.equals(msgData, that.msgData)) return false;
        if (origin != null ? !origin.equals(that.origin) : that.origin != null) return false;
        if (repository != null ? !repository.equals(that.repository) : that.repository != null) return false;
        if (storage != null ? !storage.equals(that.storage) : that.storage != null) return false;
        if (timestamp != null ? !timestamp.equals(that.timestamp) : that.timestamp != null) return false;

        return true;
    }

    @Override
    public String toString() {
        return "ProgramInvokeImpl{" +
                "address=" + address +
                ", origin=" + origin +
                ", caller=" + caller +
                ", balance=" + balance +
                ", gas=" + gas +
                ", gasPrice=" + gasPrice +
                ", callValue=" + callValue +
                ", msgData=" + Arrays.toString(msgData) +
                ", timestamp=" + timestamp +
                ", gaslimit=" + gaslimit +
                ", storage=" + storage +
                ", repository=" + repository +
                ", byTransaction=" + byTransaction +
                ", byTestingSuite=" + byTestingSuite +
                ", callDeep=" + callDeep +
                '}';
    }
}
