package one.inve.core;

import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import org.apache.log4j.Logger;

/**
 * A json serializable message. The base type.
 **/
public class Message {
    public static Logger logger = Logger.getLogger("Message.class");
    public byte[] pubkey;
    private int type;
    public long sequence_no;

    public int getType() {
        return type;
    }
    public String getTypeString() {
        return Types._NAMES_.get(this.getType());
    }

    /**
     * Create the basic type of message.
     * @param type messagetype
     * @param sequence_no of tries
     */
    public Message(byte[] pubkey, int type, long sequence_no){
        this.pubkey = pubkey;
        this.type = type;
        this.sequence_no = sequence_no;
    }

    /**
     * Encode the basic data for the network.
     * @return JSONObject data
     * @throws JSONException
     */
    public JSONObject messageEncode () throws JSONException {
        JSONObject data = new JSONObject();
        data.put("pubkey", this.pubkey);
        data.put("type", this.type);
        data.put("sequence_no", this.sequence_no);
        return data;
    }

    /**
     * Create a specific message from the received message.
     * @param data received message
     * @return specific message object
     * @throws JSONException
     */
    public static Message messageConvert(JSONObject data) throws JSONException {
        int type = 0;
        try {
            type = data.getInteger("type");
        } catch (JSONException e) {
            logger.error(e);
        }
        if (Types.PROPOSE == type){
            return ProposeMessage.messageDecipher(data);
        }
        if (Types.PREVOTE == type){
            return PrevoteMessage.messageDecipher(data);
        }
        if (Types.VOTE == type){
            return VoteMessage.messageDecipher(data);
        }
        return new Message(data.getBytes("pubkey"), data.getInteger("type"), data.getLong("sequence_no"));
    }

    /**
     *
     * @return String with basic data from this message.
     */
    @Override
    public String toString(){
        try {
            return this.messageEncode().toString();
        } catch (JSONException e) {
            logger.error(e);
            return "Error toString/one.inve.core.Message";
        }
    }
}
