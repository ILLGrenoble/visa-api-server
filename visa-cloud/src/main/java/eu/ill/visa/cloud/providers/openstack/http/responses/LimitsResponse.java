package eu.ill.visa.cloud.providers.openstack.http.responses;

import eu.ill.visa.cloud.domain.CloudLimit;

public record LimitsResponse(AbsoluteLimitsResponse limits) {
    public record AbsoluteLimitsResponse(CloudLimit absolute) {
    }
}
