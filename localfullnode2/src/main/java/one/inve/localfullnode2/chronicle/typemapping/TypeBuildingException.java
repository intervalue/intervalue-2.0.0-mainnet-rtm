package one.inve.localfullnode2.chronicle.typemapping;

public class TypeBuildingException extends RuntimeException {

	private static final long serialVersionUID = -2048811720629989096L;

	public TypeBuildingException(String message) {
		super(message);
	}

	public TypeBuildingException(String message, Throwable cause) {
		super(message, cause);
	}

	public TypeBuildingException(Throwable cause) {
		super(cause);
	}
}