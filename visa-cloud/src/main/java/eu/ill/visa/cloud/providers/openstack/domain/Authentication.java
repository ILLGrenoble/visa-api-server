package eu.ill.visa.cloud.providers.openstack.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import java.util.ArrayList;
import java.util.List;

@JsonRootName(value = "auth")
public class Authentication {
    private final Identity identity;

    public Authentication(String applicationId, String applicationSecret) {
        this.identity = new Identity(applicationId, applicationSecret);
    }

    public Identity getIdentity() {
        return identity;
    }

    public static final class Identity {
        private final List<String> methods =  new ArrayList<>(List.of("application_credential"));
        private final ApplicationCredential applicationCredential;

        public Identity(String applicationId, String applicationSecret) {
            this.applicationCredential = new ApplicationCredential(applicationId, applicationSecret);
        }

        public List<String> getMethods() {
            return methods;
        }

        @JsonProperty("application_credential")
        public ApplicationCredential getApplicationCredential() {
            return applicationCredential;
        }
    }

    public record ApplicationCredential(String id, String secret) {
    }
}
