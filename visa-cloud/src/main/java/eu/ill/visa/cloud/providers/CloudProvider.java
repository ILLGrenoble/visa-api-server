package eu.ill.visa.cloud.providers;


import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;

import java.util.List;

public interface CloudProvider {

    List<CloudImage> images() throws CloudException;

    CloudImage image(String id) throws CloudException;

    List<CloudFlavour> flavors() throws CloudException;

    CloudFlavour flavor(String id) throws CloudException;

    List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException;

    List<CloudInstance> instances() throws CloudException;

    CloudInstance instance(String id) throws CloudException;

    String ip(final String id) throws CloudException;

    void rebootInstance(String id) throws CloudException;

    void shutdownInstance(String id) throws CloudException;

    void startInstance(String id) throws CloudException;

    void updateSecurityGroups(String id, List<String> securityGroupNames) throws CloudException;

    CloudInstance createInstance(String name,
                                 String imageId,
                                 String flavorId,
                                 List<String> securityGroupNames,
                                 CloudInstanceMetadata metadata,
                                 String bootCommand) throws CloudException;

    void deleteInstance(String id) throws CloudException;

    CloudLimit limits() throws CloudException;
}
