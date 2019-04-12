package one.inve.contract.shell.http;

import java.lang.reflect.Method;

public interface IFunc {
	boolean isCalculationApproved(Object obj, Method m, Object... parameters);

	Object execute();
}
