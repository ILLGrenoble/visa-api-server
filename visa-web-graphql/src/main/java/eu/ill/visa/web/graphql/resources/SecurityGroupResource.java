package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.CloudClientService;
import eu.ill.visa.business.services.CloudProviderService;
import eu.ill.visa.business.services.SecurityGroupFilterService;
import eu.ill.visa.business.services.SecurityGroupService;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.CloudProviderConfiguration;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.SecurityGroup;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.QueryFilterInput;
import eu.ill.visa.web.graphql.inputs.SecurityGroupInput;
import eu.ill.visa.web.graphql.types.SecurityGroupType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;
import java.util.Optional;

import static eu.ill.visa.web.graphql.inputs.QueryFilterInput.toQueryFilter;
import static java.util.Objects.requireNonNullElseGet;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class SecurityGroupResource {

    private final SecurityGroupService securityGroupService;
    private final SecurityGroupFilterService securityGroupFilterService;
    private final CloudClientService cloudClientService;
    private final CloudProviderService cloudProviderService;

    @Inject
    public SecurityGroupResource(final SecurityGroupService securityGroupService,
                                 final SecurityGroupFilterService securityGroupFilterService,
                                 final CloudClientService cloudClientService,
                                 final CloudProviderService cloudProviderService) {
        this.securityGroupService = securityGroupService;
        this.securityGroupFilterService = securityGroupFilterService;
        this.cloudClientService = cloudClientService;
        this.cloudProviderService = cloudProviderService;
    }

    /**
     * Get a list of securityGroups
     *
     * @return a list of securityGroups
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public @NotNull List<SecurityGroupType> securityGroups(QueryFilterInput filter) throws DataFetchingException {
        try {
            return securityGroupService.getAll(requireNonNullElseGet(toQueryFilter(filter), QueryFilter::new), new OrderBy("name", true)).stream()
                .map(SecurityGroupType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }


    /**
     * Create a new securityGroup
     *
     * @param input the securityGroup name
     * @return the newly created securityGroup
     */
    @Mutation
    public @NotNull SecurityGroupType createSecurityGroup(@NotNull SecurityGroupInput input) throws InvalidInputException {
        // Validate the security group
        this.validateSecurityGroupInput(input);

        Optional<SecurityGroup> existingSecurityGroup = this.securityGroupService.getAll()
            .stream()
            .filter(securityGroup -> {
                if (!input.getName().equals(securityGroup.getName())) {
                    return false;
                }
                Long inputCloudId = input.getCloudId() == -1 ? null :  input.getCloudId();
                if (inputCloudId == null) {
                    return securityGroup.getCloudId() == null;
                } else {
                    return inputCloudId.equals(securityGroup.getCloudId());
                }
            })
            .findFirst();
        if (existingSecurityGroup.isPresent()) {
            return new SecurityGroupType(existingSecurityGroup.get());
        }

        final SecurityGroup securityGroup = new SecurityGroup(input.getName());
        securityGroup.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));
        securityGroupService.save(securityGroup);
        return new SecurityGroupType(securityGroup);
    }

    /**
     * Update a securityGroup
     *
     * @param id    the securityGroup id
     * @param input the securityGroup name
     * @return the updated created securityGroup
     * @throws EntityNotFoundException thrown if the given the securityGroup id was not found
     */
    @Mutation
    public @NotNull SecurityGroupType updateSecurityGroup(@NotNull @AdaptToScalar(Scalar.Int.class) Long id, @NotNull SecurityGroupInput input) throws EntityNotFoundException, InvalidInputException {
        final SecurityGroup securityGroup = this.securityGroupService.getById(id);
        if (securityGroup == null) {
            throw new EntityNotFoundException("SecurityGroup was not found for the given id");
        }

        // Validate the security group
        this.validateSecurityGroupInput(input);

        securityGroup.setName(input.getName());
        securityGroup.setCloudProviderConfiguration(this.getCloudProviderConfiguration(input.getCloudId()));

        securityGroupService.save(securityGroup);
        return new SecurityGroupType(securityGroup);
    }

    /**
     * Delete a security group for a given id
     *
     * @param id the security group id
     * @return the deleted security group
     * @throws EntityNotFoundException thrown if the security group is not found
     */
    @Mutation
    public @NotNull SecurityGroupType deleteSecurityGroup(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException {
        final SecurityGroup securityGroup = securityGroupService.getById(id);
        if (securityGroup == null) {
            throw new EntityNotFoundException("Security group not found for the given id");
        }
        securityGroupFilterService.getAll()
            .stream()
            .filter(filter -> filter.getSecurityGroup().getId().equals(id))
            .forEach(securityGroupFilterService::delete);
        securityGroupService.delete(securityGroup);
        return new SecurityGroupType(securityGroup);
    }

    private void validateSecurityGroupInput(SecurityGroupInput securityGroupInput) throws InvalidInputException {
        try {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(securityGroupInput.getCloudId());
            if (cloudClient == null) {
                throw new InvalidInputException("Invalid cloud Id");
            }

            boolean securityGroupExists = cloudClient.securityGroups().contains(securityGroupInput.getName());
            if (!securityGroupExists) {
                throw new InvalidInputException("Invalid Cloud Security Group");
            }

        } catch (CloudException exception) {
            throw new InvalidInputException("Error accessing Cloud");
        }
    }


    private CloudProviderConfiguration getCloudProviderConfiguration(Long cloudId) {
        if (cloudId != null && cloudId > 0) {
            CloudClient cloudClient = this.cloudClientService.getCloudClient(cloudId);
            return this.cloudProviderService.getById(cloudClient.getId());
        }
        return null;
    }

}
