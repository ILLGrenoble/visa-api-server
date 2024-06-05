package eu.ill.visa.cloud.providers.openstack.endpoints;

import eu.ill.visa.cloud.domain.CloudImage;
import eu.ill.visa.cloud.exceptions.CloudException;

import java.util.List;

public interface ImageEndpoint {

    List<CloudImage> images() throws CloudException;
    CloudImage image(final String id) throws CloudException;
}
