package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.ApplicationCredential;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("ApplicationCredentialDetail")
public class ApplicationCredentialDetailType {

    @AdaptToScalar(Scalar.Int.class)
    private final @NotNull Long id;
    private final @NotNull String name;
    private final @NotNull String applicationId;
    private final Date lastUsedAt;

    public ApplicationCredentialDetailType(ApplicationCredential applicationCredential) {
        this.id = applicationCredential.getId();
        this.name = applicationCredential.getName();
        this.applicationId = applicationCredential.getApplicationId();
        this.lastUsedAt = applicationCredential.getLastUsedAt();
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

    public Date getLastUsedAt() {
        return lastUsedAt;
    }
}
