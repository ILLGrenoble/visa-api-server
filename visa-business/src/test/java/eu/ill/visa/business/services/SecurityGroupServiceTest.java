package eu.ill.visa.business.services;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.SecurityGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
@TestTransaction
public class SecurityGroupServiceTest {

    @Inject
    private SecurityGroupService securityGroupService;

    @Inject
    private InstrumentService instrumentService;

    @Inject
    private ExperimentService experimentService;

    @Inject
    private InstanceService instanceService;

    @Test
    @DisplayName("It should get all admin securityGroups")
    void testGetAllSecurityGroups() {
        final List<SecurityGroup> securityGroups = securityGroupService.getAll();
        assertEquals(7, securityGroups.size());
    }

    @Test
    @DisplayName("It should get all general securityGroups")
    void testGetDefaultSecurityGroups() {
        final List<SecurityGroup> securityGroups = securityGroupService.getDefaultSecurityGroups(null);
        assertEquals(1, securityGroups.size());
    }

    @Test
    @DisplayName("It should get specific securityGroups for an instance owned by admin")
    void testSecurityGroupsForInstanceWithAdminOwner() {
        Instance instance = instanceService.getById(1000L);

        final List<String> securityGroupsNames = securityGroupService.getAllSecurityGroupNamesForInstance(instance);
        assertEquals(7, securityGroupsNames.size());
    }

    @Test
    @DisplayName("It should get specific securityGroups for an instance owned by staff")
    void testSecurityGroupsForInstanceWithStaffOwner() {
        Instance instance = instanceService.getById(1006L);

        final List<String> securityGroupsNames = securityGroupService.getAllSecurityGroupNamesForInstance(instance);
        assertEquals(2, securityGroupsNames.size());
    }

}
