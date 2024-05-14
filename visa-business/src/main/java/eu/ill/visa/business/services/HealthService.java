package eu.ill.visa.business.services;

import eu.ill.visa.cloud.domain.CloudLimit;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.cloud.services.CloudClientGateway;
import eu.ill.visa.core.domain.HealthReport;
import eu.ill.visa.core.domain.HealthState;
import eu.ill.visa.core.domain.enumerations.HealthStatus;
import eu.ill.visa.core.entity.Image;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;

import java.util.List;

@Transactional
@Singleton
public class HealthService {

    private final CloudClientGateway cloudClientGateway;
    private final ImageService imageService;

    @Inject
    public HealthService(final CloudClientGateway cloudClientGateway,
                         final ImageService imageService) {
        this.cloudClientGateway = cloudClientGateway;
        this.imageService = imageService;
    }

    public HealthReport getHealthReport() {
        HealthState cloudState = this.getCloudHealthStatus();
        HealthState databaseState = this.getDatabaseHealthStatus();

        HealthState globalState;
        if (cloudState.isOk() && databaseState.isOk()) {
            globalState = new HealthState(HealthStatus.OK);

        } else if (cloudState.isOk() && !databaseState.isOk()) {
            globalState = new HealthState(HealthStatus.NOT_OK, "Database connection error");

        } else if (!cloudState.isOk() && databaseState.isOk()) {
            globalState = new HealthState(HealthStatus.NOT_OK, "Cloud connection error");

        } else {
            globalState = new HealthState(HealthStatus.NOT_OK, "Cloud and database connection errors");
        }

        return new HealthReport(globalState, cloudState, databaseState);
    }

    private HealthState getCloudHealthStatus() {
        try {
            List<CloudClient> cloudClients = this.cloudClientGateway.getAll();
            for (CloudClient cloudClient : cloudClients) {
                CloudLimit cloudLimit = cloudClient.limits();
                if (cloudLimit == null) {
                    return new HealthState(HealthStatus.NOT_OK, "Unable to obtain Cloud status from Cloud \"" + cloudClient.getName() + "\"");
                }

            }
            return new HealthState(HealthStatus.OK);

        } catch (CloudException e) {
            return new HealthState(HealthStatus.NOT_OK, e.getMessage());
        }
    }

    private HealthState getDatabaseHealthStatus() {
        try {
            List<Image> images = imageService.getAll();
            if (images == null) {
                return new HealthState(HealthStatus.NOT_OK, "Unable to obtain database status");
            }
            return new HealthState(HealthStatus.OK);

        } catch (Exception e) {
            return new HealthState(HealthStatus.NOT_OK, e.getMessage());
        }
    }

}
