package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.visa.core.domain.SecurityGroup;
import eu.ill.visa.core.domain.SecurityGroupFilter;

import javax.persistence.EntityManager;

public class SecurityGroupFilterFilterProvider extends AbstractFilterQueryProvider<SecurityGroupFilter> {

    public SecurityGroupFilterFilterProvider(EntityManager entityManager) {
        super(SecurityGroupFilter.class, entityManager);
        addFields(
            orderableField("objectId"),
            orderableField("objectType"),
            orderableField("securityGroup.id", "sg.id"),
            orderableField("securityGroup.name", "sg.name")
        );
    }

}
