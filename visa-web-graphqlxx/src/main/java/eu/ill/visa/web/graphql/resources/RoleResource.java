package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.RoleService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.InvalidInputException;
import eu.ill.visa.web.graphql.inputs.RoleInput;
import eu.ill.visa.web.graphql.types.RoleType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.util.Date;
import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class RoleResource {

    private final RoleService roleService;

    @Inject
    public RoleResource(final RoleService roleService) {
        this.roleService = roleService;
    }


    /**
     * Get a list of all roles and groups
     *
     * @return all the roles and groups
     */
    @Query
    public @NotNull List<RoleType> rolesAndGroups() {
        return this.roleService.getAllRolesAndGroups().stream()
            .map(RoleType::new)
            .toList();
    }

    /**
     * Get a list of all roles
     *
     * @return all the roles
     */
    @Query
    public @NotNull List<RoleType> roles() {
        return this.roleService.getAllRoles().stream()
            .map(RoleType::new)
            .toList();
    }

    /**
     * Get a list of all groups
     *
     * @return all the groups
     */
    @Query
    public @NotNull List<RoleType> groups() {
        return this.roleService.getAllGroups().stream()
            .map(RoleType::new)
            .toList();
    }

    @Mutation
    public @NotNull RoleType createRole(@NotNull RoleInput input) throws InvalidInputException {
        final Role existingRole = this.roleService.getByName(input.getName());
        if (existingRole != null) {
            throw new InvalidInputException("Role with given name already exists");
        }

        Role role = new Role(input.getName(), input.getDescription());
        role.setGroupCreatedAt(new Date());

        this.roleService.save(role);

        return new RoleType(role);
    }

    @Mutation
    public @NotNull RoleType updateRole(@NotNull @AdaptToScalar(Scalar.Int.class) Long roleId, @NotNull RoleInput input) throws EntityNotFoundException, InvalidInputException {
        final Role existingRoleWithId = this.roleService.getById(roleId);
        if (existingRoleWithId == null) {
            throw new EntityNotFoundException("Role with given Id does not exist");
        }

        final Role existingRoleWithName = this.roleService.getByName(input.getName());
        if (existingRoleWithName != null && !existingRoleWithName.getId().equals(roleId)) {
            throw new InvalidInputException("Role with given name already exists");
        }
        existingRoleWithId.setName(input.getName());
        existingRoleWithId.setDescription(input.getDescription());

        this.roleService.save(existingRoleWithId);

        return new RoleType(existingRoleWithId);
    }

    @Mutation
    public @NotNull Boolean deleteRole(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) throws EntityNotFoundException {
        final Role existingRoleWithId = this.roleService.getById(id);
        if (existingRoleWithId == null) {
            throw new EntityNotFoundException("Role with given Id does not exist");
        }

        existingRoleWithId.setGroupDeletedAt(new Date());
        this.roleService.save(existingRoleWithId);

        return true;
    }

}
