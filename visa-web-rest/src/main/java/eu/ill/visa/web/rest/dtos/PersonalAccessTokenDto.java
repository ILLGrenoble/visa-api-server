package eu.ill.visa.web.rest.dtos;

import eu.ill.visa.core.entity.PersonalAccessToken;
import eu.ill.visa.core.entity.enumerations.InstanceMemberRole;

import java.util.Date;

public class PersonalAccessTokenDto {

    private final Long id;
    private final Long instanceId;
    private final String instanceUid;
    private final String token;
    private final String name;
    private final InstanceMemberRole role;
    private final Date createdAt;

    public PersonalAccessTokenDto(final PersonalAccessToken personalAccessToken) {
        this.id = personalAccessToken.getId();
        this.instanceId = personalAccessToken.getInstance().getId();
        this.instanceUid = personalAccessToken.getInstance().getUid();
        this.token = personalAccessToken.getToken();
        this.name = personalAccessToken.getName();
        this.role = personalAccessToken.getRole();
        this.createdAt = personalAccessToken.getCreatedAt();
    }

    public Long getId() {
        return id;
    }

    public Long getInstanceId() {
        return instanceId;
    }

    public String getInstanceUid() {
        return instanceUid;
    }

    public String getToken() {
        return token;
    }

    public String getName() {
        return name;
    }

    public InstanceMemberRole getRole() {
        return role;
    }

    public Date getCreatedAt() {
        return createdAt;
    }
}
