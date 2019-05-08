package one.inve.localfullnode2.dep;

public class DBId extends DependentItem {
	private String dbId;

	public String get() {
		return dbId;
	}

	public void set(String dbId) {
		this.dbId = dbId;
		nodifyAll();
	}
}
