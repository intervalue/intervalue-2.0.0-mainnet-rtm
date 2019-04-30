package one.inve.localfullnode2.utilities;

import com.alibaba.fastjson.JSONObject;
import one.inve.utils.DSA;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * 密钥工具类
 * @author Clare
 * @date   2018/7/23 0003
 */
public class HnKeyUtils {
    private static final Logger logger = LoggerFactory.getLogger("HnKeyUtils.class");

    public static final class HnKey {
        public String privKey;
        public String pubkey;

        public HnKey() {
        }

        private HnKey(Builder builder) {
            privKey = builder.privKey;
            pubkey = builder.pubkey;
        }


        public static final class Builder {
            private String privKey;
            private String pubkey;

            public Builder() {
            }

            public Builder privKey(String val) {
                privKey = val;
                return this;
            }

            public Builder pubkey(String val) {
                pubkey = val;
                return this;
            }

            public HnKey build() {
                return new HnKey(this);
            }
        }
    }

    public static String getString4PublicKey(PublicKey publicKey) {
        return DSA.encryptBASE64(publicKey.getEncoded());
    }

    public static String getString4PrivateKey(PrivateKey privateKey) {
        return DSA.encryptBASE64(privateKey.getEncoded());
    }

    public static PublicKey getPublicKey4String(String key) throws Exception {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(DSA.decryptBASE64(key));
        return KeyFactory.getInstance("RSA").generatePublic(keySpec);
    }

    public static PrivateKey getPrivateKey4String(String key) throws Exception {
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(DSA.decryptBASE64(key));
        return KeyFactory.getInstance("RSA").generatePrivate(keySpec);
    }

    public static void writeKey2File(KeyPair keyPair, String filePath) {
        try {
            HnKey hnKey = new HnKey.Builder()
                    .privKey(getString4PrivateKey(keyPair.getPrivate()))
                    .pubkey(getString4PublicKey(keyPair.getPublic()))
                    .build();
            FileOutputStream outputStream = new FileOutputStream(new File(filePath));
            outputStream.write(JSONObject.toJSONString(hnKey).getBytes());
            outputStream.close();
        } catch (Exception e) {
            logger.error("error: {}" , e);
        }
    }

    public static KeyPair readKeyFromFile(String filePath) {
        KeyPair keyPair = null;
        try {
            FileInputStream inputStream = new FileInputStream(new File(filePath));
            byte[] data = new byte[inputStream.available()];
            inputStream.read(data);
            inputStream.close();

            JSONObject object = JSONObject.parseObject(new String(data));
            keyPair = new KeyPair(getPublicKey4String(object.getString("pubkey")),
                    getPrivateKey4String(object.getString("privKey")));
        } catch (Exception e) {
            logger.error("error: {}" , e);
        }
        return keyPair;
    }

    public static String getPri2File(String filePath) {
        try {
            FileInputStream in = new FileInputStream(new File(filePath));
            byte[] data = new byte[in.available()];
            in.read(data);
            in.close();
            return new String(data);
        } catch (Exception e) {
            logger.error("error: {}" , e);
            return null;
        }
    }

}
