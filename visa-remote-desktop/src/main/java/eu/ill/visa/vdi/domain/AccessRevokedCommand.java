package eu.ill.visa.vdi.domain;

import java.io.Serializable;

public class AccessRevokedCommand implements Serializable {
    String userId;

    public AccessRevokedCommand() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}
