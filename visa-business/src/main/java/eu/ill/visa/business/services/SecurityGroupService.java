package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.business.http.SecurityGroupServiceClient;
import eu.ill.visa.core.domain.*;
import eu.ill.visa.persistence.repositories.SecurityGroupRepository;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    public List<SecurityGroup> getAll(Pagination pagination) {
        return this.repository.getAll(pagination);
    }

    public Long countAll() {
        return repository.countAll();
    }

    public List<SecurityGroup> getAllForInstance(final Instance instance) {
        User owner = instance.getOwner().getUser();

        // Get security groups from security-groups web service
        List<SecurityGroup> customSecurityGroups = this.securityGroupServiceClient.getSecurityGroups(instance);

        if (owner.hasRole(Role.ADMIN_ROLE)) {
            List<SecurityGroup> allSecurityGroups = this.getAll();

            Set<SecurityGroup> uniqueSecurityGroups = new LinkedHashSet<>(allSecurityGroups);
            uniqueSecurityGroups.addAll(customSecurityGroups);

            return new ArrayList<>(uniqueSecurityGroups);

        } else {
            List<SecurityGroup> generalSecurityGroups = this.getDefaultSecurityGroups();
            List<SecurityGroup> roleBasedSecurityGroups = this.getRoleBasedSecurityGroups(owner);

            Set<SecurityGroup> uniqueSecurityGroups = new LinkedHashSet<>(generalSecurityGroups);
            uniqueSecurityGroups.addAll(roleBasedSecurityGroups);
            uniqueSecurityGroups.addAll(customSecurityGroups);

            return new ArrayList<>(uniqueSecurityGroups);
        }

    }

    public List<SecurityGroup> getDefaultSecurityGroups() {
        return repository.getDefaultSecurityGroups();
    }

    public List<SecurityGroup> getRoleBasedSecurityGroups(final User user) {
        return repository.getRoleBasedSecurityGroups(user);
    }
}
