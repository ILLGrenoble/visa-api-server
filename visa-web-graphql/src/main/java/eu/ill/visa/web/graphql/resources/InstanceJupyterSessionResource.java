package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.InstanceJupyterSessionService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.inputs.PaginationInput;
import eu.ill.visa.web.graphql.types.Connection;
import eu.ill.visa.web.graphql.types.InstanceJupyterSessionType;
import eu.ill.visa.web.graphql.types.PageInfo;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

import static eu.ill.visa.web.graphql.inputs.PaginationInput.toPagination;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class InstanceJupyterSessionResource {

    private final InstanceJupyterSessionService instanceJupyterSessionService;

    @Inject
    public InstanceJupyterSessionResource(final InstanceJupyterSessionService instanceJupyterSessionService) {
        this.instanceJupyterSessionService = instanceJupyterSessionService;
    }


    /**
     * Get a list of jupyter sessions
     *
     * @param pagination the pagination (limit and offset)
     * @return a list of jupyter sessions
     */
    @Query
    public @NotNull Connection<InstanceJupyterSessionType> jupyterSessions(@NotNull PaginationInput pagination) {
        final List<InstanceJupyterSessionType> results = instanceJupyterSessionService.getAll(toPagination(pagination)).stream()
            .map(InstanceJupyterSessionType::new)
            .toList();
        final PageInfo pageInfo = new PageInfo(instanceJupyterSessionService.countAll(), pagination.getLimit(), pagination.getOffset());
        return new Connection<>(pageInfo, results);
    }

    /**
     * Count all active jupyter sessions
     *
     * @return a count of active Jupyter sessions
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countJupyterSessions() {
        return instanceJupyterSessionService.countAllInstances();
    }


}
