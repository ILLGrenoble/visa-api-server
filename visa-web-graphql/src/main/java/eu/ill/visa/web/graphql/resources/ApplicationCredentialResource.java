package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.ApplicationCredentialService;
import eu.ill.visa.core.entity.ApplicationCredential;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.inputs.ApplicationCredentialInput;
import eu.ill.visa.web.graphql.types.ApplicationCredentialDetailType;
import eu.ill.visa.web.graphql.types.ApplicationCredentialType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
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
    public @NotNull List<ApplicationCredentialDetailType> applicationCredentials() {
        return applicationCredentialService.getAll().stream()
            .map(ApplicationCredentialDetailType::new)
            .toList();
    }

    /**
     * Create a new application credential
     *
     * @param input the application credential properties
     * @return the newly created application credential
     */
    @Mutation
    public @NotNull ApplicationCredentialType createApplicationCredential(@NotNull @Valid ApplicationCredentialInput input) {
        final ApplicationCredential applicationCredential = applicationCredentialService.create(input.getName());
        return new ApplicationCredentialType(applicationCredential);
    }

    /**
     * Update an application credential
     *
     * @param input the application credential properties
     * @return the updated application credential
     * @throws EntityNotFoundException thrown if the applicationCredential has not been found
     */
    @Mutation
    public @NotNull ApplicationCredentialDetailType updateApplicationCredential(@NotNull @AdaptToScalar(Scalar.Int.class) Long id, @NotNull @Valid ApplicationCredentialInput input) throws EntityNotFoundException {
        final ApplicationCredential applicationCredential = this.applicationCredentialService.getById(id);
        if (applicationCredential == null) {
            throw new EntityNotFoundException("application credential not found for the given id");
        }
        applicationCredential.setName(input.getName());

        applicationCredentialService.save(applicationCredential);
        return new ApplicationCredentialDetailType(applicationCredential);
    }

    /**
     * Delete an application credential
     *
     * @param id of the application credential
     * @return the deleted application credential
     * @throws EntityNotFoundException thrown if the application credential has not been found
     */
    @Mutation
    public @NotNull ApplicationCredentialDetailType deleteApplicationCredential(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException {
        final ApplicationCredential applicationCredential = applicationCredentialService.getById(id);
        if (applicationCredential == null) {
            throw new EntityNotFoundException("applicationCredential not found for the given id");
        }
        applicationCredentialService.delete(applicationCredential);
        return new ApplicationCredentialDetailType(applicationCredential);
    }

}
