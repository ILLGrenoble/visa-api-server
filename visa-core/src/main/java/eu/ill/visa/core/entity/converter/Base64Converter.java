package eu.ill.visa.core.entity.converter;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.util.Base64;

@Converter
public class Base64Converter implements AttributeConverter<byte[], String> {

    private final static Base64.Encoder encoder = Base64.getEncoder();
    private final static Base64.Decoder decoder = Base64.getDecoder();

    @Override
    public String convertToDatabaseColumn(byte[] bytes) {
        return encoder.encodeToString(bytes);
    }

    @Override
    public byte[] convertToEntityAttribute(String data) {
        if (data == null) {
            return null;
        }
        return decoder.decode(data);
    }
}
