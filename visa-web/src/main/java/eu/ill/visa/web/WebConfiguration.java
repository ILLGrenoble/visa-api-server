package eu.ill.visa.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import eu.ill.visa.business.BusinessConfiguration;
import eu.ill.visa.cloud.CloudConfiguration;
import eu.ill.visa.scheduler.SchedulerConfiguration;
import eu.ill.visa.security.SecurityConfiguration;
import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import eu.ill.visa.web.bundles.graphql.GraphQLWebServletConfiguration;
import eu.ill.visa.web.bundles.swagger.SwaggerBundleConfiguration;
import io.dropwizard.Configuration;
import io.dropwizard.db.DataSourceFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class WebConfiguration extends Configuration {

    @Valid
    @NotNull
    private final DataSourceFactory dataSourceFactory = new DataSourceFactory();


    @Valid
    @NotNull
    private SecurityConfiguration securityConfiguration;

    @NotNull
    @Valid
    private SwaggerBundleConfiguration swaggerBundleConfiguration;

    @NotNull
    @Valid
    private CloudConfiguration cloudConfiguration;

    @NotNull
    @Valid
    private VirtualDesktopConfiguration virtualDesktopConfiguration;

    @Valid
    @NotNull
    private SchedulerConfiguration schedulerConfiguration;

    @Valid
    @NotNull
    private BusinessConfiguration businessConfiguration;

    @NotNull
    @Valid
    private GraphQLWebServletConfiguration graphQLWebServletConfiguration;

    @NotNull
    @Valid
    private ClientConfiguration clientConfiguration;

    @NotNull
    private String corsOrigin;

    @JsonProperty("corsOrigin")
    public String getCorsOrigin() {
        return this.corsOrigin;
    }

    @JsonProperty("database")
    public DataSourceFactory getDataSourceFactory() {
        return this.dataSourceFactory;
    }

    @JsonProperty("swagger")
    public SwaggerBundleConfiguration getSwaggerBundleConfiguration() {
        return this.swaggerBundleConfiguration;
    }

    @JsonProperty("cloud")
    public CloudConfiguration getCloudConfiguration() {
        return this.cloudConfiguration;
    }

    @JsonProperty("security")
    public SecurityConfiguration getSecurityConfiguration() {
        return securityConfiguration;
    }

    @JsonProperty("vdi")
    public VirtualDesktopConfiguration getVirtualDesktopConfiguration() {
        return virtualDesktopConfiguration;
    }

    @JsonProperty("scheduler")
    public SchedulerConfiguration getSchedulerConfiguration() {
        return schedulerConfiguration;
    }

    @JsonProperty("business")
    public BusinessConfiguration getBusinessConfiguration() {
        return businessConfiguration;
    }

    @JsonProperty("graphql")
    public GraphQLWebServletConfiguration getGraphQLWebServletConfiguration() {
        return graphQLWebServletConfiguration;
    }

    @JsonProperty("client")
    public ClientConfiguration getClientConfiguration() {
        return clientConfiguration;
    }

}
