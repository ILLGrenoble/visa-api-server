package eu.ill.visa.web.rest.module;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record MetaResponse<T>(T data, MetaData metaData, List<String> errors) {

    @Override
    @JsonProperty("_metadata")
    public MetaData metaData() {
        return metaData;
    }

    public static class MetaData {
        private Long count;
        private Integer page;
        private Integer limit;

        public MetaData count(Long count) {
            this.count = count;
            return this;
        }

        public MetaData page(Integer page) {
            this.page = page;
            return this;
        }

        public MetaData limit(Integer limit) {
            this.limit = limit;
            return this;
        }

        public Long getCount() {
            return count;
        }

        public Integer getPage() {
            return page;
        }

        public Integer getLimit() {
            return limit;
        }
    }
}
