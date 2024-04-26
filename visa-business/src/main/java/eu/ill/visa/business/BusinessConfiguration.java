package eu.ill.visa.business;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "business")
public interface BusinessConfiguration {

    Integer numberInstanceActionThreads();

    List<MailerConfiguration> notificationConfiguration();

    InstanceConfiguration instanceConfiguration();

    SignatureConfiguration signatureConfiguration();

    SecurityGroupServiceClientConfiguration securityGroupServiceClientConfiguration();
}
