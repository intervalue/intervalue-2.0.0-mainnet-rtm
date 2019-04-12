package one.inve.contract.ethplugin.invocation;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.AbstractApplicationContext;


/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: the only mission of the class is to construct
 *               {@link Constructors},which strongly depend on spring mechanism.
 * @author: Francis.Deng
 * @date: Nov 18, 2018 6:12:07 PM
 * @version: V1.0
 */
public class ConstructorsConstructor {
	public static Constructors getConstructors() {
		AbstractApplicationContext context = new AnnotationConfigApplicationContext(InvocationConfig.class);
		context.registerShutdownHook();
		return context.getBean(Constructors.class);
	}
}
