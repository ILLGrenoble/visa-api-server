package eu.ill.visa.business.services;

import jakarta.enterprise.context.ApplicationScoped;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import static java.lang.String.format;

@ApplicationScoped
public class InstanceNameGeneratorService {
    private static final Random RANDOM = new Random();

    private List<String> getWordsForFile(String fileName) throws IOException {
        try (
            final InputStream inputStream = getClass().getResourceAsStream(fileName);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))
        ) {
            return reader.lines().collect(Collectors.toList());
        } catch (Exception exception) {
            throw new IOException(format("Could not read content for file :%s", fileName), exception);
        }
    }

    private String getPredicate() throws IOException {
        final List<String> words = getWordsForFile("/words/predicates.txt");
        final int index = RANDOM.nextInt(words.size());
        return words.get(index);
    }

    private String getObject() throws IOException {
        final List<String> words = getWordsForFile("/words/objects.txt");
        final int index = RANDOM.nextInt(words.size());
        return words.get(index);
    }

    public String generate() throws IOException {
        return format("%s_%s", getPredicate(), getObject());
    }
}
