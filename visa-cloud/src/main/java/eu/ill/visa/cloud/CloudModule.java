package eu.ill.visa.cloud;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientFactory;

public class CloudModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    public CloudClient providesCloudClient(final CloudConfiguration configuration) throws CloudException {
        final CloudClientFactory factory = new CloudClientFactory();
        return factory.getClient(configuration);
    }

}
