package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.RoleService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.types.RoleType;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

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
    public List<RoleType> rolesAndGroups() {
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
    public List<RoleType> roles() {
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
    public List<RoleType> groups() {
        return this.roleService.getAllGroups().stream()
            .map(RoleType::new)
            .toList();
    }

}
