package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.business.http.SecurityGroupServiceClient;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.repositories.SecurityGroupRepository;

import javax.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;

@Transactional
@Singleton
public class SecurityGroupService {

    @Inject
    private SecurityGroupRepository repository;

    @Inject
    private SecurityGroupServiceClient securityGroupServiceClient;

    public SecurityGroup getById(Long id) {
        return this.repository.getById(id);
    }

    public void delete(SecurityGroup securityGroup) {
        this.repository.delete(securityGroup);
    }

    public void save(@NotNull SecurityGroup securityGroup) {
        this.repository.save(securityGroup);
    }

    public List<SecurityGroup> getAll() {
        return this.repository.getAll();
    }

    public List<SecurityGroup> getAllForCloudClient(Long cloudClientId) {
        List<SecurityGroup> securityGroups = this.getAll()
            .stream()
            .filter(securityGroup -> securityGroup.hasSameCloudClientId(cloudClientId))
            .collect(Collectors.toList());

        return securityGroups;
    }

    public List<String> getAllSecurityGroupNamesForInstance(final Instance instance) {
        User owner = instance.getOwner().getUser();

        Long cloudClientId = instance.getCloudId();

        // Get security groups from security-groups web service
        List<String> customSecurityGroups = this.securityGroupServiceClient.getSecurityGroups(instance);

        if (owner.hasRole(Role.ADMIN_ROLE)) {
            List<String> allSecurityGroups = this.getAllForCloudClient(cloudClientId)
                .stream()
                .map(SecurityGroup::getName)
                .collect(Collectors.toList());

            Set<String> uniqueSecurityGroups = new LinkedHashSet<>(allSecurityGroups);
            uniqueSecurityGroups.addAll(customSecurityGroups);

            return new ArrayList<>(uniqueSecurityGroups);

        } else {
            List<String> generalSecurityGroups = this.getDefaultSecurityGroups(cloudClientId)
                .stream()
                .map(SecurityGroup::getName)
                .collect(Collectors.toList());
            List<String> roleBasedSecurityGroups = this.getRoleBasedSecurityGroups(owner, cloudClientId)
                .stream()
                .map(SecurityGroup::getName)
                .collect(Collectors.toList());
            List<String> flavourBasedSecurityGroups = this.getFlavourBasedSecurityGroups(instance.getPlan().getFlavour(), cloudClientId)
                .stream()
                .map(SecurityGroup::getName)
                .collect(Collectors.toList());

            Set<String> uniqueSecurityGroups = new LinkedHashSet<>(generalSecurityGroups);
            uniqueSecurityGroups.addAll(roleBasedSecurityGroups);
            uniqueSecurityGroups.addAll(flavourBasedSecurityGroups);
            uniqueSecurityGroups.addAll(customSecurityGroups);

            return new ArrayList<>(uniqueSecurityGroups);
        }

    }

    public List<SecurityGroup> getAll(QueryFilter filter, OrderBy orderBy) {
        return repository.getAll(filter, orderBy);
    }

    public List<SecurityGroup> getDefaultSecurityGroups(Long cloudClientId) {
        List<SecurityGroup> securityGroups = repository.getDefaultSecurityGroups()
            .stream()
            .filter(securityGroup -> securityGroup.hasSameCloudClientId(cloudClientId))
            .collect(Collectors.toList());

        return securityGroups;
    }

    public List<SecurityGroup> getFlavourBasedSecurityGroups(final Flavour flavour, Long cloudClientId) {
        List<SecurityGroup> securityGroups = repository.getFlavourBasedSecurityGroups(flavour)
            .stream()
            .filter(securityGroup -> securityGroup.hasSameCloudClientId(cloudClientId))
            .collect(Collectors.toList());

        return securityGroups;
    }

    public List<SecurityGroup> getRoleBasedSecurityGroups(final User user, Long cloudClientId) {
        List<SecurityGroup> securityGroups = repository.getRoleBasedSecurityGroups(user)
            .stream()
            .filter(securityGroup -> securityGroup.hasSameCloudClientId(cloudClientId))
            .collect(Collectors.toList());

        return securityGroups;
    }
}
