package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.ApplicationCredential;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

@Type("ApplicationCredential")
public class ApplicationCredentialType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String name;
    private final @NotNull String applicationId;
    private final @NotNull String applicationSecret;

    public ApplicationCredentialType(ApplicationCredential applicationCredential) {
        this.id = applicationCredential.getId();
        this.name = applicationCredential.getName();
        this.applicationId = applicationCredential.getApplicationId();
        this.applicationSecret = applicationCredential.getApplicationSecret();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public String getApplicationSecret() {
        return applicationSecret;
    }
}
