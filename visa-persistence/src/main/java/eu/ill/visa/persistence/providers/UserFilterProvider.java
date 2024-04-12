package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.domain.User;

import jakarta.persistence.EntityManager;

public class UserFilterProvider extends AbstractFilterQueryProvider<User> {

    public UserFilterProvider(EntityManager entityManager) {
        super(User.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("firstName"),
            orderableField("lastName"),
            orderableField("email"),
            orderableField("userRoles.role.name", "role"),
            orderableField("lastSeenAt"),
            orderableField("activatedAt")
        );
    }

}
