package eu.ill.visa.cloud;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClientGateway;

public class CloudModule extends AbstractModule {
    @Override
    protected void configure() {

    }

    @Provides
    @Singleton
    public CloudClientGateway providesCloudClientGateway(final CloudConfiguration configuration) throws CloudException {
        return new CloudClientGateway(configuration);
    }

}
