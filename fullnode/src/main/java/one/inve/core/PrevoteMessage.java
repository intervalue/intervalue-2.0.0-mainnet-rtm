package one.inve.core;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * Json serializable Prevote message
 */
public class PrevoteMessage extends Message {

    public boolean leader;
    public byte[] data;

    /**
     * Prevote message
     * @param sequence_no of tries
     * @param pubkey the pubkey of the propose node
     * @param leader the leading node
     * @param data from the node
     */
    public PrevoteMessage(long sequence_no, byte[] pubkey, boolean leader, byte[] data) {
        super(pubkey, Types.PREVOTE, sequence_no);
        this.leader = leader;
        this.data = data;
    }

    /**
     * Create a prevote message object from the data out JSONObject.
     * @param data JSONObject
     * @return a new prevote message object with the specific data.
     * @throws JSONException
     */
    public static PrevoteMessage messageDecipher(JSONObject data) throws JSONException {
        return new PrevoteMessage(data.getLong("sequence_no"), data.getBytes("pubkey"),
                data.getBoolean("leader"), data.getBytes("data"));
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
        data.put("data", this.data);
        return data;
    }
}
