package eu.ill.visa.web.graphql.queries.resolvers.fields;

import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.visa.cloud.providers.openstack.OpenStackProvider;
import eu.ill.visa.cloud.providers.openstack.OpenStackProviderConfiguration;
import eu.ill.visa.cloud.providers.web.WebProvider;
import eu.ill.visa.cloud.providers.web.WebProviderConfiguration;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientFactory;
import graphql.kickstart.tools.GraphQLResolver;


@ApplicationScoped
public class CloudClientResolver implements GraphQLResolver<CloudClient> {

    public OpenStackProviderConfiguration openStackProviderConfiguration(CloudClient cloudClient) {
        if (cloudClient.getType().equals(CloudClientFactory.OPENSTACK)) {
            try {
                OpenStackProvider openStackProvider = (OpenStackProvider)cloudClient.getProvider();
                if (openStackProvider != null) {
                    return openStackProvider.getConfiguration();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    public WebProviderConfiguration webProviderConfiguration(CloudClient cloudClient) {
        if (cloudClient.getType().equals(CloudClientFactory.WEB)) {
            try {
                WebProvider webProvider = (WebProvider)cloudClient.getProvider();
                if (webProvider != null) {
                    return webProvider.getConfiguration();
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

}
