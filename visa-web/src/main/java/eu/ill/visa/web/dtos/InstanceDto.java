package eu.ill.visa.web.dtos;

import eu.ill.visa.core.domain.enumerations.InstanceState;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InstanceDto {

    private Long id;
    private String uid;
    private String computeId;
    private String ipAddress;
    private String name;
    private String comments;
    private PlanDto plan;
    private InstanceState state = InstanceState.UNKNOWN;
    private Integer screenWidth;
    private Integer screenHeight;
    private List<InstanceMemberDto> members = new ArrayList<>();
    private List<ExperimentDto> experiments = new ArrayList<>();
    private InstanceMemberDto membership;
    private Date lastSeenAt;
    private Date lastInteractionAt;
    private Date terminationDate;
    private Date expirationDate;
    private boolean deleteRequested;
    private boolean canConnectWhileOwnerAway;
    private String keyboardLayout;

    private Date createdAt;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getComputeId() {
        return computeId;
    }

    public void setComputeId(String computeId) {
        this.computeId = computeId;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public PlanDto getPlan() {
        return plan;
    }

    public void setPlan(PlanDto plan) {
        this.plan = plan;
    }

    public InstanceState getState() {
        return state;
    }

    public void setState(InstanceState state) {
        this.state = state;
    }

    public Integer getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(Integer screenWidth) {
        this.screenWidth = screenWidth;
    }

    public Integer getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(Integer screenHeight) {
        this.screenHeight = screenHeight;
    }

    public List<InstanceMemberDto> getMembers() {
        return members;
    }

    public void setMembers(List<InstanceMemberDto> members) {
        this.members = members;
    }

    public List<ExperimentDto> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<ExperimentDto> experiments) {
        this.experiments = experiments;
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

    public void setTerminationDate(Date terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Date lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(Date lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isDeleteRequested() {
        return deleteRequested;
    }

    public void setDeleteRequested(boolean deleteRequested) {
        this.deleteRequested = deleteRequested;
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

    public void setKeyboardLayout(String keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }
}
