package eu.ill.visa.core.domain;

public class Pagination {

    private int limit = Integer.MAX_VALUE;
    private int offset;

    public Pagination() {
    }

    public Pagination(int offset) {
        this.offset = offset;
    }

    public Pagination(int limit, int offset) {
        this.limit = limit;
        this.offset = offset;
    }

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

    public boolean isLimitBetween(int min, int max) {
        return limit >= min && limit <= max;
    }

}
