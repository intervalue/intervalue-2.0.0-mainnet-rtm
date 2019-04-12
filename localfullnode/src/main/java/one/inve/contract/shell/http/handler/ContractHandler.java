package one.inve.contract.shell.http.handler;

import com.alibaba.fastjson.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import one.inve.contract.Contract;
import one.inve.contract.ContractTransaction;
import one.inve.contract.encoding.MarshalAndUnMarshal;
import one.inve.contract.shell.event.EventNamingSelector;
import one.inve.contract.shell.event.IEvent;
import one.inve.contract.shell.http.annotation.MethodEnum;
import one.inve.contract.shell.http.annotation.RequestMapper;
import one.inve.service.CommonApiService;
import one.inve.utils.DSA;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: combining web mapper with consensus engine.
 * 
 * @author: Francis.Deng
 * @date: 2018年11月8日 下午5:19:06
 * @version: V1.0
 * @version: V1.1 support mock contract node and print version
 * @version: V2.0 t.json content format(which is signed by tools
 *           {@link one.inve.contract.sig.SigMain}) is
 *           '{"message":{"data":"rO0ABXNyACVvbmUuaW52ZS5jb250cmFjdC5Db250cmFjdFRyYW5zYWN0aW9uFxPUL+W74KwCAAxbAANhYml0AAJbQlsACGJ5dGVjb2RlcQB+AAFbAAllbmRvd21lbnRxAH4AAVsADGZ1bmN0aW9uTmFtZXEAfgABWwAIZ2FzTGltaXRxAH4AAVsACGdhc1ByaWNlcQB+AAFbAARoYXNocQB+AAFbAAVub25jZXEAfgABWwAHcmF3SGFzaHEAfgABWwAOcmVjaWV2ZUFkZHJlc3NxAH4AAVsAC3NlbmRBZGRyZXNzcQB+AAFMAAlzaWduYXR1cmV0ADlMb25lL2ludmUvY29udHJhY3QvZXRocGx1Z2luL2NyeXB0by9FQ0tleSRFQ0RTQVNpZ25hdHVyZTt4cHB1cgACW0Ks8xf4BghU4AIAAHhwAAAB2mBgYEBSNBVhAA9XYACA/VthAbyAYQAeYAA5YADzAGBgYEBSYAQ2EGEAS1dj/////3wBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAGAANQQWYxFhDCWBFGEAUFeAYzzP1gsUYQBaV1tgAID9W2EAWGEAbVZbAFs0FWEAZVdgAID9W2EAWGEBIlZbZw3gtrOnZAAANBAVYQCCV2AAgP1bYABUc///////////////////////////FhUVYQDbV2AAgFRz//////////////////////////8ZFjNz//////////////////////////8WF5BVYQEgVltniscjBInoAAA0EGEBIFdgAIBUc///////////////////////////GRYzc///////////////////////////FheQVVtWW2AAVDNz//////////////////////////+QgRaRFhRhAUpXYACA/VtgAFRz//////////////////////////+QgRaQMBYxgBVhCPwCkGBAUWAAYEBRgIMDgYWIiPGTUFBQUBUVYQEgV2AAgP0AoWVienpyMFggzTIVGJgKUio+DjtKBId4N6tOPUPwAEJfDd5mZYDNhOYAKXVxAH4ABAAAAAhFY5GCRPQAAHB1cQB+AAQAAAADAYagdXEAfgAEAAAABDuaygBwdXEAfgAEAAAAAjAwcHVxAH4ABAAAACBJSVlDV0tEWFZCQzRPWVE3S0MzNlRFV09FMzJDNVlTNnVxAH4ABAAAACA0UFM2TVpYNlQ3RUxEU0QyUlVPWlJTWUdDQzVSSE9TN3A=","signature":"32SaCGFhyhCl8T38zYc2N1+yETRJKCjdw9x4sUXpKy2vwSJgSo28El97c3nrbSUCFtnbBzO/Rw0Jqa3oNgLxcmnA==","fromAddress":"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7","type":"CONTRACT","timestamp":1545361510231,"pubkey":"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn"}}
 *           message={"data":"rO0ABXNyACVvbmUuaW52ZS5jb250cmFjdC5Db250cmFjdFRyYW5zYWN0aW9uFxPUL+W74KwCAAxbAANhYml0AAJbQlsACGJ5dGVjb2RlcQB+AAFbAAllbmRvd21lbnRxAH4AAVsADGZ1bmN0aW9uTmFtZXEAfgABWwAIZ2FzTGltaXRxAH4AAVsACGdhc1ByaWNlcQB+AAFbAARoYXNocQB+AAFbAAVub25jZXEAfgABWwAHcmF3SGFzaHEAfgABWwAOcmVjaWV2ZUFkZHJlc3NxAH4AAVsAC3NlbmRBZGRyZXNzcQB+AAFMAAlzaWduYXR1cmV0ADlMb25lL2ludmUvY29udHJhY3QvZXRocGx1Z2luL2NyeXB0by9FQ0tleSRFQ0RTQVNpZ25hdHVyZTt4cHB1cgACW0Ks8xf4BghU4AIAAHhwAAAB2mBgYEBSNBVhAA9XYACA/VthAbyAYQAeYAA5YADzAGBgYEBSYAQ2EGEAS1dj/////3wBAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAGAANQQWYxFhDCWBFGEAUFeAYzzP1gsUYQBaV1tgAID9W2EAWGEAbVZbAFs0FWEAZVdgAID9W2EAWGEBIlZbZw3gtrOnZAAANBAVYQCCV2AAgP1bYABUc///////////////////////////FhUVYQDbV2AAgFRz//////////////////////////8ZFjNz//////////////////////////8WF5BVYQEgVltniscjBInoAAA0EGEBIFdgAIBUc///////////////////////////GRYzc///////////////////////////FheQVVtWW2AAVDNz//////////////////////////+QgRaRFhRhAUpXYACA/VtgAFRz//////////////////////////+QgRaQMBYxgBVhCPwCkGBAUWAAYEBRgIMDgYWIiPGTUFBQUBUVYQEgV2AAgP0AoWVienpyMFggzTIVGJgKUio+DjtKBId4N6tOPUPwAEJfDd5mZYDNhOYAKXVxAH4ABAAAAAhFY5GCRPQAAHB1cQB+AAQAAAADAYagdXEAfgAEAAAABDuaygBwdXEAfgAEAAAAAjAwcHVxAH4ABAAAACBJSVlDV0tEWFZCQzRPWVE3S0MzNlRFV09FMzJDNVlTNnVxAH4ABAAAACA0UFM2TVpYNlQ3RUxEU0QyUlVPWlJTWUdDQzVSSE9TN3A=","signature":"32SaCGFhyhCl8T38zYc2N1+yETRJKCjdw9x4sUXpKy2vwSJgSo28El97c3nrbSUCFtnbBzO/Rw0Jqa3oNgLxcmnA==","fromAddress":"4PS6MZX6T7ELDSD2RUOZRSYGCC5RHOS7","type":"CONTRACT","timestamp":1545361510231,"pubkey":"A78IhF6zjQIGzuzKwrjG9HEISz7/oAoEhyr7AnBr3RWn"}'
 */
