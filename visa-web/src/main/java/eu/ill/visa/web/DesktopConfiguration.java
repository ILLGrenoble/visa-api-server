package eu.ill.visa.web;

import java.util.List;

public class DesktopConfiguration {

    private List<Host>           allowedClipboardUrlHosts;
    private List<KeyboardLayout> keyboardLayouts;

    public DesktopConfiguration() {
    }

    public List<Host> getAllowedClipboardUrlHosts() {
        return allowedClipboardUrlHosts;
    }

    public void setAllowedClipboardUrlHosts(List<Host> allowedClipboardUrlHosts) {
        this.allowedClipboardUrlHosts = allowedClipboardUrlHosts;
    }

    public List<KeyboardLayout> getKeyboardLayouts() {
        return keyboardLayouts;
    }

    public void setKeyboardLayouts(List<KeyboardLayout> keyboardLayouts) {
        this.keyboardLayouts = keyboardLayouts;
    }

    /**
     * Get a keyboard layout for a given layout
     * i.e. en-gb-qwerty
     *
     * @param layout the keyboard layout identifier
     * @return a keyboard layout if found, otherwise null
     */
    public KeyboardLayout getKeyboardLayoutForLayout(final String layout) {
        return keyboardLayouts.stream()
            .filter(keyboardLayout -> keyboardLayout.getLayout().equals(layout))
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
