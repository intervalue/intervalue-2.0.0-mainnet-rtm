package one.inve.util;

import com.alibaba.fastjson.JSONObject;
import one.inve.bean.wallet.KeyPair;
import one.inve.utils.DSA;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/**
 * 密钥工具类
 */
public class HnKeyUtils {
    public final static Logger logger = Logger.getLogger("HnKeyUtils.class");

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

    public static String getString4PublicKey(byte[] publicKey) {
        return DSA.encryptBASE64(publicKey);
    }

    public static String getString4PrivateKey(byte[] privateKey) {
        return DSA.encryptBASE64(privateKey);
    }

    public static byte[] getPublicKey4String(String publicKey) throws Exception {
        return DSA.decryptBASE64(publicKey);
    }

    public static byte[] getPrivateKey4String(String privateKey) throws Exception {
        return DSA.decryptBASE64(privateKey);
    }

    public static void writeKey2File(KeyPair keyPair, String filePath) {
        try {
            HnKey hnKey = new HnKey.Builder()
                    .privKey(getString4PrivateKey(keyPair.getPrivateKey()))
                    .pubkey(getString4PublicKey(keyPair.getPublicKey()))
                    .build();
            FileOutputStream outputStream = new FileOutputStream(new File(filePath));
            outputStream.write(JSONObject.toJSONString(hnKey).getBytes());
            outputStream.close();
        } catch (Exception e) {
            logger.error("writeKey2File exception", e);
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
            logger.error("readKeyFromFile exception", e);
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
            logger.error("getPri2File exception", e);
            return null;
        }
    }
}
