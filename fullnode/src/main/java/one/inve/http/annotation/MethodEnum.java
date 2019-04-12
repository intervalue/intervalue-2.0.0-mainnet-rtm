package one.inve.http.annotation;

public enum MethodEnum {
	/**
	 *
	 */
	GET("GET"),
    /**
	 *
	 */
	POST("POST"),
	/**
	 *
	 */
	PUT("PUT"),
	/**
	 *
	 */
	DELETE("PUT");

	private String methodVal;

	private MethodEnum(String methodVal) {
		this.methodVal = methodVal;
	}

	public String toVal() {
		return methodVal;
	}
}
