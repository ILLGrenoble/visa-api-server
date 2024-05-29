package eu.ill.visa.web.graphql.inputs;

import eu.ill.visa.core.domain.Pagination;
import org.eclipse.microprofile.graphql.Input;

@Input("Pagination")
public class PaginationInput {
    private int limit;
    private int offset;

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Pagination toPagination() {
        return new Pagination(this.limit, this.offset);
    }

    public boolean isLimitBetween(int min, int max) {
        return limit >= min && limit <= max;
    }
}
