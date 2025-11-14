package eu.ill.visa.cloud.providers;

import eu.ill.visa.cloud.domain.*;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.exceptions.CloudUnavailableException;

import java.util.List;

public interface CloudProvider {

    /**
     * Get a list of all images
     *
     * @return a list of images
     * @throws CloudException if there was an exception executing the request
     */
    List<CloudImage> images() throws CloudException;

    /**
     * Get an image for a given id
     *
     * @param id the image id
     * @return the image, otherwise null if not found
     * @throws CloudException if there was an exception executing the request
     */
    CloudImage image(String id) throws CloudException;

    /**
     * Get a list of all flavours
     *
     * @return a list of flavours
     * @throws CloudException if there was an exception executing the request
     */
    List<CloudFlavour> flavors() throws CloudException;

    /**
     * Get a flavour for a given id
     *
     * @param id the flavour id
     * @return a list of flavours
     * @throws CloudException if there was an exception executing the request
     */
    CloudFlavour flavor(String id) throws CloudException;

    /**
     * Get a list of all devices
     *
     * @return a list of devices
     * @throws CloudException if there was an exception executing the request
     */
    List<CloudDevice> devices() throws CloudException;

    /**
     * Get a list of all flavour device allocations
     * @return a list of flavour device allocations
     * @throws CloudException if there was an exception executing the request
     */
    List<CloudDeviceAllocation> deviceAllocations() throws CloudException;

    /**
     * Get a device for a given identifier and device type
     *
     * @param identifier the device identifier
     * @param deviceType the device type
     * @return a device
     * @throws CloudException if there was an exception executing the request
     */
    CloudDevice device(String identifier, CloudDevice.Type deviceType) throws CloudException;

    /**
     * Get a list of device allocations for the flavour
     *
     * @param flavourId the flavour id
     * @return a list of device allocations for the flavour
     * @throws CloudException if there was an exception executing the request
     */
    List<CloudDeviceAllocation> flavorDeviceAllocations(String flavourId) throws CloudException;

    /**
     * Get a list of instance identifiers
     *
     * @return a list of instance identifiers
     * @throws CloudException if there was an exception executing the request
     */
    List<CloudInstanceIdentifier> instanceIdentifiers() throws CloudException;

    /**
     * Get a list of instances
     *
     * @return a list of instances
     * @throws CloudException if there was an exception executing the request
     */
    List<CloudInstance> instances() throws CloudException;

    /**
     * Get an instance for a given id
     *
     * @param id the instance id
     * @return the instance, otherwise null
     * @throws CloudException if there was an exception executing the request
     */
    CloudInstance instance(String id) throws CloudException;

    /**
     * Get the ip address for a given instance
     *
     * @param id the instance identifier
     * @return the ip address, otherwise null
     * @throws CloudException if there was an exception executing the request
     */
    String ip(String id) throws CloudException;

    /**
     * Reboot an instance for a given instance identifier
     *
     * @param id the instance identifier
     * @throws CloudException if there was an exception executing the request
     */
    void rebootInstance(String id) throws CloudException;

    /**
     * Shutdown an instance for a given instance identifier
     *
     * @param id the instance identifier
     * @throws CloudException if there was an exception executing the request
     */
    void shutdownInstance(String id) throws CloudException;

    /**
     * Start an instance for a given instance identifier
     *
     * @param id the instance identifier
     * @throws CloudException if there was an exception executing the request
     */
    void startInstance(String id) throws CloudException;

    /**
     * Update the security groups for a given instance identifier
     *
     * @param id                 the instance identifier
     * @param securityGroupNames the list of security group names
     * @throws CloudException if there was an exception executing the request
     */
    void updateSecurityGroups(String id, List<String> securityGroupNames) throws CloudException;

    /**
     * Create a new instance
     *
     * @param name               the instance name
     * @param imageId            the cloud image identifier
     * @param flavorId           the cloud flavor identifier
     * @param securityGroupNames a list of security group names
     * @param metadata           the metadata to send to the instance
     * @param bootCommand        the command to execute when the instance is booted
     * @return a new cloud instance
     * @throws CloudException if there was an error executing the request
     */
    CloudInstance createInstance(String name,
                                 String imageId,
                                 String flavorId,
                                 List<String> securityGroupNames,
                                 CloudInstanceMetadata metadata,
                                 String bootCommand) throws CloudException;

    /**
     * Delete an instance for a given instance identifier
     *
     * @param id the instance identifier
     * @throws CloudException if there was an exception executing the request
     */
    void deleteInstance(String id) throws CloudException;

    /**
     * Retrieve the limits from the cloud provider (vCPUS used, memory used etc.)
     *
     * @return the cloud limits
     * @throws CloudException if there was an exception executing the request
     */
    CloudLimit limits() throws CloudException;

    List<String> securityGroups() throws CloudException;

    /**
     * Returns a list of custom resource classes
     * @return a list of resource classes
     * @throws CloudException if there was an exception executing the request
     * @throws CloudUnavailableException if the functionality is unavailable
     */
    List<String> resourceClasses() throws CloudException, CloudUnavailableException;

    /**
     * Returns a list of cloud hypervisor inventories (available resources)
     * @return a list of cloud hypervisor inventories
     * @throws CloudException if there was an exception executing the request
     * @throws CloudUnavailableException if the functionality is unavailable
     */
    List<CloudHypervisorInventory> hypervisorInventories() throws CloudException, CloudUnavailableException;

    /**
     * Returns a list of cloud hypervisor usages (resource usage)
     * @return a list of cloud hypervisor usages
     * @throws CloudException if there was an exception executing the request
     * @throws CloudUnavailableException if the functionality is unavailable
     */
    List<CloudHypervisorUsage> hypervisorUsages() throws CloudException, CloudUnavailableException;

    /**
     * Returns a list of cloud hypervisor allocations (server computer Ids)
     * @return a list of cloud hypervisor allocations
     * @throws CloudException if there was an exception executing the request
     * @throws CloudUnavailableException if the functionality is unavailable
     */
    List<CloudHypervisorAllocation> hypervisorAllocations() throws CloudException, CloudUnavailableException;

}
