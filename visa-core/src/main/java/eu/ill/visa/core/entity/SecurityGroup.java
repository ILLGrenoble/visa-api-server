package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Entity
@NamedQueries({
    @NamedQuery(name = "securityGroup.getById", query = """
            SELECT sg
            FROM SecurityGroup sg
            LEFT JOIN sg.cloudProviderConfiguration cpc
            WHERE sg.id = :id
            AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "securityGroup.getAll", query = """
            SELECT sg
            FROM SecurityGroup sg
            LEFT JOIN sg.cloudProviderConfiguration cpc
            WHERE cpc.deletedAt IS NULL
            ORDER BY sg.name
    """),
    @NamedQuery(name = "securityGroup.getByName", query = """
            SELECT sg
            FROM SecurityGroup sg
            LEFT JOIN sg.cloudProviderConfiguration cpc
            WHERE sg.name = :name
            AND cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "securityGroup.countAll", query = """
            SELECT count(sg.id)
            FROM SecurityGroup sg
            LEFT JOIN sg.cloudProviderConfiguration cpc
            WHERE cpc.deletedAt IS NULL
    """),
    @NamedQuery(name = "securityGroup.getDefaultSecurityGroups", query = """
            SELECT DISTINCT sg
            FROM SecurityGroup sg
            LEFT JOIN sg.cloudProviderConfiguration cpc
            LEFT OUTER JOIN SecurityGroupFilter sgf ON sgf.securityGroup = sg
            WHERE sgf.objectId IS NULL
            AND cpc.deletedAt IS NULL
    """),
})
@NamedNativeQueries({
    @NamedNativeQuery(name = "securityGroup.getFlavourBasedSecurityGroups", resultClass = SecurityGroup.class, query = """
                SELECT DISTINCT sg.id, sg.name, sg.cloud_provider_configuration_id
                FROM security_group sg
                LEFT JOIN cloud_provider_configuration cpc ON sg.cloud_provider_configuration_id = cpc.id
                LEFT JOIN security_group_filter sgf ON sg.id = sgf.security_group_id
                LEFT JOIN flavour f ON f.id = sgf.object_id
                WHERE sgf.object_type = 'FLAVOUR'
                AND f.deleted = false
                AND f.id = :flavourId
                AND cpc.deleted_at IS NULL
    """),
    @NamedNativeQuery(name = "securityGroup.getRoleBasedSecurityGroups", resultClass = SecurityGroup.class, query = """
                SELECT DISTINCT sg.id, sg.name, sg.cloud_provider_configuration_id
                FROM security_group sg
                LEFT JOIN cloud_provider_configuration cpc ON sg.cloud_provider_configuration_id = cpc.id
                LEFT JOIN security_group_filter sgf ON sg.id = sgf.security_group_id
                LEFT JOIN role r ON r.id = sgf.object_id
                LEFT JOIN user_role ur ON r.id = ur.role_id
                LEFT JOIN users u ON ur.user_id = u.id
                WHERE sgf.object_type = 'ROLE'
                AND r.group_deleted_at IS NULL
                AND u.id = :userId
                AND cpc.deleted_at IS NULL
    """),
})
@Table(name = "security_group")
public class SecurityGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 250, nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "cloud_provider_configuration_id", foreignKey = @ForeignKey(name = "fk_cloud_provider_configuration_id"), nullable = true)
    private CloudProviderConfiguration cloudProviderConfiguration;

    public SecurityGroup() {
    }

    public SecurityGroup(String name) {
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public CloudProviderConfiguration getCloudProviderConfiguration() {
        return cloudProviderConfiguration;
    }

    public void setCloudProviderConfiguration(CloudProviderConfiguration cloudProviderConfiguration) {
        this.cloudProviderConfiguration = cloudProviderConfiguration;
    }

    @Transient
    public Long getCloudId() {
        return this.cloudProviderConfiguration == null ? null : this.cloudProviderConfiguration.getId();
    }

    public boolean hasSameCloudClientId(Long cloudClientId) {
        if (this.cloudProviderConfiguration == null) {
            return cloudClientId == null || cloudClientId.equals(-1L);

        } else {
            return this.cloudProviderConfiguration.getId().equals(cloudClientId);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        SecurityGroup that = (SecurityGroup) o;

        return new EqualsBuilder()
            .append(name, that.name)
            .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
            .append(name)
            .toHashCode();
    }
}
