package eu.ill.visa.web.rest;

import java.util.List;

public interface DesktopConfiguration {

    List<Host> allowedClipboardUrlHosts();
    List<KeyboardLayout> keyboardLayouts();


    interface Host {
        String host();
        boolean https();
    }

    interface KeyboardLayout {
        String layout();
        String name();
        boolean selected();
    }

}
