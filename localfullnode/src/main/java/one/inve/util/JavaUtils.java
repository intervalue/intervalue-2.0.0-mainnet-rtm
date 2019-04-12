package one.inve.util;

public class JavaUtils {
	/**
	 * @param cls
	 * @return the wrapper class, if cls is a primitive. else returns cls
	 */
	public static Class<?> getWrapperIfPrimitive(Class<?> cls) {
		if (cls.equals(Byte.TYPE)) {
			return Byte.class;
		} else if (cls.equals(Short.TYPE)) {
			return Short.class;
		} else if (cls.equals(Integer.TYPE)) {
			return Integer.class;
		} else if (cls.equals(Long.TYPE)) {
			return Long.class;
		} else if (cls.equals(Float.TYPE)) {
			return Float.class;
		} else if (cls.equals(Double.TYPE)) {
			return Double.class;
		} else if (cls.equals(Character.TYPE)) {
			return Character.class;
		} else if (cls.equals(Boolean.TYPE)) {
			return Boolean.class;
		} else if (cls.equals(Void.TYPE)) {
			return Void.class;
		} else {
			return cls;
		}
	}
}
