package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.cloud.providers.openstack.OpenStackProvider;
import eu.ill.visa.cloud.providers.web.WebProvider;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientFactory;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.OpenStackProviderConfigurationType;
import eu.ill.visa.web.graphql.types.WebProviderConfigurationType;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

@GraphQLApi
public class CloudClientResolver {

    private final CloudClientGateway cloudClientGateway;

    public CloudClientResolver(final CloudClientGateway cloudClientGateway) {
        this.cloudClientGateway = cloudClientGateway;
    }

    public OpenStackProviderConfigurationType openStackProviderConfiguration(@Source CloudClientType cloudClientType) {
        final CloudClient cloudClient = cloudClientGateway.getCloudClient(cloudClientType.getId());

        if (cloudClient.getType().equals(CloudClientFactory.OPENSTACK)) {
            try {
                OpenStackProvider openStackProvider = (OpenStackProvider)cloudClient.getProvider();
                if (openStackProvider != null) {
                    return new OpenStackProviderConfigurationType(openStackProvider.getConfiguration());
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public WebProviderConfigurationType webProviderConfiguration(@Source CloudClientType cloudClientType) {
        final CloudClient cloudClient = cloudClientGateway.getCloudClient(cloudClientType.getId());
        if (cloudClient.getType().equals(CloudClientFactory.WEB)) {
            try {
                WebProvider webProvider = (WebProvider)cloudClient.getProvider();
                if (webProvider != null) {
                    return new WebProviderConfigurationType(webProvider.getConfiguration());
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
