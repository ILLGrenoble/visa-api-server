package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.ApplicationCredentialService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.types.ApplicationCredentialType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class ApplicationCredentialResource {

    private final ApplicationCredentialService applicationCredentialService;

    @Inject
    public ApplicationCredentialResource(final ApplicationCredentialService applicationCredentialService) {
        this.applicationCredentialService = applicationCredentialService;
    }


    @Query
    public List<ApplicationCredentialType> applicationCredentials() throws DataFetchingException {
        try {
            return applicationCredentialService.getAll().stream()
                .map(ApplicationCredentialType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }
}
