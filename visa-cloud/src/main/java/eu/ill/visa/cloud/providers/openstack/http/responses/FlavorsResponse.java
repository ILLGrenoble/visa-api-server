package eu.ill.visa.cloud.providers.openstack.http.responses;

import eu.ill.visa.cloud.domain.CloudFlavour;

import java.util.List;

public record FlavorsResponse(List<CloudFlavour> flavors) {
}
