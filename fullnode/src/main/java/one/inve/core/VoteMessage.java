package one.inve.core;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;

/**
 * Json serializable Vote message
 */
public class VoteMessage extends Message {

    public boolean leader;
    public byte[] data;

    /**
     * Vote message
     * @param sequence_no of tries
     * @param pubkey the pubkey of the propose node
     * @param leader the leader node
     * @param data of the sensor
     */
    public VoteMessage(long sequence_no, byte[] pubkey, boolean leader, byte[] data) {
        super(pubkey, Types.VOTE, sequence_no);
        this.leader = leader;
        this.data = data;
    }

    /**
     * Create a vote message object from the data out JSONObject.
     * @param data JSONObject
     * @return a new vote message object with the specific data.
     * @throws JSONException
     */
    public static VoteMessage messageDecipher(JSONObject data) throws JSONException {
        return new VoteMessage(data.getLong("sequence_no"), data.getBytes("pubkey"),
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
