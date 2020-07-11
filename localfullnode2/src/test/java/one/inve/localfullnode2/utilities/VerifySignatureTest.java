package one.inve.localfullnode2.utilities;

import org.junit.Test;

import com.alibaba.fastjson.JSONObject;

public class VerifySignatureTest {
	@Test
	public void testVerifyExoSignature() {
		String message = "{\"fromAddress\":\"EXOZZB647K4ST5U6ZDBHELXIR7EZQW5O6ND\",\"toAddress\":\"EXOXCHCVIK2BCOKCRSVVCGSOYZNT5FFKC6O\",\"amount\":\"1000000000000000000\",\"timestamp\":1594432502267,\"remark\":\"\",\"vers\":\"2.0\",\"pubkey\":\"A4D/b9Nr0zVh4eu6ZU8ZvHgYVKl09QYcdFoIZQoQkSo5\",\"type\":1,\"fee\":\"500000\",\"nrgPrice\":\"1000000000\",\"signature\":\"32fmpRRf8AYEvhQGu2m6BmOudHHoU3PhXPDQHNxUEW4H0enao212hL/Q8OcIo+JYfg1sLSyr1wUzRh3TPz1XGTyA==\"}";
		JSONObject o = JSONObject.parseObject(message);
		boolean isValid = TxVerifyUtils.verifyExoSignature(((JSONObject) o.clone()));

		System.out.println(isValid);
	}
}
