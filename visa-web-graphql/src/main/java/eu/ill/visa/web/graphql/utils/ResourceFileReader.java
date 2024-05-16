package eu.ill.visa.web.graphql.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class ResourceFileReader {
    private static final Logger logger = LoggerFactory.getLogger(ResourceFileReader.class);

    public static String readResource(final String resource) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        try (InputStream inputStream = classLoader.getResourceAsStream(resource)) {
            if (inputStream == null) {
                logger.error("Failed to read resource file {}: file not found", resource);
            } else {
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));
                StringBuilder stringBuilder = new StringBuilder();
                String ls = System.lineSeparator();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line);
                    stringBuilder.append(ls);
                }
                reader.close();
                return stringBuilder.toString();
            }

        } catch (IOException e) {
            logger.error("Failed to read resource file {}: {}", resource, e.getMessage());
        }
        return null;

    }
}
