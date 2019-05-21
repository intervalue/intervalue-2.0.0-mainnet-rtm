package one.inve.contract.sig;

import one.inve.contract.tuple.Tuple;

import java.util.Optional;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: provide a set of java classes that allow you to work with
 *               tuples
 * @author: Francis.Deng
 * @date: 2018年12月13日 下午4:44:24
 * @version: V1.0
 */
public interface ICommand {
	Optional<Tuple> execute(String... actions);
}
