package eu.ill.visa.cloud.providers;

import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;

import java.util.List;

public class NullProvider implements CloudProvider {

    @Override
    public List<CloudImage> images() {
        return null;
    }

    @Override
    public CloudImage image(String id) {
        return null;
    }

    @Override
    public List<CloudFlavour> flavors() {
        return null;
    }

    @Override
    public CloudFlavour flavor(String id) {
        return null;
    }

    @Override
    public List<CloudInstanceIdentifier> instanceIdentifiers() {
        return null;
    }

    @Override
    public List<CloudInstance> instances() {
        return null;
    }

    @Override
    public CloudInstance instance(String id) {
        return null;
    }

    @Override
    public String ip(String id) {
        return null;
    }

    @Override
    public void rebootInstance(String id) {

    }

    @Override
    public void shutdownInstance(String id) {

    }

    @Override
    public void startInstance(String id) {

    }

    @Override
    public void updateSecurityGroups(String id, List<String> securityGroupNames) {

    }

    @Override
    public CloudInstance createInstance(String name,
                                        String imageId,
                                        String flavorId,
                                        List<String> securityGroupNames,
                                        CloudInstanceMetadata metadata,
                                        String bootCommand) {
        return null;
    }

    @Override
    public void deleteInstance(String id) {

    }

    @Override
    public CloudLimit limits() throws CloudException {
        return null;
    }

    @Override
    public List<String> securityGroups() throws CloudException {
        return null;
    }

}
