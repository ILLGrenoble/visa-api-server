package eu.ill.visa.cloud.providers.openstack.http.responses;

import eu.ill.visa.cloud.domain.CloudImage;

import java.util.List;

public record ImagesResponse(List<CloudImage> images) {
}
