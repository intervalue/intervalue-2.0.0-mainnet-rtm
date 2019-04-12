package one.inve.core;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * Json serializable Propose message
 */
public class ProposeMessage extends Message {

    public boolean leader;
    public byte[] sign;
    public byte[] data;

    /**
     * Propose message
     * @param sequence_no of tries
     * @param pubkey the pubkey of the propose node
     * @param leader
     * @param sign
     * @param data 数据
     */
    public ProposeMessage(long sequence_no, byte[] pubkey, boolean leader, byte[] sign, byte[] data) {
        super(pubkey, Types.PROPOSE, sequence_no);
        this.leader = leader;
        this.sign = sign;
        this.data = data;
    }

    /**
     * Create a propose message object from the data out JSONObject.
     * @param data JSONObject
     * @return a new propose message object with the specific data.
     * @throws JSONException
     */
    public static ProposeMessage messageDecipher(JSONObject data) throws JSONException {
        return new ProposeMessage(data.getLong("sequence_no"), data.getBytes("pubkey"), data.getBoolean("leader"),
                data.getBytes("sign"), data.getString("data").getBytes()
        );
    }

    /**
     * Create JSONObject for the network.
     * @return data JSONObject
     * @throws JSONException
     */
    @Override
    public JSONObject messageEncode() throws JSONException {
        JSONObject data = super.messageEncode();
        data.put("leader", this.leader);
        data.put("sign", this.sign);
        data.put("data", new String(this.data));
        return data;
    }
}

