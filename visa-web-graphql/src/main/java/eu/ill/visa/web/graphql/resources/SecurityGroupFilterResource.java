package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.*;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.SecurityGroup;
import eu.ill.visa.core.entity.SecurityGroupFilter;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.SecurityGroupFilterInput;
import eu.ill.visa.web.graphql.types.SecurityGroupFilterType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.Arrays;
import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class SecurityGroupFilterResource {

    private final SecurityGroupFilterService securityGroupFilterService;
    private final SecurityGroupService securityGroupService;
    private final FlavourService flavourService;
    private final InstrumentService instrumentService;
    private final RoleService roleService;

    @Inject
    public SecurityGroupFilterResource(final SecurityGroupFilterService securityGroupFilterService,
                                       final SecurityGroupService securityGroupService,
                                       final FlavourService flavourService,
                                       final InstrumentService instrumentService,
                                       final RoleService roleService) {
        this.securityGroupFilterService = securityGroupFilterService;
        this.securityGroupService = securityGroupService;
        this.flavourService = flavourService;
        this.instrumentService = instrumentService;
        this.roleService = roleService;
    }


    /**
     * Get a list of securityGroupFilters
     *
     * @return a list of security group filters
     */
    @Query
    public @NotNull List<SecurityGroupFilterType> securityGroupFilters() {
        return securityGroupFilterService.getAll().stream()
            .map(SecurityGroupFilterType::new)
            .toList();
    }

    /**
     * Get a specific security group filter
     *
     * @return a security group filter
     */
    @Query
    public SecurityGroupFilterType securityGroupFilter(@NotNull @AdaptToScalar(Scalar.Int.class) Long securityGroupId, @NotNull @AdaptToScalar(Scalar.Int.class) Long objectId, @NotNull String objectType) {
        SecurityGroupFilter filter = securityGroupFilterService.getSecurityGroupFilterBySecurityIdAndObjectIdAndType(securityGroupId, objectId, objectType);
        return filter == null ? null : new SecurityGroupFilterType(filter);
    }

    /**
     * Create a new securityGroupFilter
     *
     * @param input the securityGroupFilter properties
     * @return the newly created securityGroupFilter
     */
    @Mutation
    public @NotNull SecurityGroupFilterType createSecurityGroupFilter(@NotNull @Valid SecurityGroupFilterInput input) throws EntityNotFoundException, InvalidInputException {
        if (securityGroupFilterService.getSecurityGroupFilterBySecurityIdAndObjectIdAndType(input.getSecurityGroupId(), input.getObjectId(), input.getObjectType()) == null) {

            // Validate the input data
            SecurityGroup securityGroup = this.validateSecurityGroupFilterInput(input);

            final SecurityGroupFilter securityGroupFilter = new SecurityGroupFilter(securityGroup, input.getObjectId(), input.getObjectType());
            securityGroupFilterService.save(securityGroupFilter);
            return new SecurityGroupFilterType(securityGroupFilter);
        }
        throw new InvalidInputException("A security group filter for the given object id and type already exists");
    }

    /**
     * Update a securityGroupFilter
     *
     * @param id    the securityGroupFilter id
     * @param input the securityGroupFilter properties
     * @return the updated created securityGroupFilter
     * @throws EntityNotFoundException thrown if the given the securityGroupFilter id was not found
     */
    @Mutation
    public @NotNull SecurityGroupFilterType updateSecurityGroupFilter(@NotNull @AdaptToScalar(Scalar.Int.class) Long id, @NotNull @Valid SecurityGroupFilterInput input) throws EntityNotFoundException, InvalidInputException {
        final SecurityGroupFilter securityGroupFilter = this.securityGroupFilterService.getById(id);
        if (securityGroupFilter == null) {
            throw new EntityNotFoundException("SecurityGroupFilter was not found for the given id");
        }

        // Validate the input data
        SecurityGroup securityGroup = this.validateSecurityGroupFilterInput(input);

        securityGroupFilter.setSecurityGroup(securityGroup);
        securityGroupFilter.setObjectId(input.getObjectId());
        securityGroupFilter.setObjectType(input.getObjectType());
        securityGroupFilterService.save(securityGroupFilter);
        return new SecurityGroupFilterType(securityGroupFilter);
    }

    /**
     * Delete a securityGroupFilter for a given id
     *
     * @param id the securityGroupFilter id
     * @return the deleted securityGroupFilter
     * @throws EntityNotFoundException thrown if the securityGroupFilter is not found
     */
    @Mutation
    public @NotNull SecurityGroupFilterType deleteSecurityGroupFilter(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException {
        final SecurityGroupFilter securityGroupFilter = securityGroupFilterService.getById(id);
        if (securityGroupFilter == null) {
            throw new EntityNotFoundException("SecurityGroupFilter not found for the given id");
        }
        securityGroupFilterService.delete(securityGroupFilter);
        return new SecurityGroupFilterType(securityGroupFilter);
    }

    private SecurityGroup validateSecurityGroupFilterInput(SecurityGroupFilterInput input) throws EntityNotFoundException, InvalidInputException {

        final SecurityGroup securityGroup = this.securityGroupService.getById(input.getSecurityGroupId());
        if (securityGroup == null) {
            throw new EntityNotFoundException("SecurityGroup not found for the given id");
        }

        String objectType = input.getObjectType();
        final String[] validObjectTypes = {"INSTRUMENT", "ROLE", "FLAVOUR"};
        if (!Arrays.asList(validObjectTypes).contains(objectType)) {
            throw new InvalidInputException("ObjectType is not valid for SecurityGroupFilter");
        }

        // Check objectIds are valid
        if (objectType.equals("INSTRUMENT") && instrumentService.getById(input.getObjectId()) == null) {
            throw new EntityNotFoundException("Instrument not found for the given id");

        } else if (objectType.equals("ROLE") && roleService.getById(input.getObjectId()) == null) {
            throw new EntityNotFoundException("Role not found for the given id");

        } else if (objectType.equals("FLAVOUR") && flavourService.getById(input.getObjectId()) == null) {
            throw new EntityNotFoundException("Flavour not found for the given id");
        }

        return securityGroup;
    }

}
