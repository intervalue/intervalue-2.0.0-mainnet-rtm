package one.inve.contract.sig;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.subgraph.orchid.encoders.Hex;
import one.inve.bean.message.ContractMessage;
import one.inve.contract.ContractTransactionData;
import one.inve.contract.encoding.MarshalAndUnMarshal;
import one.inve.contract.sig.struct.ContractEnforcement;
import one.inve.contract.tuple.Pair;
import one.inve.contract.tuple.Tuple;
import one.inve.utils.SignUtil;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.math.BigInteger;
import java.util.Optional;

/**
 * 
 * Copyright © CHXX Co.,Ltd. All rights reserved.
 * 
 * @Description: Output signed message to t.json specified.
 * @author: Francis.Deng
 * @date: 2018年12月11日 下午2:35:45
 * @version: V1.0
 * @version: V1.1 As long as target value is provided and signing process works
 *           well,a pair is returned.
 */
public class OutputSigCommand implements ICommand {
	private static final Logger logger = LoggerFactory.getLogger(OutputSigCommand.class);
	/**
	 * "CD, confInputFile, nonceURL, signingOutputFile" are necessary parameters
	 */
	@Override
	public Optional<Tuple> execute(String... actions) {
		Optional<Tuple> urlAndDataPair = Optional.empty();
		ContractTransactionData ct = new ContractTransactionData();

		ContractEnforcement ce = null;
		if ((ce = readFromConf(actions[0], actions[1])) == null) {
			throw new RuntimeException("find no conf");
		}

		logger.info("get account info from {}", actions[2]);
		String nonce = getNonce(actions[2], ce.getUnit().getSender());

		ct.setNonce(new BigInteger(nonce).toByteArray());

		if (ce.getUnit().getCalldata() != null && !ce.getUnit().getCalldata().equals("")) {
			ct.setCalldata(Hex.decode(ce.getUnit().getCalldata()));
		}
		if (ce.getUnit().getGasPrice() != null && !ce.getUnit().getGasPrice().equals("")) {
			ct.setGasPrice(new BigInteger(ce.getUnit().getGasPrice()).toByteArray());
		}
		if (ce.getUnit().getValue() != null && !ce.getUnit().getValue().equals("")) {
			ct.setValue(new BigInteger(ce.getUnit().getValue()).toByteArray());
		}
		if (ce.getUnit().getGasLimit() != null && !ce.getUnit().getGasLimit().equals("")) {
			ct.setGasLimit(new BigInteger(ce.getUnit().getGasLimit()).toByteArray());
		}
		// non-hex format
		if (ce.getUnit().getToAddress() != null && !ce.getUnit().getToAddress().equals("")) {
			ct.setToAddress(ce.getUnit().getToAddress().getBytes());
		}

		File tjson = new File(actions[0] + File.separator + actions[3]);
		try (FileWriter f2 = new FileWriter(tjson, false)) {
			byte[] d = MarshalAndUnMarshal.marshal(ct);

			logger.info("prepare for signing invocation");
			ContractMessage cm = new ContractMessage(ce.getUnit().getMnemonicCode(), d, ce.getUnit().getSender());

			String o = cm.getMessage();
			// o = o.replace("\\", "");
			JSONObject json = JSON.parseObject(o);
			if (!SignUtil.verify(json.getString("message"))) {
				throw new RuntimeException("failing in sig verification");
			}

			// output to t.json
			f2.write(o.toString());

			if (ce.getTarget() != null && !ce.getTarget().equals("")) {
				urlAndDataPair = Optional.of(Pair.with(ce.getTarget(), o.toString()));
			}

		} catch (Exception e) {
			logger.error("failed to get account info.", e);
		}

		return urlAndDataPair;

	}

	protected ContractEnforcement readFromConf(String path, String confFile) {
		ContractEnforcement ce = null;

		try (JSONReader reader = new JSONReader(new FileReader(new File(path + File.separator + confFile)));) {
			ce = reader.readObject(ContractEnforcement.class);
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(String.format("%s is unavailable", path + File.separator + confFile), e);
		}

		return ce;
	}

	protected String getNonce(String url, String address) {
		String accInfo = getNonce(url, address, "0");

		JSONObject jo = new JSONObject();
		jo = JSON.parseObject(accInfo);
		accInfo = jo.getString("data");
		jo = JSON.parseObject(accInfo);
		BigInteger nonce = jo.getBigInteger("nonce");

		return nonce.toString();
	}

	protected String getNonce(String url, String address, String defaultResponseContent) {
		String responseContext = defaultResponseContent;
		try {
			CloseableHttpClient httpclient = HttpClients.createDefault();
			// 创建httpget.
			HttpPost post = new HttpPost(url);
			JSONObject jo = new JSONObject();
			jo.put("address", address);
			StringEntity ett = new StringEntity(jo.toJSONString());
			post.setEntity(ett);

			logger.info("executing request {}", post.getURI());
			// 执行get请求.
			CloseableHttpResponse response = httpclient.execute(post);

			try {
				// 获取响应实体
				HttpEntity entity = response.getEntity();

				logger.debug("--------------------------------------");
				// 打印响应状态
				logger.debug(response.getStatusLine().toString());
				if (entity != null) {
					// 打印响应内容长度
					logger.info("Response content length: " + entity.getContentLength());
					// 打印响应内容
					String s = EntityUtils.toString(entity);
					logger.info("Response content: " + s);
					responseContext = s;
				}
				logger.debug("------------------------------------");
			} finally {
				response.close();
			}
			httpclient.close();
		} catch (Exception e) {
			logger.error("failed to get account info by address", e);
		} 

		return responseContext;
	}
}
