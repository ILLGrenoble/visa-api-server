package eu.ill.visa.business;

import io.smallrye.config.ConfigMapping;

import java.util.Map;

@ConfigMapping(prefix = "business.notifications")
public interface NotificationConfiguration {

    String adapter();
    Map<String, String> parameters();
    Boolean enabled();
}
