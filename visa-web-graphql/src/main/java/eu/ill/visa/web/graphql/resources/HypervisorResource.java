package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.HypervisorService;
import eu.ill.visa.core.entity.Hypervisor;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.types.HypervisorType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class HypervisorResource {

    private final HypervisorService hypervisorService;

    @Inject
    public HypervisorResource(final HypervisorService hypervisorService) {
        this.hypervisorService = hypervisorService;
    }

    /**
     * Determine if hypervisor data available
     *
     * @return true if hypervisor data is available
     */
    @Query
    public @NotNull boolean hypervisorsAvailable() {
        return this.hypervisorService.countAll() > 0;
    }

    /**
     * Get a list of hypervisors
     *
     * @return a list of hypervisors
     */
    @Query
    public @NotNull List<HypervisorType> hypervisors() {
        return this.hypervisorService.getAll().stream()
            .map(HypervisorType::new)
            .toList();
    }

    /**
     * Get a list of hypervisors
     *
     * @return a list of hypervisors
     */
    @Query
    public HypervisorType hypervisor(@NotNull @AdaptToScalar(Scalar.Int.class) Long id) {
        final Hypervisor hypervisor = this.hypervisorService.getById(id);
        return hypervisor == null ? null : new HypervisorType(hypervisor);
    }

}
