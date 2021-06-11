package eu.ill.visa.cloud.helpers;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JsonHelper {
    public static JsonObject parse(final String json) {
        final InputStream jsonInputStream = new ByteArrayInputStream(json.getBytes());
        final JsonReader  jsonReader      = Json.createReader(jsonInputStream);
        final JsonObject  out             = jsonReader.readObject();
        jsonReader.close();
        return out;
    }
}
