package eu.ill.visa.security.suppliers;

import eu.ill.visa.business.services.InstanceService;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.security.tokens.InstanceToken;
import io.quarkus.security.identity.SecurityIdentity;
import io.quarkus.security.runtime.QuarkusSecurityIdentity;
import jakarta.enterprise.context.Dependent;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Dependent
public class InstanceTokenIdentitySupplier {

    private static final Logger logger = LoggerFactory.getLogger(InstanceTokenIdentitySupplier.class);

    private final InstanceService instanceService;

    @Inject
    InstanceTokenIdentitySupplier(final InstanceService instanceService) {
        this.instanceService = instanceService;
    }

    public SecurityIdentity authenticate(final String instanceId, final String instanceComputeId) {
        try {
            if (instanceId != null && instanceComputeId != null) {

                final Long instanceIdNumber = Long.parseLong(instanceId);
                final Instance instance = this.instanceService.getFullById(instanceIdNumber);

                if (instance != null && instance.getComputeId().equals(instanceComputeId)) {
                    logger.debug("Successfully authenticated instance: {} ({})", instance.getName(), instance.getId());

                    InstanceToken instanceToken = new InstanceToken(instance);

                    return QuarkusSecurityIdentity.builder()
                        .setPrincipal(instanceToken)
                        .addRole(Role.INSTANCE_CREDENTIAL_ROLE)
                        .setAnonymous(false)
                        .build();
                }
            }

        } catch (NumberFormatException ignored) {
        }
        return null;
    }
}
