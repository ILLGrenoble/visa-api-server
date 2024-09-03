package eu.ill.visa.broker.brokers.redis.providers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.visa.broker.brokers.redis.RedisMessageCarrier;
import eu.ill.visa.broker.domain.exceptions.MessageMarshallingException;
import io.quarkus.redis.datasource.codecs.Codec;
import jakarta.enterprise.context.ApplicationScoped;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

import static com.fasterxml.jackson.databind.SerializationFeature.WRITE_DATES_AS_TIMESTAMPS;

@ApplicationScoped
public class RedisMessageCarrierCodec implements Codec {

    private final ObjectMapper mapper;

    public RedisMessageCarrierCodec() {
        this.mapper = new ObjectMapper();
        this.mapper.configure(WRITE_DATES_AS_TIMESTAMPS, false);
    }

    @Override
    public boolean canHandle(Type clazz) {
        return clazz.equals(RedisMessageCarrier.class);
    }

    @Override
    public byte[] encode(Object item) {
        var carrier = (RedisMessageCarrier)item;
        try {
            String serialized = mapper.writeValueAsString(carrier);
            return serialized.getBytes(StandardCharsets.UTF_8);

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Object decode(byte[] item) {
        try {
            // Deserialize carrier
            RedisMessageCarrier carrier = this.mapper.readValue(item, RedisMessageCarrier.class);

            try {
                // Deserialize message
                if (carrier.getPayload() == null) {
                    throw new MessageMarshallingException(String.format("Message payload of type %s is Null", carrier.getClassName()));
                }

                Class<?> clazz = Class.forName(carrier.getClassName());
                carrier.setData(this.mapper.convertValue(carrier.getPayload(), clazz));

                return carrier;

            } catch (ClassNotFoundException e) {
                throw new MessageMarshallingException(String.format("Failed to convert message payload to type %s: %s", carrier.getClassName(), e.getMessage()));
            }

        } catch (IOException e) {
            throw new MessageMarshallingException(String.format("Failed to deserialize RedisMessageCarrier: %s", e.getMessage()));
        }
    }
}
