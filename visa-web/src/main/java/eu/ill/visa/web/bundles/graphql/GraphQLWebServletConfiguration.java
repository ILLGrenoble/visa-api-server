package eu.ill.visa.web.bundles.graphql;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class GraphQLWebServletConfiguration {

    private Boolean tracing;

    private List<String> files;

    private int resultsLimit;

    @JsonProperty
    @NotNull
    @Valid
    public Boolean getTracing() {
        return this.tracing;
    }

    @JsonProperty
    @NotNull
    @Valid
    public List<String> getFiles() {
        return this.files;
    }

    @JsonProperty
    @NotNull
    @Valid
    public Integer getResultsLimit() {
        return this.resultsLimit;
    }
}
