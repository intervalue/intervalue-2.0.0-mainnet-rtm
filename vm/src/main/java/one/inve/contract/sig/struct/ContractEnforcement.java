package one.inve.contract.sig.struct;

public class ContractEnforcement {
	private ContractTerms unit;
	private String target;
	private String method;

	public ContractTerms getUnit() {
		return unit;
	}

	public void setUnit(ContractTerms unit) {
		this.unit = unit;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

}
