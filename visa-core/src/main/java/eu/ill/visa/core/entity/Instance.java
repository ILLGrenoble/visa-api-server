package eu.ill.visa.core.entity;

import eu.ill.visa.core.entity.converter.CommaSeparatedListConverter;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;
import eu.ill.visa.core.entity.enumerations.InstanceState;
import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@NamedQueries({
    @NamedQuery(name = "instance.getIdByUid", query = """
            SELECT i.id
            FROM Instance i
            WHERE i.uid = :uid
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getById", query = """
            SELECT i FROM Instance i
            JOIN FETCH i.plan p
            JOIN FETCH p.image im
            JOIN FETCH p.flavour f
            WHERE i.id = :id
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getByIdForOwner", query = """
            SELECT i FROM Instance i
            JOIN i.members m
            JOIN FETCH i.plan p
            JOIN FETCH p.image im
            JOIN FETCH p.flavour f
            WHERE m.user = :user
            AND m.role = 'OWNER'
            AND i.id = :instanceId
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getByUID", query = """
            SELECT i FROM Instance i
            JOIN FETCH i.plan p
            JOIN FETCH p.image im
            JOIN FETCH p.flavour f
            WHERE i.uid = :uid
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getAll", query = """
            SELECT DISTINCT i FROM Instance i
            JOIN FETCH i.plan p
            JOIN FETCH p.image im
            JOIN FETCH p.flavour f
            WHERE i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getAllInactive", query = """
            SELECT DISTINCT i FROM Instance i
            WHERE i.deletedAt IS NULL
            AND i.terminationDate IS NOT NULL
            AND i.lastSeenAt < :date
    """),
    @NamedQuery(name = "instance.getAllNewInactive", query = """
            SELECT DISTINCT i FROM Instance i
            LEFT OUTER JOIN InstanceExpiration ie on ie.instance = i
            WHERE i.deletedAt IS NULL
            AND i.terminationDate IS NOT NULL
            AND i.lastSeenAt < :date
            AND ie IS NULL
    """),
    @NamedQuery(name = "instance.getAllNewTerminations", query = """
            SELECT DISTINCT i FROM Instance i
            LEFT OUTER JOIN InstanceExpiration ie on ie.instance = i
            WHERE i.deletedAt IS NULL
            AND i.terminationDate IS NOT NULL
            AND i.terminationDate < :date
            AND ie IS NULL
    """),
    @NamedQuery(name = "instance.countAll", query = """
            SELECT COUNT(i) FROM Instance i
            WHERE i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.countAllForState", query = """
            SELECT COUNT(i) FROM Instance i
            WHERE i.state = :state
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getAllForUser", query = """
            SELECT i FROM Instance i
            JOIN i.members m
            JOIN FETCH i.plan p
            JOIN FETCH p.image im
            JOIN FETCH p.flavour f
            WHERE m.user = :user
            AND i.deletedAt IS NULL
            ORDER BY i.id DESC
    """),
    @NamedQuery(name = "instance.getAllWithMembersForInstances", query = """
            SELECT i FROM Instance i
            LEFT JOIN FETCH i.members m
            LEFT JOIN FETCH m.user u
            LEFT JOIN FETCH u.affiliation a
            WHERE i IN :instances
            ORDER BY i.id DESC
    """),
    @NamedQuery(name = "instance.getAllWithExperimentsForInstances", query = """
            SELECT i FROM Instance i
            LEFT JOIN FETCH i.experiments e
            LEFT JOIN FETCH e.instrument instr
            WHERE i IN :instances
            ORDER BY i.id DESC
    """),
    @NamedQuery(name = "instance.getAllWithAttributesForInstances", query = """
            SELECT i FROM Instance i
            LEFT JOIN FETCH i.attributes a
            WHERE i IN :instances
            ORDER BY i.id DESC
    """),
    @NamedQuery(name = "instance.countAllForUser", query = """
            SELECT COUNT(i) FROM Instance i
            JOIN i.members m
            WHERE m.user = :user
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.countAllForUserAndRole", query = """
            SELECT COUNT(i) FROM Instance i
            JOIN i.members m
            WHERE m.user = :user
            AND m.role = :role
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getAllWithStates", query = """
            SELECT i FROM Instance i
            WHERE i.state in :states
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getAllToDelete", query = """
            SELECT i FROM Instance i
            WHERE i.state = 'STOPPED'
            AND i.deleteRequested = true
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getByIdForInstrumentScientist", query = """
            SELECT DISTINCT i FROM Instance i
            LEFT JOIN i.experiments e
            LEFT JOIN e.instrument instr
            LEFT JOIN InstrumentScientist ir on ir.instrument = instr
            WHERE ir.user = :user
            AND i.id = :instanceId
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getByIdForExperimentBetweenDates", query = """
            SELECT DISTINCT i FROM Instance i
            LEFT JOIN i.experiments e
            WHERE i.id = :id
            AND e.startDate <= :periodEnd
            AND e.endDate >= :periodStart
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getDeletedByComputeId", query = """
            SELECT i FROM Instance i
            WHERE i.computeId = :computeId
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.getPartialById", query = """
            SELECT new eu.ill.visa.core.entity.partial.InstancePartial(i.id, i.lastSeenAt, i.lastInteractionAt)
            FROM Instance i
            WHERE i.id = :id
            AND i.deletedAt IS NULL
    """),
    @NamedQuery(name = "instance.updatePartialById", query = """
            UPDATE Instance i
            SET i.lastSeenAt = :lastSeenAt, i.lastInteractionAt = :lastInteractionAt
            WHERE i.id = :id
    """),
    @NamedQuery(name = "instance.updateVdiProtocolById", query = """
            UPDATE Instance i
            SET i.vdiProtocol = :vdiProtocol
            WHERE i.id = :id
    """),
    @NamedQuery(name = "instance.countByFlavour", query = """
            SELECT new eu.ill.visa.core.entity.partial.NumberInstancesByFlavour(f.id, f.name, COUNT(i.id))
            FROM Instance i
            JOIN i.plan p
            JOIN p.flavour f
            WHERE i.deletedAt IS NULL
            GROUP BY f.id, f.name
            ORDER BY f.name
    """),
    @NamedQuery(name = "instance.countByImage", query = """
            SELECT new eu.ill.visa.core.entity.partial.NumberInstancesByImage(im.id, im.name, im.version, COUNT(i.id))
            FROM Instance i
            JOIN i.plan p
            JOIN p.image im
            WHERE i.deletedAt IS NULL
            GROUP BY im.id, im.name, im.version
            ORDER BY im.name, im.version DESC
    """),
    @NamedQuery(name = "instance.countByCloudClient", query = """
            SELECT new eu.ill.visa.core.entity.partial.NumberInstancesByCloudClient(cpc.id, cpc.name, COUNT(i.id))
            FROM Instance i
            JOIN i.plan p
            JOIN p.image im
            LEFT JOIN im.cloudProviderConfiguration cpc
            WHERE i.deletedAt IS NULL
            GROUP BY cpc.id, cpc.name
            ORDER BY cpc.name
    """),
})
@Table(name = "instance")
public class Instance extends Timestampable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "uid", length = 16, nullable = false)
    private String uid;

    @Column(name = "compute_id", length = 250, nullable = true)
    private String computeId;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @Column(name = "comments", length = 2500, nullable = true)
    private String comments;

    @ManyToOne
    @JoinColumn(name = "plan_id", foreignKey = @ForeignKey(name = "fk_plan_id"))
    private Plan plan;

    @Column(name = "username", length = 100, nullable = true)
    private String username;

    @Column(name = "ip_address", nullable = true)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", length = 50, nullable = false)
    private InstanceState state = InstanceState.UNKNOWN;

    @Column(name = "screen_width", nullable = false)
    private Integer screenWidth;

    @Column(name = "screen_height", nullable = false)
    private Integer screenHeight;

    @Column(name = "last_seen_at", nullable = true)
    private Date lastSeenAt = new Date();

    @Column(name = "last_interaction_at", nullable = true)
    private Date lastInteractionAt = new Date();

    @Column(name = "termination_date", nullable = true)
    private Date terminationDate;

    @Column(name = "expiration_date", nullable = true)
    private Date expirationDate;

    @Column(name = "deleted_at", nullable = true)
    private Date deletedAt;

    @Column(name = "delete_requested", nullable = false, columnDefinition = "")
    private Boolean deleteRequested = false;

    @Column(name = "unrestricted_member_access", nullable = true)
    private Date unrestrictedMemberAccess = null;

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_member_id"), nullable = false)
    private List<InstanceMember> members = new ArrayList<>();

    @ManyToMany()
    @JoinTable(
        name = "instance_experiment",
        joinColumns = @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id")),
        inverseJoinColumns = @JoinColumn(name = "experiment_id", foreignKey = @ForeignKey(name = "fk_experiment_id"))
    )
    private List<Experiment> experiments = new ArrayList<>();

    @OneToMany(orphanRemoval = true, cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE, CascadeType.REFRESH})
    @JoinColumn(name = "instance_id", foreignKey = @ForeignKey(name = "fk_instance_id"), nullable = false)
    private List<InstanceAttribute> attributes  = new ArrayList<>();

    @Column(name = "keyboard_layout", nullable = false)
    private String keyboardLayout;

    @Convert(converter = CommaSeparatedListConverter.class)
    @Column(name = "security_groups", nullable = true, columnDefinition = "TEXT")
    private List<String> securityGroups = new ArrayList<>();

    @Convert(converter = CommaSeparatedListConverter.class)
    @Column(name = "active_protocols", nullable = true, columnDefinition = "TEXT")
    private List<String> activeProtocols = new ArrayList<>();

    @Column(name = "state_hash", nullable = true)
    private Integer stateHash;

    @ManyToOne
    @JoinColumn(name = "vdi_protocol", foreignKey = @ForeignKey(name = "fk_vdi_protocol_id"), nullable = true)
    private ImageProtocol vdiProtocol;

    @Column(name = "public_access_token", length = 250, nullable = true)
    private String publicAccessToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "public_access_role", length = 255, nullable = true)
    private InstanceMemberRole publicAccessRole;

    public Instance() {
    }

    public Instance(Builder builder) {
        this.id = builder.id;
        this.uid = builder.uid;
        this.computeId = builder.computeId;
        this.name = builder.name;
        this.comments = builder.comments;
        this.plan = builder.plan;
        this.username = builder.username;
        this.state = builder.state;
        this.screenWidth = builder.screenWidth;
        this.screenHeight = builder.screenHeight;
        this.lastSeenAt = builder.lastSeenAt;
        this.keyboardLayout = builder.keyboardLayout;
        this.members.addAll(builder.members);
        this.experiments.addAll(builder.experiments);
        this.attributes.addAll(builder.attributes);
        this.vdiProtocol = builder.vdiProtocol;
    }

    public static Builder builder() {
        return new Builder();
    }

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

    public Plan getPlan() {
        return plan;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setPlan(Plan plan) {
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

    public Date getLastSeenAt() {
        return lastSeenAt;
    }

    public void setLastSeenAt(Date lastSeenAt) {
        this.lastSeenAt = lastSeenAt;
    }

    public void updateLastSeenAt() {
        this.lastSeenAt = new Date();
    }

    public Date getLastInteractionAt() {
        return lastInteractionAt;
    }

    public void setLastInteractionAt(Date lastInteractionAt) {
        this.lastInteractionAt = lastInteractionAt;
    }

    public void updateLastInteractionAt() {
        this.lastSeenAt = new Date();
    }

    public Date getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(Date terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Transient
    public Boolean getDeleted() {
        return this.deletedAt != null;
    }

    public void setDeleted(Boolean deleted) {
        if (deleted) {
            this.setDeletedAt(new Date());
        } else {
            this.setDeletedAt(null);
        }
    }

    public Date getDeletedAt() {
        return deletedAt;
    }

    public void setDeletedAt(Date deletedAt) {
        this.deletedAt = deletedAt;
    }

    public Boolean getDeleteRequested() {
        return deleteRequested;
    }

    public void setDeleteRequested(Boolean deleteRequested) {
        this.deleteRequested = deleteRequested;
    }

    public Date getUnrestrictedMemberAccess() {
        return unrestrictedMemberAccess;
    }

    public void setUnrestrictedMemberAccess(Date unrestrictedMemberAccess) {
        this.unrestrictedMemberAccess = unrestrictedMemberAccess;
    }

    public boolean canAccessWhenOwnerAway() {
        return this.unrestrictedMemberAccess != null;
    }

    public String getKeyboardLayout() {
        return keyboardLayout;
    }

    public void setKeyboardLayout(String keyboardLayout) {
        this.keyboardLayout = keyboardLayout;
    }

    public List<String> getSecurityGroups() {
        return securityGroups;
    }

    public void setSecurityGroups(List<String> securityGroups) {
        this.securityGroups = securityGroups;
    }

    public List<String> getActiveProtocols() {
        return activeProtocols;
    }

    public void setActiveProtocols(List<String> activeProtocols) {
        this.activeProtocols = activeProtocols;
    }

    public List<InstanceMember> getMembers() {
        return members;
    }

    public void setMembers(List<InstanceMember> members) {
        this.members = members;
    }

    public InstanceMember addMember(InstanceMember member) {
        InstanceMember aMember = this.getMember(member.getUser(), member.getRole());

        if (aMember != null) {
            return aMember;

        } else {
            this.members.add(member);
            return member;
        }
    }

    public void removeMember(InstanceMember instanceMember) {
        InstanceMember aMember = this.getMember(instanceMember.getUser(), instanceMember.getRole());
        if (aMember != null) {
            this.members.remove(aMember);
        }
    }

    public boolean isMember(User user, InstanceMemberRole role) {
        for (final InstanceMember member : this.members) {
            if (member.isUser(user) && member.isRole(role)) {
                return true;
            }
        }
        return false;
    }


    @Transient
    public InstanceMember getOwner() {
        for (final InstanceMember aMember : this.members) {
            if (aMember.isRole(InstanceMemberRole.OWNER)) {
                return aMember;
            }
        }
        return null;
    }

    public InstanceMember getMember(User user, InstanceMemberRole role) {
        for (final InstanceMember aMember : this.members) {
            if (aMember.isUser(user) && aMember.isRole(role)) {
                return aMember;
            }
        }
        return null;
    }

    public InstanceMember getMember(User user) {
        for (final InstanceMember aMember : this.members) {
            if (aMember.isUser(user)) {
                return aMember;
            }
        }
        return null;
    }

    public boolean isMember(InstanceMember member) {
        return (this.getMember(member.getUser(), member.getRole()) != null);
    }

    public boolean isMember(User user) {
        for (final InstanceMember member : this.members) {
            if (member.isUser(user)) {
                return true;
            }
        }
        return false;
    }

    public boolean isOwner(User user) {
        for (final InstanceMember aMember : this.members) {
            if (aMember.isUser(user) && aMember.isRole(InstanceMemberRole.OWNER)) {
                return true;
            }
        }
        return false;
    }

    public List<Experiment> getExperiments() {
        return experiments;
    }

    public void setExperiments(List<Experiment> experiments) {
        this.experiments = experiments;
    }

    public void addExperiment(Experiment experiment) {
        this.experiments.add(experiment);
    }

    public void removeExperiment(Experiment experiment) {
        this.experiments.remove(experiment);
    }


    public List<InstanceAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<InstanceAttribute> attributes) {
        this.attributes = attributes;
    }

    public void addMetadata(InstanceAttribute attribute) {
        this.attributes.add(attribute);
    }

    public boolean hasAnyState(List<InstanceState> targetStates) {
        for (final InstanceState targetState : targetStates) {
            if (this.state.equals(targetState)) {
                return true;
            }
        }
        return false;
    }

    public Integer getStateHash() {
        return stateHash;
    }

    public void setStateHash(Integer stateHash) {
        this.stateHash = stateHash;
    }

    public void updateStateHash() {
        this.stateHash = new HashCodeBuilder(17, 37)
            .append(state)
            .append(computeId)
            .append(name)
            .append(comments)
            .append(ipAddress)
            .append(terminationDate)
            .append(expirationDate)
            .append(deleteRequested)
            .append(unrestrictedMemberAccess)
            .append(activeProtocols)
            .append(vdiProtocol)
            .toHashCode();
    }

    public ImageProtocol getVdiProtocol() {
        return vdiProtocol;
    }

    public void setVdiProtocol(ImageProtocol vdiProtocol) {
        this.vdiProtocol = vdiProtocol;
    }

    public String getPublicAccessToken() {
        return publicAccessToken;
    }

    public void setPublicAccessToken(String publicAccessToken) {
        this.publicAccessToken = publicAccessToken;
    }

    public InstanceMemberRole getPublicAccessRole() {
        return publicAccessRole;
    }

    public void setPublicAccessRole(InstanceMemberRole publicAccessRole) {
        this.publicAccessRole = publicAccessRole;
    }

    @Transient
    public Long getCloudId() {
        return this.plan.getCloudId();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("uid", uid)
            .append("computeId", computeId)
            .append("name", name)
            .append("comments", comments)
            .append("plan", plan)
            .append("username", username)
            .append("ipAddress", ipAddress)
            .append("state", state)
            .append("screenWidth", screenWidth)
            .append("screenHeight", screenHeight)
            .append("lastSeenAt", lastSeenAt)
            .append("terminationDate", terminationDate)
            .append("keyboardLayout", keyboardLayout)
            .append("members", members)
            .append("attributes", attributes)
            .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Instance instance = (Instance) o;

        return new EqualsBuilder()
            .append(id, instance.id)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(id)
            .toHashCode();
    }

    public InstanceMember createMember(User user, InstanceMemberRole role) {
        InstanceMember instanceMember = InstanceMember.newBuilder()
            .user(user)
            .role(role)
            .build();

        this.addMember(instanceMember);

        return instanceMember;
    }

    public static final class Builder {
        private Long id;
        private String uid;
        private String computeId;
        private String name;
        private String comments;
        private Plan plan;
        private String username;
        private InstanceState state = InstanceState.UNKNOWN;
        private Integer screenWidth;
        private Integer screenHeight;
        private List<InstanceMember> members = new ArrayList<>();
        private List<Experiment> experiments = new ArrayList<>();
        private Date lastSeenAt;
        private String keyboardLayout;
        private List<InstanceAttribute> attributes = new ArrayList<>();
        private ImageProtocol vdiProtocol;

        private Builder() {
        }

        public Instance build() {
            return new Instance(this);
        }

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder uid(String uid) {
            this.uid = uid;
            return this;
        }

        public Builder computeId(String computeId) {
            this.computeId = computeId;
            return this;
        }

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder comments(String comments) {
            this.comments = comments;
            return this;
        }

        public Builder plan(Plan plan) {
            this.plan = plan;
            return this;
        }

        public Builder username(String username) {
            this.username = username;
            return this;
        }

        public Builder state(InstanceState state) {
            this.state = state;
            return this;
        }

        public Builder screenWidth(Integer screenWidth) {
            this.screenWidth = screenWidth;
            return this;
        }

        public Builder lastSeenAt(Date lastSeenAt) {
            this.lastSeenAt = lastSeenAt;
            return this;
        }

        public Builder screenHeight(Integer screenHeight) {
            this.screenHeight = screenHeight;
            return this;
        }

        public Builder keyboardLayout(String layout) {
            this.keyboardLayout = layout;
            return this;
        }

        public Builder members(List<InstanceMember> members) {
            this.members = members;
            return this;
        }

        public Builder experiments(List<Experiment> experiments) {
            this.experiments = experiments;
            return this;
        }


        public Builder attributes(List<InstanceAttribute> attributes) {
            this.attributes = attributes;
            return this;
        }

        public Builder vdiProtocol(ImageProtocol vdiProtocol) {
            this.vdiProtocol = vdiProtocol;
            return this;
        }

        public Builder member(User user, InstanceMemberRole role) {
            InstanceMember instanceMember = InstanceMember.newBuilder()
                .user(user)
                .role(role)
                .build();
            this.members.add(instanceMember);

            return this;
        }
    }
}
