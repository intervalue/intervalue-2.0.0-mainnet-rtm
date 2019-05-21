package one.inve.contract.inve.vm.trace;

import one.inve.contract.ethplugin.config.SystemProperties;
import one.inve.contract.ethplugin.vm.DataWord;
import one.inve.contract.ethplugin.vm.OpCode;
import one.inve.contract.inve.vm.program.invoke.INVEProgramInvoke;
import org.spongycastle.util.encoders.Hex;

import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;
import static one.inve.contract.ethplugin.util.ByteUtil.toHexString;
import static one.inve.contract.ethplugin.vm.trace.Serializers.serializeFieldsOnly;

public class INVEProgramTrace {

    private List<Op> ops = new ArrayList<>();
    private String result;
    private String error;
    private String contractAddress;

    public INVEProgramTrace() {
        this(null, null);
    }

    public INVEProgramTrace(SystemProperties config, INVEProgramInvoke programInvoke) {
        if (programInvoke != null && config.vmTrace()) {
            contractAddress = Hex.toHexString(programInvoke.getOwnerAddress().getLast20Bytes());
        }
    }

    public List<Op> getOps() {
        return ops;
    }

    public void setOps(List<Op> ops) {
        this.ops = ops;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getContractAddress() {
        return contractAddress;
    }

    public void setContractAddress(String contractAddress) {
        this.contractAddress = contractAddress;
    }

    public INVEProgramTrace result(byte[] result) {
        setResult(toHexString(result));
        return this;
    }

    public INVEProgramTrace error(Exception error) {
        setError(error == null ? "" : format("%s: %s", error.getClass(), error.getMessage()));
        return this;
    }

    public Op addOp(byte code, int pc, int deep, DataWord gas, OpActions actions) {
        Op op = new Op();
        op.setActions(actions);
        op.setCode(OpCode.code(code));
        op.setDeep(deep);
        op.setGas(gas.value());
        op.setPc(pc);

        ops.add(op);

        return op;
    }

    /**
     * Used for merging sub calls execution.
     */
    public void merge(INVEProgramTrace programTrace) {
        this.ops.addAll(programTrace.ops);
    }

    public String asJsonString(boolean formatted) {
        return serializeFieldsOnly(this, formatted);
    }

    @Override
    public String toString() {
        return asJsonString(true);
    }
}
