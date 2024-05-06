package eu.ill.visa.web.rest.controllers;

import eu.ill.visa.business.services.ConfigurationService;
import eu.ill.visa.core.entity.Configuration;
import eu.ill.visa.web.rest.ClientConfiguration;
import eu.ill.visa.web.rest.dtos.ConfigurationDto;
import eu.ill.visa.web.rest.module.MetaResponse;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.util.Map;
import java.util.stream.Collectors;

import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;


@Path("/configuration")
@Produces(APPLICATION_JSON)
@Consumes(APPLICATION_JSON)
public class ConfigurationController extends AbstractController {

    private final Logger logger = LoggerFactory.getLogger(ConfigurationController.class);

    private final ClientConfiguration clientConfiguration;
    private final ConfigurationService configurationService;

    @Inject
    ConfigurationController(final ClientConfiguration clientConfiguration,
                            final ConfigurationService configurationService) {
        this.clientConfiguration = clientConfiguration;
        this.configurationService = configurationService;
    }

    @GET
    public MetaResponse<ConfigurationDto> getConfiguration() {
        ConfigurationDto configurationDto = new ConfigurationDto();
        String version = getClass().getPackage().getImplementationVersion();
        if (version == null) {
            version = this.getVersionFromPom();
        }

        Map<String, String> metadata = this.configurationService.getAll().stream()
            .collect(Collectors.toMap(
                Configuration::getKey,
                Configuration::getValue
            ));

        configurationDto.setVersion(version);
        configurationDto.setContactEmail(this.clientConfiguration.contactEmail().orElse(null));
        configurationDto.setLogin(this.clientConfiguration.loginConfiguration());
        configurationDto.setAnalytics(this.clientConfiguration.analyticsConfiguration());
        configurationDto.setDesktop(this.clientConfiguration.desktopConfiguration());
        configurationDto.setExperiments(this.clientConfiguration.experimentsConfiguration());
        configurationDto.setMetadata(metadata);
        return createResponse(configurationDto);
    }

    private String getVersionFromPom() {
        MavenXpp3Reader reader = new MavenXpp3Reader();
        try {
            Model model = reader.read(new FileReader("pom.xml"));
            return model.getVersion();

        } catch (Exception e) {
            logger.warn("Got an exception while trying to get application version: {}", e.getMessage());
        }

        return null;
    }

}
