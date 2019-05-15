package one.inve.localfullnode2.utilities.http;

import java.util.List;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import one.inve.localfullnode2.utilities.StringUtils;

/**
 * HTTP请求参数解析器, 支持GET, POST
 * 
 * @author Clare
 * @date 2018/11/16 1729.
 */
public class ParameterParser {
	private static final String TEXT_PLAIN_VALUE = "text/plain";

	/**
	 * 解析请求参数
	 * 
	 * @return 包含所有请求参数的键值对, 如果没有参数, 则返回空Map
	 *
	 * @throws Exception 异常
	 */
	public static DataMap<String, Object> parse(FullHttpRequest request) throws Exception {
		DataMap<String, Object> paramMap = new DataMap<>();

		HttpMethod method = request.method();
		if (HttpMethod.GET == method) {
			// 是GET请求
			QueryStringDecoder decoder = new QueryStringDecoder(request.uri());
			decoder.parameters().forEach((key, value) -> {
				// entry.getValue()是一个List, 只取第一个元素
				paramMap.put(key, value.get(0));
			});
		} else if (HttpMethod.POST == method) {
			String contentType = request.headers().get("Content-type");
			if (StringUtils.isEmpty(contentType)) {
				throw new Exception("http request header content type is empty.");
			} else if (contentType.startsWith(TEXT_PLAIN_VALUE)) {
				if (request.content().isReadable()) {
					String json = request.content().toString(CharsetUtil.UTF_8);
					if (StringUtils.isNotEmpty(json)) {
						paramMap.putAll(convertJsonStr2Map(json));
					}
				}
			}

			// 是POST请求
			HttpPostRequestDecoder decoder = new HttpPostRequestDecoder(request);
			decoder.offer(request);
			List<InterfaceHttpData> parmList = decoder.getBodyHttpDatas();
			for (InterfaceHttpData parm : parmList) {
				Attribute data = (Attribute) parm;
				paramMap.put(data.getName(), data.getValue());
			}
		} else {
			throw new Exception("http request method is not supported.");
		}

		return paramMap;
	}

	/**
	 * 将json转化成map
	 * 
	 * @param jsonStr json串
	 * @return map
	 */
	private static DataMap<String, Object> convertJsonStr2Map(String jsonStr) throws Exception {
		try {
			return JSON.parseObject(jsonStr, new TypeReference<DataMap<String, Object>>() {
			});
		} catch (Exception e) {
			throw new Exception("http request body is not json text.");
		}
	}
}