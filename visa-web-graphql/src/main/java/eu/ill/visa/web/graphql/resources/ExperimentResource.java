package eu.ill.visa.web.graphql.resources;

import eu.ill.visa.business.services.ExperimentService;
import eu.ill.visa.core.domain.filters.ExperimentFilter;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.inputs.OrderByInput;
import eu.ill.visa.web.graphql.inputs.PaginationInput;
import eu.ill.visa.web.graphql.types.Connection;
import eu.ill.visa.web.graphql.types.ExperimentType;
import eu.ill.visa.web.graphql.types.PageInfo;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import java.util.List;

import static eu.ill.visa.web.graphql.inputs.OrderByInput.toOrderBy;
import static eu.ill.visa.web.graphql.inputs.PaginationInput.toPagination;
import static java.lang.String.format;
import static java.util.Objects.requireNonNullElseGet;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class ExperimentResource {

    private final ExperimentService experimentService;

    @Inject
    public ExperimentResource(final ExperimentService experimentService) {
        this.experimentService = experimentService;
    }

    /**
     * Get a list of experiments
     *
     * @param filter     the given query filter
     * @param orderBy    the ordering of results
     * @param pagination the pagination (limit and offset)
     * @return a list of experiments
     * @throws DataFetchingException thrown if there was an error fetching the results
     */
    @Query
    public @NotNull Connection<ExperimentType> experiments(final ExperimentFilter filter, final OrderByInput orderBy, @NotNull final PaginationInput pagination) throws DataFetchingException {
        if (!pagination.isLimitBetween(0, 50)) {
            throw new DataFetchingException(format("Limit must be between %d and %d", 0, 200));
        }
        final List<ExperimentType> results = experimentService.getAll(
            requireNonNullElseGet(filter, ExperimentFilter::new),
            toPagination(pagination),
            requireNonNullElseGet(toOrderBy(orderBy), () -> new OrderBy("id", true))
        ).stream()
            .map(ExperimentType::new)
            .toList();
        final PageInfo pageInfo = new PageInfo(experimentService.countAll(requireNonNullElseGet(filter, ExperimentFilter::new)), pagination.getLimit(), pagination.getOffset());
        return new Connection<>(pageInfo, results);
    }

    /**
     * Count all experiments
     *
     * @param filter a filter to filter the results
     * @return a count of experiments
     */
    @Query
    public @NotNull @AdaptToScalar(Scalar.Int.class) Long countExperiments(final ExperimentFilter filter) {
        return experimentService.countAll(requireNonNullElseGet(filter, ExperimentFilter::new));
    }

}
