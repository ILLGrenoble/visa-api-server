package eu.ill.visa.cloud.domain;


import java.util.HashMap;
import java.util.Map;

public class CloudInstanceMetadata extends HashMap<String, String> {

    public CloudInstanceMetadata() {

    }

    public CloudInstanceMetadata(Map<String, String> map) {
        this.putAll(map);
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key));
    }

    public double getDouble(String key) {
        return Double.parseDouble(get(key));
    }

    public long getLong(String key) {
        return Long.parseLong(get(key));
    }

    public Boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key));
    }

    public void put(String key, Object value) {
        if (value == null) {
            put(key, null);
        } else {
            put(key, value.toString());
        }
    }
}
