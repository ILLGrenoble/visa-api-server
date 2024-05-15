package eu.ill.visa.web.graphql.queries.resolvers.fields;

import eu.ill.visa.core.domain.ProtocolStatus;
import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;
import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.InstanceSessionService;
import eu.ill.visa.cloud.domain.CloudInstance;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.entity.*;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import graphql.kickstart.tools.GraphQLResolver;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import static eu.ill.visa.business.services.PortService.isPortOpen;
import static eu.ill.visa.core.entity.enumerations.InstanceMemberRole.OWNER;
import static java.util.Objects.requireNonNullElseGet;
import static java.util.concurrent.CompletableFuture.runAsync;


@ApplicationScoped
public class InstanceResolver implements GraphQLResolver<Instance> {

    private final CloudClientGateway cloudClientGateway;
    private final InstanceSessionService instanceSessionService;

    @Inject
    public InstanceResolver(final CloudClientGateway cloudClientGateway,
                            final InstanceSessionService instanceSessionService) {
        this.cloudClientGateway = cloudClientGateway;
        this.instanceSessionService = instanceSessionService;
    }

    public CompletableFuture<List<ProtocolStatus>> protocols(Instance instance) {
        final CompletableFuture<List<ProtocolStatus>> future = new CompletableFuture<>();

        runAsync(() -> {
            try {
                CloudClient cloudClient = this.cloudClientGateway.getCloudClient(instance.getCloudId());
                if (cloudClient == null) {
                    future.completeExceptionally(new DataFetchingException("Cloud Client with ID " + instance.getCloudId() + " does not exist"));

                } else {
                    final Plan plan = instance.getPlan();
                    final Image image = plan.getImage();
                    final List<ProtocolStatus> results = new ArrayList<>();
                    final List<ImageProtocol> protocols = requireNonNullElseGet(image.getProtocols(), ArrayList::new);
                    if (protocols.size() > 0) {
                        final CloudInstance cloudInstance = cloudClient.instance(instance.getComputeId());
                        for (final ImageProtocol protocol : protocols) {
                            final String address = cloudInstance.getAddress();
                            final int port = protocol.getPort();
                            final boolean active = isPortOpen(address, port);
                            results.add(new ProtocolStatus(protocol, active));
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
    public CompletableFuture<CloudInstance> cloudInstance(Instance instance) {
        final CompletableFuture<CloudInstance> future = new CompletableFuture<>();
        runAsync(() -> {
            try {
                CloudClient cloudClient = this.cloudClientGateway.getCloudClient(instance.getCloudId());
                if (cloudClient == null) {
                    future.completeExceptionally(new DataFetchingException("Cloud Client with ID " + instance.getCloudId() + " does not exist"));

                } else {
                    future.complete(cloudClient.instance(instance.getComputeId()));
                }

            } catch (CloudException exception) {
                future.completeExceptionally(new DataFetchingException(exception.getMessage()));
            }
        });
        return future;
    }

    public User owner(Instance instance) {
        final Optional<InstanceMember> member = instance.getMembers().stream()
            .filter(object -> object.isRole(OWNER))
            .findFirst();
        return member.map(InstanceMember::getUser).orElse(null);
    }

    public List<InstanceSessionMember> activeSessions(final Instance instance) throws DataFetchingException {
        try {
            return instanceSessionService.getAllSessionMembers(instance);
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    public List<InstanceSessionMember> sessions(final Instance instance) throws DataFetchingException {
        try {
            return instanceSessionService.getAllHistorySessionMembers(instance);
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    public CloudClient cloudClient(final Instance instance) {
        return this.cloudClientGateway.getCloudClient(instance.getCloudId());
    }

}

