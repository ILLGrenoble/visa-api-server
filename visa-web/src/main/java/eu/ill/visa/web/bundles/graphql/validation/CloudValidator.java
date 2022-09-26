package eu.ill.visa.web.bundles.graphql.validation;

import com.google.inject.Inject;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientService;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CloudValidator implements ConstraintValidator<Cloud, String> {


    private final CloudClientService cloudClientService;
    private Cloud cloud;

    @Inject
    CloudValidator(final CloudClientService cloudClientService) {
        this.cloudClientService = cloudClientService;
    }

    @Override
    public void initialize(Cloud cloud) {
        this.cloud = cloud;
    }

    @Override
    public boolean isValid(final String identifier, final ConstraintValidatorContext cxt) {
        // TODO CloudClient: select specific cloud client
        CloudClient cloudClient = this.cloudClientService.getDefaultCloudClient();
        try {
            return switch (cloud.type()) {
                case "image" -> cloudClient.image(identifier) != null;
                case "flavour" -> cloudClient.flavour(identifier) != null;
                default -> false;
            };

        } catch (CloudException exception) {
            return false;
        }
    }
}
