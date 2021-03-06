package eu.ill.visa.vdi.services;

import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.InstanceMember;
import eu.ill.visa.core.domain.User;
import eu.ill.visa.vdi.domain.Role;

import java.util.List;

import static eu.ill.visa.vdi.domain.Role.*;
import static java.util.Objects.requireNonNull;

/**
 * Service to check if a  user has access to the instance
 */
public class RoleService {

    public Role getRole(final Instance instance, final User user) {
        requireNonNull(instance, "instance cannot be null");
        requireNonNull(user, "user cannot be null");

        final InstanceMember member = instance.getMember(user);
        if (member == null) {
            if (user.hasAnyRole(List.of("ADMIN", "IT_SUPPORT", "INSTRUMENT_CONTROL", "INSTRUMENT_SCIENTIST"))) {
                return SUPPORT;
            }

            return NONE;
        }
        return switch (member.getRole()) {
            case OWNER -> OWNER;
            case GUEST -> GUEST;
            case USER -> USER;
            case SUPPORT -> SUPPORT;
        };
    }
}
