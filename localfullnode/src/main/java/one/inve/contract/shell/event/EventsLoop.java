package one.inve.contract.shell.event;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: The EventsLoop is core class of the event mechanism: provide a
 *               "register" method which allows its client to bind handler with
 *               event selector
 *               <p>
 *               IEventSelector
 *               </p>
 *               ;besides, any class is able to use "emit" method to emit a
 *               event.But,it does not ensure response.
 * @author: Francis.Deng
 * @date: 2018年11月2日 上午10:23:09
 * @version: V1.0
 */
public class EventsLoop {
	private Map<IEventSelector, List<IHandler>> handlersMap = new ConcurrentHashMap<IEventSelector, List<IHandler>>();

	// register your handler along with a selector which making a decision,allowing
	// multiple handlers.
	public void register(IEventSelector selector, IHandler... handlers) {
		if (selector != null) {
			if (handlersMap.containsKey(selector)) {
				List<IHandler> hlrs = handlersMap.get(selector);
				hlrs.addAll(new ArrayList(Arrays.asList(handlers)));
			} else {
				handlersMap.put(selector, new ArrayList(Arrays.asList(handlers)));
			}

		}
	}


	public void emit(IEvent e) {
		Iterator<IEventSelector> allSelectors = handlersMap.keySet().iterator();
		
		while (allSelectors.hasNext()) {
			IEventSelector selector = allSelectors.next();
			
			if (selector.qualified(e)) {
				List<IHandler> handlers = handlersMap.get(selector);
				
				for (IHandler handler : handlers) {
					handler.handle(e);
				}
			}
		}


	}
}
