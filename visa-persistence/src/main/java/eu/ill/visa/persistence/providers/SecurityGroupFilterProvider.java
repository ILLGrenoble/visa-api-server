package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.entity.SecurityGroup;

import jakarta.persistence.EntityManager;

public class SecurityGroupFilterProvider extends AbstractFilterQueryProvider<SecurityGroup> {

    public SecurityGroupFilterProvider(EntityManager entityManager) {
        super(SecurityGroup.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("name")
        );
    }

}
