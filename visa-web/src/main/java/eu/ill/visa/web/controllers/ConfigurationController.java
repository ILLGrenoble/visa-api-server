package eu.ill.visa.web.controllers;

import com.google.inject.Inject;
import eu.ill.visa.business.services.ConfigurationService;
import eu.ill.visa.core.domain.Configuration;
import eu.ill.visa.web.ClientConfiguration;
import eu.ill.visa.web.dtos.ConfigurationDto;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.FileReader;
import java.util.Map;
import java.util.stream.Collectors;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;


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
    public Response getConfiguration() {
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
        configurationDto.setContactEmail(this.clientConfiguration.getContactEmail());
        configurationDto.setLogin(this.clientConfiguration.getLoginConfiguration());
        configurationDto.setAnalytics(this.clientConfiguration.getAnalyticsConfiguration());
        configurationDto.setDesktop(this.clientConfiguration.getDesktopConfiguration());
        configurationDto.setExperiments(this.clientConfiguration.getExperimentsConfiguration());
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
