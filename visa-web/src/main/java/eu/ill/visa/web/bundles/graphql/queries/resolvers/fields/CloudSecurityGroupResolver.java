package eu.ill.visa.web.bundles.graphql.queries.resolvers.fields;

import eu.ill.visa.business.services.SecurityGroupService;
import eu.ill.visa.core.domain.SecurityGroup;
import eu.ill.visa.web.bundles.graphql.queries.domain.CloudSecurityGroup;
import graphql.kickstart.tools.GraphQLResolver;

import javax.inject.Inject;

public class CloudSecurityGroupResolver implements GraphQLResolver<CloudSecurityGroup> {


    private final SecurityGroupService securityGroupService;

    @Inject()
    public CloudSecurityGroupResolver(final SecurityGroupService securityGroupService) {
        this.securityGroupService = securityGroupService;
    }

    /**
     * Check if this security group has been integrated into the platform
     */
    SecurityGroup integration(CloudSecurityGroup cloudSecurityGroup) {
        return this.securityGroupService.getByName(cloudSecurityGroup.getName());
    }

}


