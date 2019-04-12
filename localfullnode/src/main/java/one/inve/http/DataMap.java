package one.inve.http;

import java.util.HashMap;

public class DataMap<K,V> extends HashMap<K,V> {
    public String getString(String key) {
        return (null==super.get(key)) ? null : "" + super.get(key);
    }

    public Integer getInteger(String key) {
        return (null==super.get(key)) ? null : Integer.parseInt(""+super.get(key));
    }

    public Long getLong(String key) {
        return (null==super.get(key)) ? null : Long.parseLong(""+super.get(key));
    }

    public Double getDouble(String key) {
        return (null==super.get(key)) ? null : Double.parseDouble(""+super.get(key));
    }

    public Boolean getBoolean(String key) {
        return (null==super.get(key)) ? null : Boolean.parseBoolean(""+super.get(key));
    }

    public Byte getByte(String key) {
        return (null==super.get(key)) ? null : Byte.parseByte(""+super.get(key));
    }

    public Short getShort(String key) {
        return (null==super.get(key)) ? null : Short.parseShort(""+super.get(key));
    }
}
