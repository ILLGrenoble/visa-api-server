package eu.ill.visa.cloud.providers.openstack.http.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.*;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ServerInput {
    public String name;
    public @JsonProperty("imageRef") String imageId;
    public @JsonProperty("flavorRef") String flavorId;
    public @JsonProperty("networks") List<ServerAddressUUID> networks;
    public @JsonProperty("security_groups") List<ServerSecurityGroup> securityGroups;
    public Map<String, String> metadata;
    public @JsonProperty("user_data") String userDataBase64;

    public static ServerInputBuilder Builder() {
        return new ServerInputBuilder();
    }

    public static record ServerSecurityGroup(String name) {
    }

    public static record ServerAddressUUID(String uuid) {
    }

    public static class ServerInputBuilder {
        private final static Base64.Encoder encoder = Base64.getEncoder();

        private String name;
        private String imageId;
        private String flavorId;
        private List<String> networks = new ArrayList<>();
        private List<String> securityGroups = new ArrayList<>();
        private Map<String, String> metadata = new HashMap<>();
        private String userData;

        public ServerInputBuilder name(final String name) {
            this.name = name;
            return this;
        }

        public ServerInputBuilder imageId(final String imageId) {
            this.imageId = imageId;
            return this;
        }

        public ServerInputBuilder flavorId(final String flavorId) {
            this.flavorId = flavorId;
            return this;
        }

        public ServerInputBuilder networks(final List<String> networks) {
            this.networks = networks;
            return this;
        }

        public ServerInputBuilder securityGroups(final List<String> securityGroups) {
            this.securityGroups = securityGroups;
            return this;
        }

        public ServerInputBuilder metadata(final Map<String, String> metadata) {
            this.metadata = metadata;
            return this;
        }

        public ServerInputBuilder userData(final String userData) {
            this.userData = userData;
            return this;
        }

        public ServerInput build() {
            final ServerInput serverInput = new ServerInput();
            serverInput.name = this.name;
            serverInput.flavorId = this.flavorId;
            serverInput.imageId = this.imageId;
            serverInput.networks = this.networks.stream().map(ServerAddressUUID::new).toList();
            serverInput.securityGroups = this.securityGroups.stream().map(ServerSecurityGroup::new).toList();
            serverInput.metadata = this.metadata;
            serverInput.userDataBase64 = this.userData == null ? null : encoder.encodeToString(this.userData.getBytes());

            return serverInput;
        }
    }

}
