package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.Instance;
import eu.ill.visa.core.entity.enumerations.InstanceState;

import java.util.Date;
import java.util.List;

public class InstanceDto {

    private final Long id;
    private final String uid;
    private final String computeId;
    private final String ipAddress;
    private final String name;
    private final String comments;
    private final PlanDto plan;
    private final InstanceState state;
    private final Integer screenWidth;
    private final Integer screenHeight;
    private final List<InstanceMemberDto> members;
    private final List<ExperimentDto> experiments;
    private InstanceMemberDto membership;
    private final Date lastSeenAt;
    private final Date lastInteractionAt;
    private final Date terminationDate;
    private final Date expirationDate;
    private final boolean deleteRequested;
    private boolean canConnectWhileOwnerAway;
    private final String keyboardLayout;
    private boolean unrestrictedAccess;
    private final List<String> activeProtocols;

    private Date createdAt;

    public InstanceDto(Instance instance) {
        this.id = instance.getId();
        this.uid = instance.getUid();
        this.computeId = instance.getComputeId();
        this.ipAddress = instance.getIpAddress();
        this.name = instance.getName();
        this.comments = instance.getComments();
        this.plan = new PlanDto(instance.getPlan());
        this.state = instance.getState();
        this.screenWidth = instance.getScreenWidth();
        this.screenHeight = instance.getScreenHeight();
        this.members = instance.getMembers().stream().map(InstanceMemberDto::new).toList();
        this.experiments = instance.getExperiments().stream().map(ExperimentDto::new).toList();
        this.lastSeenAt = instance.getLastSeenAt();
        this.lastInteractionAt = instance.getLastInteractionAt();
        this.terminationDate = instance.getTerminationDate();
        this.expirationDate = instance.getExpirationDate();
        this.deleteRequested = instance.getDeleteRequested();
        this.keyboardLayout = instance.getKeyboardLayout();
        this.activeProtocols = instance.getActiveProtocols();
    }

    public Long getId() {
        return id;
    }

    public String getUid() {
        return uid;
    }

    public String getComputeId() {
        return computeId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getName() {
        return name;
    }

    public String getComments() {
        return comments;
    }

    public PlanDto getPlan() {
        return plan;
    }

    public InstanceState getState() {
        return state;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public List<InstanceMemberDto> getMembers() {
        return members;
    }

    public List<ExperimentDto> getExperiments() {
        return experiments;
    }

    public InstanceMemberDto getMembership() {
        return membership;
    }

    public void setMembership(InstanceMemberDto membership) {
        this.membership = membership;
    }

    public Date getTerminationDate() {
        return terminationDate;
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public boolean isDeleteRequested() {
        return deleteRequested;
    }

    public boolean isCanConnectWhileOwnerAway() {
        return canConnectWhileOwnerAway;
    }

    public void setCanConnectWhileOwnerAway(boolean canConnectWhileOwnerAway) {
        this.canConnectWhileOwnerAway = canConnectWhileOwnerAway;
    }

    public String getKeyboardLayout() {
        return keyboardLayout;
    }

    public boolean isUnrestrictedAccess() {
        return unrestrictedAccess;
    }

    public void setUnrestrictedAccess(boolean unrestrictedAccess) {
        this.unrestrictedAccess = unrestrictedAccess;
    }

    public List<String> getActiveProtocols() {
        return activeProtocols;
    }
}
