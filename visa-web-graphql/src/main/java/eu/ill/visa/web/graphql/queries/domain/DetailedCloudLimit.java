package eu.ill.visa.web.graphql.queries.domain;

import eu.ill.visa.cloud.domain.CloudLimit;
import eu.ill.visa.cloud.services.CloudClient;

public class DetailedCloudLimit {

    private CloudClient cloudClient;
    private CloudLimit cloudLimit;
    private String error;


    public DetailedCloudLimit(CloudClient cloudClient, CloudLimit cloudLimit) {
        this.cloudClient = cloudClient;
        this.cloudLimit = cloudLimit;
    }

    public DetailedCloudLimit(CloudClient cloudClient, String error) {
        this.cloudClient = cloudClient;
        this.error = error;
    }

    public CloudClient getCloudClient() {
        return cloudClient;
    }

    public void setCloudClient(CloudClient cloudClient) {
        this.cloudClient = cloudClient;
    }

    public CloudLimit getCloudLimit() {
        return cloudLimit;
    }

    public void setCloudLimit(CloudLimit cloudLimit) {
        this.cloudLimit = cloudLimit;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }
}
