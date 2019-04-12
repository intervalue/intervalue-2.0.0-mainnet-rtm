package one.inve.contract.shell.edge;

import one.inve.contract.shell.edge.TxMixin.InterceptResult;
import one.inve.contract.shell.edge.TxMixin.StringRef;

public class ConsensusEdge implements TxMixin.TxSubmittedMixin {
	private IPeer peer;

	public ConsensusEdge(IPeer peer) {
		super();
		this.peer = peer;
	}

	@Override
	public String doSendMessage(String signed) {
		return peer.sendMessage(signed);
	}

	public String deploySmartContract(String txMessage) {
		return submitTX(txMessage, new EmptyValidator());
	}

	public class EmptyValidator implements TxMixin.Interceptable {

		@Override
		public InterceptResult intercept(StringRef str) {
			InterceptResult ok = InterceptResult.OK;

			if (str.getBytes() == null) {
				ok = new InterceptResult(false, "EmptyValidator:empty json string");
			}

			return ok;
		}

	}


//	public class Signature implements TxMixin.Interceptable{
//
//		@Override
//		public InterceptResult intercept(StringRef str) {
//			InterceptResult ok = InterceptResult.OK;
//
//			String s = new String(str.getBytes());
//			JSONObject unitObject = JSONObject.parseObject(s);
//
//			// generate sig
//
//			// Before:
//			// {"fromAddress":"VZLSZWP6ASHOYEXWC3VIIR4JOXYN32HH","timestamp":11111111111,"pubkey":"A1vr3yRoRTk0Pv+0MqpRK+M723sdftsN4DZohxzO86Pz"}
//			// After:
//			// {"signature":"AITBGAjV3YS+p3EnXAOH1+t2lBjUGofpcbsAmEhcxhSjO5X5oFGVHogTf9Sp/L1FxNQnVkPYO6cp\r\nojdbuyRFT9A=","fromAddress":"VZLSZWP6ASHOYEXWC3VIIR4JOXYN32HH","timestamp":11111111111,"pubkey":"A1vr3yRoRTk0Pv+0MqpRK+M723sdftsN4DZohxzO86Pz"}
//			String source = SignUtil.getSourceString(unitObject);
//			String sig = SignUtil.signStr(SignUtil.getSourceString(unitObject),
//					ECKey.fromPublicOnly(unitObject.getBytes("pubkey")));
//			unitObject.put("signature", sig);
//
//			str = new StringRef(unitObject.toJSONString());
//
//			return ok;
//		}
//	
//	}
}