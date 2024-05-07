package eu.ill.visa.web.rest.module;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

public record MetaResponse<T>(T data, MetaData metaData, List<String> errors) {

    @Override
    @JsonProperty("_metadata")
    @JsonInclude(NON_NULL)
    public MetaData metaData() {
        return metaData;
    }

    @JsonInclude(NON_NULL)
    public List<String> errors() {
        return errors;
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
