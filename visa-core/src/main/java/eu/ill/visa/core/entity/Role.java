package eu.ill.visa.core.entity;

import jakarta.persistence.*;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Date;

@Entity
@NamedQueries({
    @NamedQuery(name = "role.getById", query = """
            SELECT r
            FROM Role r
            WHERE r.id = :id
            AND r.groupDeletedAt IS NULL
    """),
    @NamedQuery(name = "role.getByName", query = """
            SELECT r
            FROM Role r
            WHERE r.name = :name
            AND r.groupDeletedAt IS NULL
    """),
    @NamedQuery(name = "role.getAll", query = """
            SELECT r
            FROM Role r
            WHERE r.groupDeletedAt IS NULL
            ORDER BY r.groupCreatedAt DESC, r.name ASC
    """),
    @NamedQuery(name = "role.getAllRoles", query = """
            SELECT r
            FROM Role r
            WHERE r.groupDeletedAt IS NULL
            AND r.groupCreatedAt IS NULL
            ORDER BY r.name ASC
    """),
    @NamedQuery(name = "role.getAllGroups", query = """
            SELECT r
            FROM Role r
            WHERE r.groupDeletedAt IS NULL
            AND r.groupCreatedAt IS NOT NULL
            ORDER BY r.name ASC
    """),
})
@Table(name = "role", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"name"}, name = "uk_role_name")
})
public class Role {

    public static final String ADMIN_ROLE = "ADMIN";
    public static final String STAFF_ROLE = "STAFF";
    public static final String INSTRUMENT_CONTROL_ROLE   = "INSTRUMENT_CONTROL";
    public static final String INSTRUMENT_SCIENTIST_ROLE = "INSTRUMENT_SCIENTIST";
    public static final String IT_SUPPORT_ROLE           = "IT_SUPPORT";
    public static final String SCIENTIFIC_COMPUTING_ROLE = "SCIENTIFIC_COMPUTING";
    public static final String GUEST_ROLE = "GUEST";
    public static final String APPLICATION_CREDENTIAL_ROLE = "APPLICATION_CREDENTIAL";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "description", length = 250)
    private String description;

    @Column(name = "group_created_at")
    private Date groupCreatedAt;

    @Column(name = "group_deleted_at")
    private Date groupDeletedAt;


    public Role() {

    }

    public Role(String name) {
        this.name = name;
    }

    public Role(String name, String description) {
        this.name = name;
        this.description = description;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getGroupCreatedAt() {
        return groupCreatedAt;
    }

    public void setGroupCreatedAt(Date groupCreatedAt) {
        this.groupCreatedAt = groupCreatedAt;
    }

    public Date getGroupDeletedAt() {
        return groupDeletedAt;
    }

    public void setGroupDeletedAt(Date groupDeletedAt) {
        this.groupDeletedAt = groupDeletedAt;
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Role) {
            final Role other = (Role) object;
            return new EqualsBuilder()
                .append(name, other.name)
                .isEquals();
        }
        return false;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
            .append(id)
            .append(name)
            .toHashCode();
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("id", id)
            .append("name", name)
            .toString();
    }


    public boolean isName(String name) {
        return this.name.equalsIgnoreCase(name);
    }
}
