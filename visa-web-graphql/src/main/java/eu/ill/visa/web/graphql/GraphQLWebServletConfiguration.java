package eu.ill.visa.web.graphql;

import io.smallrye.config.ConfigMapping;

import java.util.List;

@ConfigMapping(prefix = "graphql", namingStrategy = ConfigMapping.NamingStrategy.VERBATIM)
public interface GraphQLWebServletConfiguration {

    Boolean tracing();

    List<String> files();

    int resultsLimit();
}
