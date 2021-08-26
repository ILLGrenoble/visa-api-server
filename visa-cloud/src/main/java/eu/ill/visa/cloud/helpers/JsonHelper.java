package eu.ill.visa.cloud.helpers;

import javax.json.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import static javax.json.Json.*;

public class JsonHelper {
    public static JsonObject parseObject(final String json) {
        final InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes());
        final JsonReader jsonReader = createReader(jsonInputStream);
        final JsonObject out = jsonReader.readObject();
        jsonReader.close();
        return out;
    }

    public static JsonArray parseArray(final String json) {
        final InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes());
        final JsonReader jsonReader = createReader(jsonInputStream);
        final JsonArray out = jsonReader.readArray();
        jsonReader.close();
        return out;
    }

    public static JsonArray toJsonArray(final List<String> values) {
        final JsonArrayBuilder builder = createArrayBuilder();
        values.forEach(builder::add);
        return builder.build();
    }

    public static JsonObject toJsonObject(final Map<String, String> values) {
        final JsonObjectBuilder builder = createObjectBuilder();
        values.forEach(builder::add);
        return builder.build();
    }


}
