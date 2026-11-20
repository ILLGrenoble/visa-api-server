package eu.ill.visa.common;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "common", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface CommonConfiguration {

    ErrorReportEmailConfiguration errorReportEmail();
}
