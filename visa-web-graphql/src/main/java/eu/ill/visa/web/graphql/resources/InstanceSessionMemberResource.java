package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.InstanceSessionMemberService;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.inputs.PaginationInput;
import eu.ill.visa.web.graphql.types.Connection;
import eu.ill.visa.web.graphql.types.InstanceSessionMemberType;
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
public class InstanceSessionMemberResource {

    private final InstanceSessionMemberService instanceSessionMemberService;

    @Inject
    public InstanceSessionMemberResource(final InstanceSessionMemberService instanceSessionMemberService) {
        this.instanceSessionMemberService = instanceSessionMemberService;
    }


    /**
     * Get a list of sessions
     *
     * @param pagination the pagination (limit and offset)
     * @return a list of sessions
     */
    @Query
    public @NotNull Connection<InstanceSessionMemberType> sessions(@NotNull PaginationInput pagination) {
        final List<InstanceSessionMemberType> results = instanceSessionMemberService.getAll(toPagination(pagination)).stream()
            .map(InstanceSessionMemberType::new)
            .toList();
        final PageInfo pageInfo = new PageInfo(instanceSessionMemberService.countAll(), pagination.getLimit(), pagination.getOffset());
        return new Connection<>(pageInfo, results);
    }

    /**
     * Count all sessions
     *
     * @return a count of sessions
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countSessions() {
        return instanceSessionMemberService.countAll();
    }

    /**
     * Count all active sessions (interaction within the last 5 minutes)
     *
     * @return a count of active sessions
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countActiveSessions() {
        return instanceSessionMemberService.countAllActive();
    }

}
