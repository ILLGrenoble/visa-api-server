package eu.ill.visa.web.bundles.graphql.validation;

import com.google.inject.Inject;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class CloudValidator implements ConstraintValidator<Cloud, String> {


    private final CloudClient client;
    private Cloud cloud;

    @Inject
    CloudValidator(CloudClient client) {
        this.client = client;
    }

    @Override
    public void initialize(Cloud cloud) {
        this.cloud = cloud;
    }

    @Override
    public boolean isValid(final String identifier, final ConstraintValidatorContext cxt) {
        try {
            return switch (cloud.type()) {
                case "image" -> client.image(identifier) != null;
                case "flavour" -> client.flavour(identifier) != null;
                default -> false;
            };

        } catch (CloudException exception) {
            return false;
        }
    }
}
