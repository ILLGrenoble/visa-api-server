package eu.ill.visa.business.services;

import eu.ill.visa.business.SecurityGroupServiceClientConfiguration;
import eu.ill.visa.business.http.SecurityGroupServiceClient;
import eu.ill.visa.core.domain.OrderBy;
import eu.ill.visa.core.domain.QueryFilter;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.persistence.repositories.SecurityGroupRepository;
import io.quarkus.rest.client.reactive.QuarkusRestClientBuilder;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Transactional
@Singleton
public class SecurityGroupService {
    private static final Logger logger = LoggerFactory.getLogger(SecurityGroupService.class);

    private final SecurityGroupRepository repository;
    private final SecurityGroupServiceClientConfiguration securityGroupServiceClientConfiguration;

    private final SecurityGroupServiceClient securityGroupServiceClient;

    @Inject
    public SecurityGroupService(final SecurityGroupRepository repository,
                                final SecurityGroupServiceClientConfiguration securityGroupServiceClientConfiguration) {
        this.repository = repository;
        this.securityGroupServiceClientConfiguration = securityGroupServiceClientConfiguration;

        if (this.securityGroupServiceClientConfiguration.enabled() && this.securityGroupServiceClientConfiguration.url().isPresent()) {
            this.securityGroupServiceClient = QuarkusRestClientBuilder.newBuilder()
                .baseUri(URI.create(securityGroupServiceClientConfiguration.url().get()))
                .build(SecurityGroupServiceClient.class);

        } else {
            this.securityGroupServiceClient = null;
        }

    }

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
        return this.getAll().stream()
            .filter(securityGroup -> securityGroup.hasSameCloudClientId(cloudClientId))
            .toList();
    }

    public List<String> getAllSecurityGroupNamesForInstance(final Instance instance) {
        User owner = instance.getOwner().getUser();

        Long cloudClientId = instance.getCloudId();

        // Get security groups from security-groups web service
        List<String> customSecurityGroups = this.getCustomSecurityGroups(instance);

        if (owner.hasRole(Role.ADMIN_ROLE)) {
            List<String> allSecurityGroups = this.getAllForCloudClient(cloudClientId).stream()
                .map(SecurityGroup::getName)
                .toList();

            Set<String> uniqueSecurityGroups = new LinkedHashSet<>(allSecurityGroups);
            uniqueSecurityGroups.addAll(customSecurityGroups);

            return new ArrayList<>(uniqueSecurityGroups);

        } else {
            List<String> generalSecurityGroups = this.getDefaultSecurityGroups(cloudClientId).stream()
                .map(SecurityGroup::getName)
                .toList();
            List<String> roleBasedSecurityGroups = this.getRoleBasedSecurityGroups(owner, cloudClientId).stream()
                .map(SecurityGroup::getName)
                .toList();
            List<String> flavourBasedSecurityGroups = this.getFlavourBasedSecurityGroups(instance.getPlan().getFlavour(), cloudClientId).stream()
                .map(SecurityGroup::getName)
                .toList();

            Set<String> uniqueSecurityGroups = new LinkedHashSet<>(generalSecurityGroups);
            uniqueSecurityGroups.addAll(roleBasedSecurityGroups);
            uniqueSecurityGroups.addAll(flavourBasedSecurityGroups);
            uniqueSecurityGroups.addAll(customSecurityGroups);

            return new ArrayList<>(uniqueSecurityGroups);
        }

    }

    private List<String> getCustomSecurityGroups(final Instance instance) {
        List<String> securityGroupNames = new ArrayList<>();

        if (this.securityGroupServiceClient != null) {
            try {
                logger.info("Requesting security groups for instance {} from {} ...", instance.getId(), this.securityGroupServiceClientConfiguration.url().get());
                List<SecurityGroup> securityGroups = this.securityGroupServiceClient.getSecurityGroups(instance);
                securityGroupNames = securityGroups.stream().map(SecurityGroup::getName).toList();
                logger.info("... got security groups [{}] for instance {}", String.join(", ", securityGroupNames), instance.getId());

            } catch (Exception e) {
                logger.error("Caught exception getting security groups for instance {}: {} ", instance.getId(), e.getMessage());
            }
        }
        return securityGroupNames;
    }

    public List<SecurityGroup> getAll(QueryFilter filter, OrderBy orderBy) {
        return repository.getAll(filter, orderBy);
    }

    public List<SecurityGroup> getDefaultSecurityGroups(Long cloudClientId) {
        return repository.getDefaultSecurityGroups().stream()
            .filter(securityGroup -> securityGroup.hasSameCloudClientId(cloudClientId))
            .toList();
    }

    public List<SecurityGroup> getFlavourBasedSecurityGroups(final Flavour flavour, Long cloudClientId) {
        return repository.getFlavourBasedSecurityGroups(flavour).stream()
            .filter(securityGroup -> securityGroup.hasSameCloudClientId(cloudClientId))
            .toList();
    }

    public List<SecurityGroup> getRoleBasedSecurityGroups(final User user, Long cloudClientId) {
        return repository.getRoleBasedSecurityGroups(user).stream()
            .filter(securityGroup -> securityGroup.hasSameCloudClientId(cloudClientId))
            .toList();
    }
}
