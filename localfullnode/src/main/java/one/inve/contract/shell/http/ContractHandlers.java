package one.inve.contract.shell.http;

import one.inve.contract.shell.http.annotation.RequestMapper;
import one.inve.contract.shell.http.annotation.RequestMatchable;
import one.inve.contract.shell.http.handler.ContractHandler;
import one.inve.contract.util.PkgUtil;
import one.inve.contract.util.ReflectionUtil;
import one.inve.node.GeneralNode;
import one.inve.util.ReflectionUtils;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: Because {@link RequestMapper} is applied to
 *               {@link ContractHandler},the class is delegated to find
 *               appropriate method to enrich {@link IFunc}.
 * @author: Francis.Deng
 * @date: 2018年11月3日 上午9:24:48
 * @version: V1.0
 */
public class ContractHandlers {
	private List<Class> targetClasses;
	// private List<Method> targetMethods;
	private RequestMatchable matcher;

	private Class requestMapperClass;

	public ContractHandlers(String pkgName, Class requestMapperClass) {
		this(pkgName, requestMapperClass, null);
	}

	public ContractHandlers(String pkgName, Class requestMapperClass, RequestMatchable matcher) {
		try {
			Class[] allClasses = PkgUtil.getClasses(pkgName);

			for (Class clazz : allClasses) {
				//this.targetMethods = ReflectionUtil.findAnnotatedMethods(clazz, RequestMapper.class);
				if (ReflectionUtil.hasAnnotatedMethod(clazz, requestMapperClass)) {
					if (targetClasses == null) {
						targetClasses = new ArrayList<Class>();
					}
					
					targetClasses.add(clazz);
				}
			}

			this.requestMapperClass = requestMapperClass;
			this.matcher = matcher;
		} catch (ClassNotFoundException e) {
			newRunTimeException(pkgName + " is incorrect", e);
			e.printStackTrace();
		} catch (IOException e) {
			newRunTimeException(pkgName + " is incorrect", e);
			e.printStackTrace();
		}
	}

	private RuntimeException newRunTimeException(String des, Throwable t) {
		throw new RuntimeException(des, t);
	}

	// if actualMatcher is passed and func isCalculationApproved
	public IFunc enrichFunc(GeneralNode node, RequestMatchable alternativeMatcher, IFunc func, Object... params) {
		RequestMatchable actualMatcher = (alternativeMatcher != null) ? alternativeMatcher : matcher;
		
		for (Class c:targetClasses) {
			List<Method> annotatedMethods = ReflectionUtils.findAnnotatedMethods(c, requestMapperClass);
			Constructor constructor = ReflectionUtils.findConstructor(c, new Class[1]);
			if (constructor == null) {		//参数
				// ensure that http service has at least a constructor without parameter.
				continue;
			}
			
			for (Method method : annotatedMethods) {
				RequestMapper requestMapper = (RequestMapper) method.getAnnotation(requestMapperClass);

				if (actualMatcher.isMatched(requestMapper)) {
					Object instance = ReflectionUtils.newInstance(constructor, node);

					if (func.isCalculationApproved(instance, method, params)) {
						return func;
					}
				}
			}			
		}

		return null;

	}

}
