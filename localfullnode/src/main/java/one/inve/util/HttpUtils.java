package one.inve.util;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.ssl.TrustStrategy;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模拟请求工具类
 */
public class HttpUtils {

    /**
     * http get请求
     *
     * @param url
     * @return
     */
    public static String httpGet(String url) throws IOException, ClassNotFoundException, NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        return httpGet(url, null, false);
    }

    /**
     * 、
     * https get 请求
     *
     * @param url
     * @return
     * @throws ClassNotFoundException
     * @throws KeyManagementException
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws IOException
     */
    public static String httpsGet(String url) throws ClassNotFoundException, KeyManagementException, NoSuchAlgorithmException, KeyStoreException, IOException {
        return httpGet(url, null, true);
    }

    /**
     * http post 请求
     *
     * @param url
     * @param params
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public static String httpPost(String url, HashMap<String, String> params) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        return httpPost(url, params, null, false);
    }

    /**
     * https post 请求
     *
     * @param url
     * @param params
     * @return
     * @throws NoSuchAlgorithmException
     * @throws KeyStoreException
     * @throws KeyManagementException
     */
    public static String httpsPost(String url, HashMap<String, String> params) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        return httpPost(url, params, null, true);
    }

    /**
     * @param @return 参数
     * @return String    返回类型
     * @throws
     * @Title: httpGet
     */
    public static String httpGet(String url, HashMap<String, Object> maps, boolean https) throws IOException, ClassNotFoundException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // 创建HttpClient上下文
        HttpClientContext context = HttpClientContext.create();

        // 创建一个CloseableHttpClient
        CloseableHttpClient httpClient = null;
        if (https) {
            httpClient = getCloseableHttpClient();
        } else {
            httpClient = HttpClients.createDefault();
        }
        // get method
        HttpGet httpGet = new HttpGet(url);

        //设置header
        if (maps != null) {
            if (maps.containsKey(HttpHeaders.REFERER)) {
                httpGet.setHeader(HttpHeaders.REFERER, (String) maps.get(HttpHeaders.REFERER));
            }
            if (maps.containsKey(HttpHeaders.HOST)) {
                httpGet.setHeader(HttpHeaders.HOST, (String) maps.get(HttpHeaders.HOST));
            }
            if (maps.containsKey(HttpHeaders.USER_AGENT)) {
                httpGet.setHeader(HttpHeaders.USER_AGENT, (String) maps.get(HttpHeaders.USER_AGENT));
            }
            if (maps.containsKey(HttpHeaders.ACCEPT)) {
                httpGet.setHeader(HttpHeaders.ACCEPT, (String) maps.get(HttpHeaders.ACCEPT));
            }
        }

        //response
        CloseableHttpResponse response = null;
        //get response into String
        String result = "";

        response = httpClient.execute(httpGet, context);
        HttpEntity entity = response.getEntity();
        result = EntityUtils.toString(entity, "UTF-8");

        //释放连接
        httpGet.releaseConnection();
        httpClient.close();
        return result;
    }

    /**
     * 创建CloseableHttpClient
     *
     * @return
     * @throws KeyStoreException
     * @throws NoSuchAlgorithmException
     * @throws KeyManagementException
     */
    private static CloseableHttpClient getCloseableHttpClient() throws KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
        // 全局请求设置
        RequestConfig globalConfig = RequestConfig.custom().setCookieSpec(CookieSpecs.STANDARD).setConnectionRequestTimeout(10000).setConnectTimeout(10000).setSocketTimeout(10000).build();

        //SSL 过滤
        SSLContextBuilder builder = new SSLContextBuilder();
        builder.loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                return true;
            }
        });
        SSLConnectionSocketFactory sslConnectionSocketFactory = new SSLConnectionSocketFactory(builder.build(), new String[]{"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"}, null, NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http", PlainConnectionSocketFactory.INSTANCE).register("https", trustAllHttpsCertificates()).build();
        PoolingHttpClientConnectionManager manager = new PoolingHttpClientConnectionManager(registry);
        manager.setMaxTotal(200);

        // http 请求默认设置
        HttpClientBuilder custom = HttpClients.custom();
        custom.setDefaultRequestConfig(globalConfig);
        custom.setSSLSocketFactory(sslConnectionSocketFactory);
        custom.setConnectionManager(manager);
        custom.setConnectionManagerShared(true);
        return custom.build();
    }

    /**
     * SSL  https 构建
     *
     * @return
     */
    private static SSLConnectionSocketFactory trustAllHttpsCertificates() {
        SSLConnectionSocketFactory socketFactory = null;
        TrustManager[] trustAllCerts = new TrustManager[1];
        TrustManager tm = new miTM();
        trustAllCerts[0] = tm;
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, null);
            socketFactory = new SSLConnectionSocketFactory(sc, NoopHostnameVerifier.INSTANCE);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
        return socketFactory;
    }

    static class miTM implements TrustManager, X509TrustManager {

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] certs, String authType) {
            //don't check
        }

        @Override
        public void checkClientTrusted(X509Certificate[] certs, String authType) {
            //don't check
        }

    }


    /**
     * @param @return 参数
     * @return String    返回类型
     * @throws
     * @Title: httpPost
     */
    public static String httpPost(String url, HashMap<String, String> params, HashMap<String, Object> config, boolean https) throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException, IOException {
        //httpClient
        //HttpClient httpClient = getCloseableHttpClient();
        HttpClient httpClient = null;
        if (https) {
            httpClient = getCloseableHttpClient();
        } else {
            httpClient = HttpClients.createDefault();
        }

        // get method
        HttpPost httpPost = new HttpPost(url);

        //设置header
        if (config != null) {
            if (config.containsKey(HttpHeaders.REFERER)) {
                httpPost.setHeader(HttpHeaders.REFERER, (String) config.get(HttpHeaders.REFERER));
            }
            if (config.containsKey(HttpHeaders.HOST)) {
                httpPost.setHeader(HttpHeaders.HOST, (String) config.get(HttpHeaders.HOST));
            }
            if (config.containsKey(HttpHeaders.USER_AGENT)) {
                httpPost.setHeader(HttpHeaders.USER_AGENT, (String) config.get(HttpHeaders.USER_AGENT));
            }
            if (config.containsKey(HttpHeaders.ACCEPT)) {
                httpPost.setHeader(HttpHeaders.ACCEPT, (String) config.get(HttpHeaders.ACCEPT));
            }
            if (config.containsKey(HttpHeaders.CONTENT_TYPE)) {
                httpPost.setHeader(HttpHeaders.CONTENT_TYPE, (String) config.get(HttpHeaders.CONTENT_TYPE));
            }
            if (config.containsKey("Origin")) {
                httpPost.setHeader("Origin", (String) config.get("Origin"));
            }
        }

        //set params post参数
        List<NameValuePair> listParams = new ArrayList<NameValuePair>();

        //添加post参数
        for (Map.Entry<String, String> entry : params.entrySet()) {
            listParams.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
        }
        String result = "";
        try {
            //response
            httpPost.setEntity(new UrlEncodedFormEntity(listParams, "UTF-8"));
            HttpResponse response = null;
            response = httpClient.execute(httpPost);
            //get response into String

            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            httpPost.releaseConnection();
            ((CloseableHttpClient) httpClient).close();
        }
        return result;
    }
}
