package one.inve.contract.shell.http;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: check whether passed parameters are correlated with method
 *               parameter definition
 * @author: Francis.Deng
 * @date: 2018年11月3日 下午3:29:38
 * @version: V1.0
 */
public class ParameterTypesEqualityFunction implements IFunc {
	private Object obj;
	private Method method;
	private Object[] parameters;

	@Override
	public boolean isCalculationApproved(Object obj, Method m, Object... parameters) {
		Class[] parameterTypes0 = m.getParameterTypes();
		Class retType0 = m.getReturnType();
		final int size = parameterTypes0.length;
		
		if (size != parameters.length) {
			return false;
		}
		
		for (int index=0;index<size;index++) {
			if (parameterTypes0[index] != parameters[index].getClass()) {
				return false;
			}
		}

		this.obj = obj;
		this.method = m;
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
