package eu.ill.visa.broker.brokers.redis;

import eu.ill.visa.broker.domain.exceptions.MessageMarshallingException;

public class RedisMessageCarrier {
    private String className;
    private Object payload;
    private Object data;

    public RedisMessageCarrier() {
    }

    public RedisMessageCarrier(Object payload) {
        this.className = payload.getClass().getName();
        this.payload = payload;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public Object getPayload() {
        return payload;
    }

    public void setPayload(Object payload) {
        this.payload = payload;
    }

    @SuppressWarnings("unchecked")
    <T> T getData() throws MessageMarshallingException {
        try {
            return (T)this.data;

        } catch (ClassCastException e) {
            throw new MessageMarshallingException(String.format("Failed to cast message data: %s", e.getMessage()));
        }
    }

    public void setData(Object data) {
        this.data = data;
    }

}
