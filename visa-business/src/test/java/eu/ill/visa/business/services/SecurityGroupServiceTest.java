package eu.ill.visa.business.services;

import com.google.inject.Inject;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.SecurityGroup;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(BusinessExtension.class)
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
        final List<SecurityGroup> securityGroups = securityGroupService.getDefaultSecurityGroups();
        assertEquals(1, securityGroups.size());
    }

    @Test
    @DisplayName("It should get specific securityGroups for an instance owned by admin")
    void testSecurityGroupsForInstanceWithAdminOwner() {
        Instance instance = instanceService.getById(1000L);

        final List<SecurityGroup> securityGroups = securityGroupService.getAllForInstance(instance);
        assertEquals(7, securityGroups.size());
    }

    @Test
    @DisplayName("It should get specific securityGroups for an instance owned by staff")
    void testSecurityGroupsForInstanceWithStaffOwner() {
        Instance instance = instanceService.getById(1006L);

        final List<SecurityGroup> securityGroups = securityGroupService.getAllForInstance(instance);
        assertEquals(2, securityGroups.size());
    }

}
