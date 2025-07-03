package eu.ill.visa.web.graphql.resolvers;

import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.cloud.providers.openstack.OpenStackProvider;
import eu.ill.visa.cloud.providers.web.WebProvider;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientFactory;
import eu.ill.visa.web.graphql.types.CloudClientType;
import eu.ill.visa.web.graphql.types.OpenStackProviderConfigurationType;
import eu.ill.visa.web.graphql.types.WebProviderConfigurationType;
import io.quarkus.runtime.annotations.RegisterForReflection;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

@RegisterForReflection
@GraphQLApi
public class CloudClientResolver {

    private final CloudClientService cloudClientService;

    public CloudClientResolver(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
    }

    public OpenStackProviderConfigurationType openStackProviderConfiguration(@Source CloudClientType cloudClientType) {
        final CloudClient cloudClient = cloudClientService.getCloudClient(cloudClientType.getId());

        if (cloudClient.getType().equals(CloudClientFactory.OPENSTACK)) {
            try {
                OpenStackProvider openStackProvider = (OpenStackProvider)cloudClient.getProvider();
                if (openStackProvider != null) {
                    return new OpenStackProviderConfigurationType(openStackProvider.getOpenStackConfiguration());
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public WebProviderConfigurationType webProviderConfiguration(@Source CloudClientType cloudClientType) {
        final CloudClient cloudClient = cloudClientService.getCloudClient(cloudClientType.getId());
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
