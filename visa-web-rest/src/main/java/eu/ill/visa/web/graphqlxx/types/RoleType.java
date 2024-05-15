package eu.ill.visa.web.graphqlxx.types;

import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;

import java.util.Date;

public class RoleType {

    @AdaptToScalar(Scalar.Int.class)
    private Long id;
    private String name;
    private String description;
    private Date groupCreatedAt;

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
}
