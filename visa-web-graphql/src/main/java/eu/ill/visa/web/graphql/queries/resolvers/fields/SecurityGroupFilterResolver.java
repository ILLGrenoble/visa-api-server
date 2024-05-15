package eu.ill.visa.web.graphql.queries.resolvers.fields;

import eu.ill.visa.business.services.FlavourService;
import eu.ill.visa.business.services.InstrumentService;
import eu.ill.visa.business.services.RoleService;
import eu.ill.visa.core.entity.Flavour;
import eu.ill.visa.core.entity.Instrument;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.SecurityGroupFilter;
import graphql.kickstart.tools.GraphQLResolver;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SecurityGroupFilterResolver implements GraphQLResolver<SecurityGroupFilter> {

    private final InstrumentService instrumentService;
    private final RoleService       roleService;
    private final FlavourService    flavourService;

    @Inject()
    public SecurityGroupFilterResolver(final InstrumentService instrumentService,
                                       final RoleService roleService,
                                       final FlavourService flavourService) {
        this.instrumentService = instrumentService;
        this.roleService = roleService;
        this.flavourService = flavourService;
    }

    public String objectName(SecurityGroupFilter securityGroupFilter) {
        final String objectType = securityGroupFilter.getObjectType();
        final Long objectId = securityGroupFilter.getObjectId();
        if ("INSTRUMENT".equals(objectType)) {
            final Instrument instrument = instrumentService.getById(objectId);
            return instrument == null ? null : instrument.getName();

        } else if ("ROLE".equals(objectType)) {
            final Role role = roleService.getById(objectId);
            return role == null ? null : role.getName();

        } else if ("FLAVOUR".equals(objectType)) {
            final Flavour flavour = flavourService.getById(objectId);
            return flavour == null ? null : flavour.getName();
        }
        return null;
    }

}


