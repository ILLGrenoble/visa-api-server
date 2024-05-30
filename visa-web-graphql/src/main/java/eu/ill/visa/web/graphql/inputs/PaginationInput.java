package eu.ill.visa.web.graphql.inputs;

import eu.ill.visa.core.domain.Pagination;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Input;

@Input("Pagination")
public class PaginationInput {
    private Integer limit = Integer.MAX_VALUE;
    private @NotNull Integer offset;

    public Integer getLimit() {
        return limit;
    }

    public void setLimit(Integer limit) {
        this.limit = limit;
    }

    public Integer getOffset() {
        return offset;
    }

    public void setOffset(Integer offset) {
        this.offset = offset;
    }

    public Boolean isLimitBetween(Integer min, Integer max) {
        return limit >= min && limit <= max;
    }

    public static Pagination toPagination(final PaginationInput input) {
        if (input == null) {
            return null;
        }
        return new Pagination(input.limit, input.offset);
    }
}
