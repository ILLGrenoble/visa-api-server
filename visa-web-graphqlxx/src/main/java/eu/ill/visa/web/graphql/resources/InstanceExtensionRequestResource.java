package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.InstanceExtensionRequestService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.types.InstanceExtensionRequestType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class InstanceExtensionRequestResource {

    private final InstanceExtensionRequestService instanceExtensionRequestService;

    @Inject
    public InstanceExtensionRequestResource(final InstanceExtensionRequestService instanceExtensionRequestService) {
        this.instanceExtensionRequestService = instanceExtensionRequestService;
    }

    @Query
    public @NotNull List<InstanceExtensionRequestType> instanceExtensionRequests() throws DataFetchingException {
        try {
            return this.instanceExtensionRequestService.getAll().stream()
                .map(InstanceExtensionRequestType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }
}
