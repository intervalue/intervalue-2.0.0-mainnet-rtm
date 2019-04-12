package one.inve.http;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * check whether passed parameters are correlated with method
 *               parameter definition
 * @author Francis.Deng
 * @date 2018年11月3日 下午3:29:38
 * @version V1.0
 */
public class ParameterCheckFunction implements IFunction {
	private Object obj;
	private Method method;
	private Object[] parameters;

	@Override
	public boolean isCalculationApproved(Object obj, Method method, Object... parameters) {
		Class[] parameterTypes = method.getParameterTypes();

		final int size = parameterTypes.length;
		if (size != parameters.length) {
			return false;
		}

		for (int index=0; index<size; index++) {
			if ( parameterTypes[index].isInstance(parameters[index].getClass()) ) {
				return false;
			}
		}

		this.obj = obj;
		this.method = method;
		this.parameters = parameters;

		return true;
	}

	@Override
	public Object execute() {
		Object ret = null;
		try {
			method.setAccessible(true);
			ret = method.invoke(obj, parameters);
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			e.printStackTrace();
		}
		
		return ret;
	}

}
