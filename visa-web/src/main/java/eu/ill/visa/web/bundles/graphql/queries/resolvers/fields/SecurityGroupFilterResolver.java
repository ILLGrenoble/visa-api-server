package eu.ill.visa.web.bundles.graphql.queries.resolvers.fields;

import eu.ill.visa.business.services.InstrumentService;
import eu.ill.visa.business.services.RoleService;
import eu.ill.visa.core.domain.Instrument;
import eu.ill.visa.core.domain.Role;
import eu.ill.visa.core.domain.SecurityGroupFilter;
import graphql.kickstart.tools.GraphQLResolver;

import javax.inject.Inject;

public class SecurityGroupFilterResolver implements GraphQLResolver<SecurityGroupFilter> {

    private final InstrumentService instrumentService;
    private final RoleService       roleService;

    @Inject()
    public SecurityGroupFilterResolver(final InstrumentService instrumentService,
                                       final RoleService roleService) {
        this.instrumentService = instrumentService;
        this.roleService = roleService;
    }

    String objectName(SecurityGroupFilter securityGroupFilter) {
        final String objectType = securityGroupFilter.getObjectType();
        final Long objectId = securityGroupFilter.getObjectId();
        if ("INSTRUMENT".equals(objectType)) {
            final Instrument instrument = instrumentService.getById(objectId);
            return instrument == null ? null : instrument.getName();
        } else if ("ROLE".equals(objectType)) {
            final Role role = roleService.getById(objectId);
            return role == null ? null : role.getName();
        }
        return null;
    }

}


