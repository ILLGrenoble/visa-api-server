package eu.ill.visa.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;

@ApplicationScoped
public class DesktopConfigurationImpl {

    private static final Logger logger = LoggerFactory.getLogger(DesktopConfigurationImpl.class);

    private final List<Host> allowedClipboardUrlHosts;
    private final List<KeyboardLayout> keyboardLayouts;

    @Inject
    public DesktopConfigurationImpl(final DesktopConfiguration configuration) {

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            this.allowedClipboardUrlHosts = Arrays.asList(objectMapper.readValue(configuration.allowedClipboardUrlHostsJson(), Host[].class));
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize configuration value allowedClipboardUrlHosts: {}", configuration.allowedClipboardUrlHostsJson());
            throw new RuntimeException(e);
        }
        try {
            this.keyboardLayouts = Arrays.asList(objectMapper.readValue(configuration.keyboardLayoutsJson(), KeyboardLayout[].class));
        } catch (JsonProcessingException e) {
            logger.error("Failed to deserialize configuration value keyboardLayouts: {}", configuration.keyboardLayoutsJson());
            throw new RuntimeException(e);
        }
    }

    public List<Host> getAllowedClipboardUrlHosts() {
        return allowedClipboardUrlHosts;
    }

    public List<KeyboardLayout> getKeyboardLayouts() {
        return keyboardLayouts;
    }


    /**
     * Get a keyboard layout for a given layout
     * i.e. en-gb-qwerty
     *
     * @param layout the keyboard layout identifier
     * @return a keyboard layout if found, otherwise null
     */
    public KeyboardLayout getKeyboardLayoutForLayout(final String layout) {
        return this.keyboardLayouts.stream()
            .filter(keyboardLayout -> keyboardLayout.layout.equals(layout))
            .findAny()
            .orElse(null);
    }

    public static class Host {

        private String  host;
        private boolean https;

        public Host() {

        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public boolean isHttps() {
            return https;
        }

        public void setHttps(boolean https) {
            this.https = https;
        }
    }

    public static class KeyboardLayout {

        private String  layout;
        private String  name;
        private boolean selected;

        public String getLayout() {
            return layout;
        }

        public void setLayout(String layout) {
            this.layout = layout;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }
    }

}
