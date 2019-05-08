package one.inve.localfullnode2.dep;

public class NValue extends DependentItem {
	private int nValue;

	public int get() {
		return nValue;
	}

	public void set(int nValue) {
		this.nValue = nValue;
		nodifyAll();
	}

}
