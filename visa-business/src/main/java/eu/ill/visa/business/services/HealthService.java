package eu.ill.visa.business.services;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import eu.ill.visa.cloud.domain.CloudLimit;
import eu.ill.visa.cloud.exceptions.CloudException;
import eu.ill.visa.cloud.services.CloudClient;
import eu.ill.visa.core.domain.HealthReport;
import eu.ill.visa.core.domain.Image;
import eu.ill.visa.core.domain.enumerations.HealthStatus;

import java.util.List;

@Transactional
@Singleton
public class HealthService {

    private final CloudClient cloudClient;
    private final ImageService imageService;

    @Inject
    public HealthService(final CloudClient cloudClient,
                         final ImageService imageService) {
        this.cloudClient = cloudClient;
        this.imageService = imageService;
    }

    public HealthReport getHealthReport() {
        HealthStatus cloudStatus = this.getCloudHealthStatus();
        HealthStatus databaseStatus = this.getDatabaseHealthStatus();

        HealthStatus globalStatus = cloudStatus.equals(HealthStatus.OK) && databaseStatus.equals(HealthStatus.OK) ? HealthStatus.OK : HealthStatus.NOT_OK;

        return new HealthReport(globalStatus, cloudStatus, databaseStatus);
    }

    private HealthStatus getCloudHealthStatus() {
        try {
            CloudLimit cloudLimit = cloudClient.limits();
            return cloudLimit == null ? HealthStatus.NOT_OK : HealthStatus.OK;
        } catch (CloudException e) {
            return HealthStatus.NOT_OK;
        }
    }

    private HealthStatus getDatabaseHealthStatus() {
        try {
            List<Image> images = imageService.getAll();
            return images == null ? HealthStatus.NOT_OK : HealthStatus.OK;
        } catch (Exception e) {
            return HealthStatus.NOT_OK;
        }
    }

}
