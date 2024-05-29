package eu.ill.visa.web.graphql.resolvers;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.ExperimentService;
import eu.ill.visa.business.services.InstanceMemberService;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.entity.InstanceMember;
import eu.ill.visa.persistence.repositories.InstanceAttributeRepository;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.types.*;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Source;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static eu.ill.visa.business.services.PortService.isPortOpen;
import static eu.ill.visa.core.entity.enumerations.InstanceMemberRole.OWNER;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.concurrent.CompletableFuture.runAsync;


@GraphQLApi
public class InstanceResolver {

    private final CloudClientGateway cloudClientGateway;
    private final InstanceSessionService instanceSessionService;
    private final InstanceMemberService instanceMemberService;
    private final ExperimentService experimentService;
    private final InstanceAttributeRepository instanceAttributeRepository;

    @Inject
    public InstanceResolver(final CloudClientGateway cloudClientGateway,
                            final InstanceSessionService instanceSessionService,
                            final InstanceMemberService instanceMemberService,
                            final ExperimentService experimentService,
                            final InstanceAttributeRepository instanceAttributeRepository) {
        this.cloudClientGateway = cloudClientGateway;
        this.instanceSessionService = instanceSessionService;
        this.instanceMemberService = instanceMemberService;
        this.experimentService = experimentService;
        this.instanceAttributeRepository = instanceAttributeRepository;
    }

    public List<InstanceMemberType> members(@Source InstanceType instance) {
        return this.instanceMemberService.getAllByInstanceId(instance.getId()).stream()
            .map(InstanceMemberType::new)
            .toList();
    }

    public List<ExperimentType> experiments(@Source InstanceType instance) {
        return this.experimentService.getAllForInstanceId(instance.getId()).stream()
            .map(ExperimentType::new)
            .toList();
    }

    public List<InstanceAttributeType> attributes(@Source InstanceType instance) {
        return this.instanceAttributeRepository.getAllForInstanceId(instance.getId()).stream()
            .map(InstanceAttributeType::new)
            .toList();
    }

    public CompletableFuture<List<ProtocolStatusType>> protocols(@Source InstanceType instance) {
        final CompletableFuture<List<ProtocolStatusType>> future = new CompletableFuture<>();

        runAsync(() -> {
            try {
                CloudClient cloudClient = this.cloudClientGateway.getCloudClient(instance.getCloudId());
                if (cloudClient == null) {
                    future.completeExceptionally(new DataFetchingException("Cloud Client with ID " + instance.getCloudId() + " does not exist"));

                } else {
                    final PlanType plan = instance.getPlan();
                    final ImageType image = plan.getImage();
                    final List<ProtocolStatusType> results = new ArrayList<>();
                    final List<ImageProtocolType> protocols = requireNonNullElseGet(image.getProtocols(), ArrayList::new);
                    if (!protocols.isEmpty()) {
                        final CloudInstance cloudInstance = cloudClient.instance(instance.getComputeId());
                        for (final ImageProtocolType protocol : protocols) {
                            final String address = cloudInstance.getAddress();
                            final int port = protocol.getPort();
                            final boolean active = isPortOpen(address, port);
                            results.add(new ProtocolStatusType(protocol, active));
                        }
                        future.complete(results);
                    }
                    future.complete(results);
                }

            } catch (CloudException exception) {
                future.completeExceptionally(new DataFetchingException(exception.getMessage()));
            }
        });
        return future;
    }

    /**
     * Get cloud instance from the the cloud provider
     *
     * @return a list of cloud images
     */
    public CompletableFuture<CloudInstanceType> cloudInstance(@Source InstanceType instance) {
        final CompletableFuture<CloudInstanceType> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                CloudClient cloudClient = this.cloudClientGateway.getCloudClient(instance.getCloudId());
                if (cloudClient == null) {
                    future.completeExceptionally(new DataFetchingException("Cloud Client with ID " + instance.getCloudId() + " does not exist"));

                } else {
                    CloudInstance cloudInstance = cloudClient.instance(instance.getComputeId());
                    if (cloudInstance != null) {
                        future.complete(new CloudInstanceType(cloudInstance));
                    } else {
                        future.completeExceptionally(new DataFetchingException("Cloud Instance with compute ID " + instance.getComputeId() + " does not exist"));
                    }
                }

            } catch (CloudException exception) {
                future.completeExceptionally(new DataFetchingException(exception.getMessage()));
            }
        });
        return future;
    }

    public UserType owner(@Source final InstanceType instance) {
        return this.instanceMemberService.getAllByInstanceIdAndRole(instance.getId(), OWNER).stream()
            .findFirst()
            .map(InstanceMember::getUser)
            .map(UserType::new)
            .orElse(null);
    }

    public List<InstanceSessionMemberType> activeSessions(@Source final InstanceType instance) throws DataFetchingException {
        try {
            return instanceSessionService.getAllSessionMembersByInstanceId(instance.getId()).stream()
                .map(InstanceSessionMemberType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    public List<InstanceSessionMemberType> sessions(@Source final InstanceType instance) throws DataFetchingException {
        try {
            return instanceSessionService.getAllHistorySessionMembersByInstanceId(instance.getId()).stream()
                .map(InstanceSessionMemberType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    public @NotNull CloudClientType cloudClient(@Source final InstanceType instance) {
        CloudClient cloudClient = this.cloudClientGateway.getCloudClient(instance.getCloudId());
        if (cloudClient != null) {
            return new CloudClientType(cloudClient);
        }
        return null;
    }

}

