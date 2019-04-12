package one.inve.http;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.QueryStringDecoder;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import io.netty.util.CharsetUtil;
import one.inve.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * HTTP请求参数解析器, 支持GET, POST
 * @author Clare
 * @date   2018/11/16 1729.
 */
public class ParameterParser {
    private static Logger logger = LoggerFactory.getLogger(ParameterParser.class);
    private static final String TEXT_PLAIN_VALUE = "text/plain";
    private static final String APP_JSON_VALUE = "application/json";

    /**
     * 解析请求参数
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
                logger.error("http request header content type is empty. param: {}", JSON.toJSONString(request));
                throw new Exception("http request header content type is empty.");
            } else if (contentType.startsWith(TEXT_PLAIN_VALUE) || contentType.startsWith(APP_JSON_VALUE)) {
                if(request.content().isReadable()){
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
     * @param jsonStr json串
     * @return map
     */
    private static DataMap<String, Object> convertJsonStr2Map(String jsonStr) throws Exception {
        try {
            return JSON.parseObject(jsonStr, new TypeReference<DataMap<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("http request body is not json text.");
        }
    }

    public static void main(String[] args) throws Exception {
         String jsonStr = "{" +
                 "    \"pubkey\":\"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\"" +
                 "}";
        convertJsonStr2Map(jsonStr);
    }
}