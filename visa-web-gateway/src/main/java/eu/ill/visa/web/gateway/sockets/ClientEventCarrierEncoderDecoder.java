package eu.ill.visa.web.gateway.sockets;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.ill.visa.broker.domain.models.ClientEventCarrier;
import jakarta.websocket.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientEventCarrierEncoderDecoder implements Encoder.Text<ClientEventCarrier>, Decoder.Text<ClientEventCarrier>{

    private static final Logger logger = LoggerFactory.getLogger(ClientEventCarrierEncoderDecoder.class);
    private final ObjectMapper mapper = new ObjectMapper();

    @Override
    public ClientEventCarrier decode(String message) throws DecodeException {
        try {
            return mapper.readValue(message, ClientEventCarrier.class);

        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize websocket object: {}", e.getMessage());
            throw new DecodeException(message, String.format("Failed to deserialize websocket object: %s", e.getMessage()), e);
        }
    }

    @Override
    public boolean willDecode(String message) {
        try {
            mapper.readTree(message);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    @Override
    public String encode(ClientEventCarrier eventCarrier) throws EncodeException {
        try {
            return mapper.writeValueAsString(eventCarrier);

        } catch (JsonProcessingException e) {
            logger.error("Failed to serialize ClientEventCarrier: {}", e.getMessage());
            throw new EncodeException(eventCarrier, String.format("Failed to deserialize websocket object: %s", e.getMessage()), e);
        }
    }

    @Override
    public void init(EndpointConfig config) {
        Encoder.Text.super.init(config);
    }

    @Override
    public void destroy() {
        Encoder.Text.super.destroy();
    }
}
