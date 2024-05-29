package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.domain.CloudLimit;

public class DetailedCloudLimit {

    private final CloudClientType cloudClient;
    private final CloudLimit cloudLimit;
    private final String error;

    public DetailedCloudLimit(CloudClientType cloudClient, CloudLimit cloudLimit) {
        this.cloudClient = cloudClient;
        this.cloudLimit = cloudLimit;
        this.error = null;
    }

    public DetailedCloudLimit(CloudClientType cloudClient, String error) {
        this.cloudClient = cloudClient;
        this.cloudLimit = null;
        this.error = error;
    }

    public CloudClientType getCloudClient() {
        return cloudClient;
    }

    public CloudLimit getCloudLimit() {
        return cloudLimit;
    }

    public String getError() {
        return error;
    }
}
