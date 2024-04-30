package eu.ill.visa.business;

import io.smallrye.config.ConfigMapping;

@ConfigMapping(prefix = "business", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface BusinessConfiguration {

    Integer numberInstanceActionThreads();

//    List<MailerConfiguration> notificationConfiguration();
//
//    InstanceConfiguration instanceConfiguration();
//
//    SignatureConfiguration signatureConfiguration();
//
//    SecurityGroupServiceClientConfiguration securityGroupServiceClientConfiguration();
}
