package eu.ill.visa.web.graphql.relay;

import java.util.List;

import static java.util.Objects.requireNonNull;

public class Connection<T> {

    private final PageInfo pageInfo;
    private final List<T> data;

    public Connection(final PageInfo pageInfo, final List<T> data) {
        this.pageInfo = requireNonNull(pageInfo, "pageInfo cannot be null");
        this.data = requireNonNull(data, "data cannot be null");
    }

    public PageInfo getPageInfo() {
        return pageInfo;
    }

    public List<T> getData() {
        return data;
    }
}
