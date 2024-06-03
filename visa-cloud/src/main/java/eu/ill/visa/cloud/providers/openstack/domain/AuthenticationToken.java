package eu.ill.visa.cloud.providers.openstack.domain;

import java.util.Date;

public record AuthenticationToken(String token, Date expiresAt) {

    public boolean isValid() {
        final Date now = new Date();
        return this.token != null && expiresAt.after(now);
    }
}
