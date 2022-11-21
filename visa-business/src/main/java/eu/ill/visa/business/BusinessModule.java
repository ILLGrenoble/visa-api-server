package eu.ill.visa.business;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.ill.visa.business.http.SecurityGroupServiceClient;
import eu.ill.visa.business.services.NotificationFactory;
import eu.ill.visa.business.services.NotificationService;
import eu.ill.visa.business.services.SignatureService;

public class BusinessModule extends AbstractModule {

    @Override
    protected void configure() {

    }

    @Provides
    public NotificationService providesNotificationService(final BusinessConfiguration configuration) {
        final NotificationFactory factory = new NotificationFactory();
        return factory.getNotificationService(
            configuration.getNotificationConfiguration(),
            configuration.getInstanceConfiguration(),
            configuration.getRootURL()
        );
    }

    @Provides
    public SecurityGroupServiceClient providesSecurityGroupServiceClient(final BusinessConfiguration configuration) {
        return new SecurityGroupServiceClient(configuration.getSecurityGroupServiceClientConfiguration());
    }


    @Provides
    public SignatureConfiguration providesSignatureConfiguration(final BusinessConfiguration configuration) {
        return configuration.getSignatureConfiguration();
    }

    @Provides
    public InstanceConfiguration providesInstanceConfiguration(final BusinessConfiguration configuration) {
        return configuration.getInstanceConfiguration();
    }

    @Provides
    public SignatureService providesSignatureService(final SignatureConfiguration configuration) {
        return new SignatureService(
            configuration.getPrivateKeyPath(),
            configuration.getPublicKeyPath()
        );
    }
}
