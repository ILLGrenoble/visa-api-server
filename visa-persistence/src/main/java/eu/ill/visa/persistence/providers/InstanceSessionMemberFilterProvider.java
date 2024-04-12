package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.domain.InstanceSessionMember;

import jakarta.persistence.EntityManager;

public class InstanceSessionMemberFilterProvider extends AbstractFilterQueryProvider<InstanceSessionMember> {

    public InstanceSessionMemberFilterProvider(EntityManager entityManager) {
        super(InstanceSessionMember.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("createdAt"),
            field("active")

        );
    }

}
