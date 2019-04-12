package one.inve.threads;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import one.inve.bean.node.ChainTypes;
import one.inve.node.GeneralNode;
import one.inve.util.HttpUtils;
import one.inve.util.StringUtils;
import org.apache.log4j.Logger;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;


/**
 * 维护更新多币种汇率
 * Created by Clare  on 2018/12/12 0006.
 */
public class ExchangeRateThread extends Thread {
    private final static Logger logger = Logger.getLogger("ExchangeRateThread.class");
//    private final static String INVE_PRICE_URL = "http://api.fcoin.com/v2/market/ticker/inveusdt";
    private final static String OTHER_PRICE_URL = "http://openapi.chainfin.online/openapi/v2/dcmarket/hq/price";
//    private final static String OTHER_PRICE_URL = "http://192.168.5.50:8088/openapi/v2/dcmarket/hq/price";

    private GeneralNode node;

    public ExchangeRateThread(GeneralNode node) {
        this.node = node;
    }

    @Override
    public void run() {
        logger.info("start ExchangeRateThread...");
        Instant t0;
        while (true) {
            t0 = Instant.now();
//            // inve
//            String inveInfo = null;
//            try {
//                inveInfo = HttpUtils.httpGet(INVE_PRICE_URL);
//                logger.info("inveInfo: " + inveInfo);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
            // other: btc/eth/snc
            String otherInfo = null;
            try {
                otherInfo = HttpUtils.httpGet(OTHER_PRICE_URL+"?name="
                        +String.join(",", ChainTypes.NAMES.values()));
                logger.info("otherInfo: " + otherInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 获取价格
//            if (StringUtils.isNotEmpty(inveInfo)) {
//                JSONObject o = JSONObject.parseObject(inveInfo);
//                if (0==o.getInteger("status")) {
//                    node.getChainTokenPrices()[ChainTypes.INVE-1] =
//                            o.getJSONObject("data").getJSONArray("ticker").getDouble(0);
//                }
//            }

            try {
                if (StringUtils.isNotEmpty(otherInfo)) {
                    JSONObject o = JSONObject.parseObject(otherInfo);
                    if (o.getInteger("code") == 0) {
                        o.getJSONArray("list").forEach(p -> {
                            JSONObject po = (JSONObject) p;
                            for (Map.Entry<Integer, String> entry : ChainTypes.NAMES.entrySet()) {
                                if (entry.getValue().toUpperCase()
                                        .equals(po.getString("name").toUpperCase())) {
                                    node.getChainTokenPrices()[entry.getKey() - 1] = po.getDouble("price");
                                    break;
                                }
                            }
                        });
                    } else {
                        logger.warn("request other prices failed or data null.");
                    }
                }
                // 计算汇率
                StringBuilder key;
                int size = ChainTypes.NAMES.size();
                for (int i = 0; i < size; i++) {
                    for (int j = i + 1; j < size; j++) {
                        key = new StringBuilder();
                        key.append(ChainTypes.NAMES.get(i + 1)).append(":").append(ChainTypes.NAMES.get(j + 1));
                        if (0 != node.getChainTokenPrices()[j]) {
                            BigDecimal bigDecimal =
                                    new BigDecimal(node.getChainTokenPrices()[i] / node.getChainTokenPrices()[j]);
                            node.getRatios().put(key.toString(),
                                    bigDecimal.setScale(9, BigDecimal.ROUND_HALF_UP).toString());
                        }
                    }
                }
            } catch (Exception e) {
                logger.error("error: {}", e);
            }

            long interval = Duration.between(t0, Instant.now()).toMillis();
            if (interval < node.parameters.ratioUpdateMinInterval) {
                try {
                    Thread.sleep(node.parameters.ratioUpdateMinInterval-interval);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void main(String[] args) {
        String inveInfo = "{\"status\":0,\"data\":{\"ticker\":[0.005289020,169.840000000,0.005229710,5000.000000000,0.005450000,20.000000000,0.005622940000000000,0.005774150000000000,0.005229740000000000,18724872.970000000000000000,100278.369817839200000000],\"type\":\"ticker.inveusdt\",\"seq\":481758}}";
//        try {
//            inveInfo = HttpUtils.httpGet(INVE_PRICE_URL);
//            logger.info("inveInfo: " + inveInfo);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
        JSONObject data = JSONObject.parseObject(inveInfo).getJSONObject("data");
        JSONArray a = data.getJSONArray("ticker");
        System.out.println(a.get(0));

        String otherInfo = "{ \"msg\": \"success\", \"code\": 0, \"list\": [ { \"price\": 3457.7717, \"name\": \"BTC\" }, { \"price\": 89.96904, \"name\": \"ETH\" } ] }";
        try {
            HashMap<String, Object> data1 = new HashMap<>();
            data1.put("name", String.join(",", ChainTypes.NAMES.values()));
            otherInfo = HttpUtils.httpGet(OTHER_PRICE_URL+"?name="+String.join(",", ChainTypes.NAMES.values()));
            logger.info("otherInfo: " + otherInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }
        JSONObject o = JSONObject.parseObject(otherInfo);
        if (o.getInteger("code")==0 ) {
            JSONArray list = o.getJSONArray("list");
            list.stream().forEach(p -> {
                JSONObject po = (JSONObject)p;
                for (Map.Entry<Integer, String> entry : ChainTypes.NAMES.entrySet()) {
                    if (entry.getValue().toUpperCase().equals(po.getString("name").toUpperCase())) {
                        System.out.println("name:" + entry.getValue() + ", price: " + po.getDouble("price"));
                        break;
                    }
                }
            });
        } else {
            logger.warn("request other prices failed or data null.");
        }

        System.out.println(ChainTypes.BTC);
        System.out.println(ChainTypes.NAMES.get(ChainTypes.BTC));

        System.out.println(String.join(",", ChainTypes.NAMES.values()));
    }
}
