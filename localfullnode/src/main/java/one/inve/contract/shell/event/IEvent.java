package one.inve.contract.shell.event;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: Implementing the interface to satisfy anything in no
 *               circumstance
 * @author: Francis.Deng
 * @date: 2018年11月2日 上午10:35:13
 * @version: V1.0
 */
public interface IEvent {
	String getName();

	Object getSource();

	Object getData();
}
