package eu.ill.visa.web.graphql.types;

import eu.ill.visa.core.entity.ApplicationCredential;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import org.eclipse.microprofile.graphql.Type;

import java.util.Date;

@Type("ApplicationCredential")
public class ApplicationCredentialType {

    @AdaptToScalar(Scalar.Int.class)
    private final Long id;
    private final String name;
    private final String applicationId;
    private final Date lastUsedAt;

    public ApplicationCredentialType(ApplicationCredential applicationCredential) {
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
