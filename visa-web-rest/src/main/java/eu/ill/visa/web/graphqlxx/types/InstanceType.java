package eu.ill.visa.web.graphqlxx.types;

import eu.ill.visa.core.entity.enumerations.InstanceState;
import eu.ill.visa.web.rest.dtos.PlanDto;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class InstanceType {

    @AdaptToScalar(Scalar.Int.class)
    private Long id;
    private String uid;
    private String name;
    private String comments;
    private InstanceState state = InstanceState.UNKNOWN;
    private List<InstanceMemberType> members = new ArrayList<>();
    private PlanDto plan;
    private CloudInstanceType cloudInstance;
    private List<ExperimentType> experiments = new ArrayList<>();
    private List<ProtocolStatusType> protocols = new ArrayList<>();
    private Date createdAt;
    private Date lastSeenAt;
    private Date lastInteractionAt;
    private Date terminationDate;
    private UserType owner;
    private List<InstanceSessionMemberType> sessions;
    private List<InstanceSessionMemberType> activeSessions;
    private String username;
    private String keyboardLayout;
    private List<InstanceAttributeType> attributes;
    private CloudClientType cloudClient;

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

    public InstanceState getState() {
        return state;
    }

    public void setState(InstanceState state) {
        this.state = state;
    }

    public List<InstanceMemberType> getMembers() {
        return members;
    }

    public void setMembers(List<InstanceMemberType> members) {
        this.members = members;
    }

    public PlanDto getPlan() {
        return plan;
    }

    public void setPlan(PlanDto plan) {
        this.plan = plan;
    }

    public CloudInstanceType getCloudInstance() {
        return cloudInstance;
    }

    public void setCloudInstance(CloudInstanceType cloudInstance) {
        this.cloudInstance = cloudInstance;
    }

    public List<ExperimentType> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<ExperimentType> experiments) {
        this.experiments = experiments;
    }

    public List<ProtocolStatusType> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<ProtocolStatusType> protocols) {
        this.protocols = protocols;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
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

    public Date getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(Date terminationDate) {
        this.terminationDate = terminationDate;
    }

    public UserType getOwner() {
        return owner;
    }

    public void setOwner(UserType owner) {
        this.owner = owner;
    }

    public List<InstanceSessionMemberType> getSessions() {
        return sessions;
    }

    public void setSessions(List<InstanceSessionMemberType> sessions) {
        this.sessions = sessions;
    }

    public List<InstanceSessionMemberType> getActiveSessions() {
        return activeSessions;
    }

    public void setActiveSessions(List<InstanceSessionMemberType> activeSessions) {
        this.activeSessions = activeSessions;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getKeyboardLayout() {
        return keyboardLayout;
    }

    public void setKeyboardLayout(String keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }

    public List<InstanceAttributeType> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<InstanceAttributeType> attributes) {
        this.attributes = attributes;
    }

    public CloudClientType getCloudClient() {
        return cloudClient;
    }

    public void setCloudClient(CloudClientType cloudClient) {
        this.cloudClient = cloudClient;
    }
}
