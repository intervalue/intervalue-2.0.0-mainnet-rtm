package one.inve.http;

import java.lang.reflect.Method;

/**
 * @author Francis.Deng
 * @date 2018年11月3日 下午3:29:38
 */
public interface IFunction {
	boolean isCalculationApproved(Object obj, Method m, Object... parameters);

	Object execute();
}
