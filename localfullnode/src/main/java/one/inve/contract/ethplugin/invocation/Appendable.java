package one.inve.contract.ethplugin.invocation;

public interface Appendable {
	void write(byte[] bytes);

	byte[] readLast();

	byte[] readFirst();
	
	boolean isPrehistoric();
}