public class ContractHandler {

	private static Logger logger = LoggerFactory.getLogger(ContractHandler.class);

	@RequestMapper(value = "/v1/contract", method = MethodEnum.POST)
	public String deployContract(String reqBody) {
		// String outcome =
		// CommonLocalImpl.getInstance(Contract.getInstance().getHostNode()).sendMessage(reqBody);
		String outcome = CommonApiService.sendMessage(reqBody, Contract.getInstance().getHostNode());
		return outcome;
	}

	// <code>curl -H "Content-Type:application/json" -X POST --data @t.json
	// http://localhost:8888/v1/mock/contract</code> <p/>
	// do nothing but emit an event instead of sending to hashnet
	// see {@code one.inve.threads.localfullnode.TransactionsSaveThread}
	@RequestMapper(value = "/v1/mock/contract", method = MethodEnum.POST)
	public String deployMockContract(String reqBody) {
		// ContractMessage contactMessage = JSON.parseObject(reqBody,
		// ContractMessage.class);

		Contract.getInstance().getShell().getEventsLoop().emit(new IEvent() {

			@Override
			public String getName() {
				return EventNamingSelector.ConsensusCompletionEventNaming;
			}

			@Override
			public Object getSource() {
				// ignore it
				return null;
			}

			@Override
			public Object getData() throws RuntimeException {
//				try {
//					String s = new String(contactMessage.data);
//					byte[] decodedBytes = Hex.decode(s);
//
//					// return MarshalAndUnMarshal.unmarshal(contactMessage.data);
//					return MarshalAndUnMarshal.unmarshal(Hex.decode(contactMessage.data));
//				} catch (Exception e) {
//					throw new RuntimeException("MarshalAndUnMarshal.unmarshal error", e);
//				}

				// Simply care about data and how to turn data into
				JSONObject unitObj = JSONObject.parseObject(reqBody);
				ContractTransaction ct0 = null;
				try {
					logger.debug("unitObj is: {}", unitObj);
					
					String dataInMessage = unitObj.getJSONObject("message").getString("data");
					byte[] decryptBASE64edDataByte = DSA.decryptBASE64(dataInMessage);
					ct0 = (ContractTransaction) MarshalAndUnMarshal.unmarshal(decryptBASE64edDataByte,
							ContractTransaction.class);
				} catch (Exception e) {
					e.printStackTrace();
					throw new RuntimeException("unmarshal error", e);
				}

				return ct0;
			}
		});

		return "";
	}

}
