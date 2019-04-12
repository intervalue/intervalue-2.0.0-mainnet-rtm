package one.inve.util;

import one.inve.http.annotation.RequestMapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReflectionUtils {
	public static Method findAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		for (Method method : clazz.getMethods()) {
			if (method.isAnnotationPresent(annotationClass)) {
				return (method);
			}
		}
		return (null);
	}

	public static List<Method> findAnnotatedMethods(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		Method[] methods = clazz.getMethods();
		List<Method> annotatedMethods = new ArrayList<Method>(methods.length);
		for (Method method : methods) {
			if (method.isAnnotationPresent(annotationClass)) {
				annotatedMethods.add(method);
			}
		}
		return annotatedMethods;
	}

	public static boolean hasAnnotatedMethod(Class<?> clazz, Class<? extends Annotation> annotationClass) {
		List<Method> allMethods = findAnnotatedMethods(clazz, RequestMapper.class);
		return allMethods.size() > 0;
	}

	private static <TYPE> Constructor<TYPE> findConstructorIntern(Class<TYPE> clazz, Class<?>[] argTypes)
			throws Exception {
		List<Constructor<?>> constructors = getAllConstructors(clazz);
		for (Constructor<?> constructor : constructors) {
			Class<?>[] parmTypes = constructor.getParameterTypes();
			if (parmTypes.length == 0 && argTypes.length == 0) {
				return (Constructor<TYPE>) constructor;
			}

			if (parmTypes.length != argTypes.length) {
				// different number of parameters, does not fit
				continue;
			}
			int i = -1;
			for (int j = 0; j < parmTypes.length; j++) {
				i++;
				Class<?> parmType = parmTypes[i];
				Class<?> argType = argTypes[i];
				// argType==null can be used for any parameterType
				if (argType != null && !parmType.isAssignableFrom(argType)) {
					// the argType cannot be used for the parmType
					// constructor cannot be used
					continue;
				}
			}
			// the constructor has the same number of parameters as the supplied
			// argTypes. And every parameter is assignable from the according
			// argType. so we found it.
			return (Constructor<TYPE>) constructor;
		}
		// no usable constructor found
		return null;
	}

	/**
	 * 
	 * @param clazz
	 * @return
	 */
	public static <TYPE> List<Constructor<?>> getAllConstructors(Class<TYPE> clazz) {
		List<Constructor<?>> constructors = new ArrayList<Constructor<?>>();
		// constructors.addAll(Arrays.asList(clazz.getConstructors()));
		constructors.addAll(Arrays.asList(clazz.getDeclaredConstructors()));
		return constructors;
	}

	/**
	 * finds a Constructor of the class, that can be called with parameters of the
	 * given argTypes. finds Constructors with any visibility using
	 * {@link Class#getDeclaredConstructors()}. in contrast to
	 * {@link Class#getDeclaredConstructor(Class...)} this method finds constructors
	 * also, when the argTypes do not exactly match the parmTypes of the
	 * Constructor, but also if every parmType.isAssignebleOf(argType). an
	 * argType==null matches every parmType.
	 * 
	 * @param clazz
	 * @param argTypes
	 * @return
	 */
	public static <TYPE> Constructor<TYPE> findConstructor(Class<TYPE> clazz, Class<?>... argTypes) {
		try {
			return findConstructorIntern(clazz, argTypes);
		} catch (Exception e) {
			return null;
		}
	}

	public static <TYPE> TYPE newInstance(Class<TYPE> clazz, Class<?>[] argTypes, Object... args) {
		Constructor<TYPE> ct = findConstructor(clazz, argTypes);
		return newInstance(ct, args);

	}

	public static <TYPE> TYPE newInstance(Constructor<TYPE> constructor, Object... args) {
		try {
			constructor.setAccessible(true);
			return (TYPE) constructor.newInstance(args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	/**
	 * Get the underlying class for a type, or null if the type is a variable type.
	 * 
	 * @param type the type
	 * @return the underlying class
	 */
	public static Class<?> getClass(Type type) {
		if (type instanceof Class) {
			return (Class) type;
		} else if (type instanceof ParameterizedType) {
			return getClass(((ParameterizedType) type).getRawType());
		} else if (type instanceof GenericArrayType) {
			Type componentType = ((GenericArrayType) type).getGenericComponentType();
			Class<?> componentClass = getClass(componentType);
			if (componentClass != null) {
				return Array.newInstance(componentClass, 0).getClass();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	/**
	 * to find the actual type parameters of the given type you may find this types
	 * using : {@link Class#getGenericSuperclass()}
	 * {@link Class#getGenericInterfaces()} {@link Method#getGenericReturnType()}
	 * {@link Method#getGenericParameterTypes()} {@link Field#getGenericType()}
	 *
	 * @param genericType
	 * @return the actual type parameters of the type which are of type Class. Type
	 *         parameters of other types are ignored silently !!
	 */
	public static List<Type> getActualTypeArguments(Type genericType) {
		List<Type> list = new ArrayList<Type>();
		if (genericType instanceof ParameterizedType) {
			ParameterizedType aType = (ParameterizedType) genericType;
			Type[] fieldArgTypes = aType.getActualTypeArguments();
			for (Type fieldArgType : fieldArgTypes) {
				list.add(fieldArgType);
			}
		}
		return list;
	}

	/**
	 *
	 * @param method
	 * @param paramTypes
	 * @return ture if the method can be called with the paramTypes
	 */
	public static boolean isInvokeable(Method method, Class<?>... paramTypes) {
		if (method.getParameterTypes().length != paramTypes.length) {
			return false;
		}
		for (int i = 0; i < paramTypes.length; i++) {
			Class<?> paramClass = paramTypes[i];
			if (!(JavaUtils.getWrapperIfPrimitive(method.getParameterTypes()[i])
					.isAssignableFrom(JavaUtils.getWrapperIfPrimitive(paramClass)))) {
				return false;
			}
		}
		return true;
	}

	@SuppressWarnings("unchecked")
	public static <TYPE> TYPE invokeMethod(final Object object, Method method, Object... args) {
		if (object == null) {
			throw new NullPointerException(
					String.format("cannot invoke method %s on object=%s", method.getName(), object));
		}
		try {
			method.setAccessible(true);
			return (TYPE) method.invoke(object, args);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}
}
