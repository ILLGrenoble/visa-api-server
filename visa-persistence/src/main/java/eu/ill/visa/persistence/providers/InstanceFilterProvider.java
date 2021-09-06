package eu.ill.visa.persistence.providers;

import eu.ill.preql.AbstractFilterQueryProvider;
import eu.ill.preql.parser.FieldValueParser;
import eu.ill.preql.parser.value.StringValueParser;
import eu.ill.visa.core.domain.Instance;
import eu.ill.visa.core.domain.enumerations.InstanceMemberRole;
import eu.ill.visa.core.domain.enumerations.InstanceState;

import javax.persistence.EntityManager;

public class InstanceFilterProvider extends AbstractFilterQueryProvider<Instance> {

    public InstanceFilterProvider(EntityManager entityManager) {
        super(Instance.class, entityManager);
        addFields(
            orderableField("id"),
            orderableField("name"),
            orderableField("comments"),
            orderableField("createdAt"),
            orderableField("lastSeenAt"),
            orderableField("plan.id"),
            orderableField("plan.flavour.id"),
            orderableField("plan.flavour.name"),
            orderableField("plan.image.id"),
            orderableField("plan.image.name"),
            orderableField("experiments.instrument.id", "instrument.id"),
            orderableField("experiments.instrument.name", "instrument.name"),
            orderableField("members.user.id", "user.id"),
            orderableField("members.user.firstName", "user.firstName"),
            orderableField("members.user.lastName", "user.lastName"),
            orderableField("state", (FieldValueParser<InstanceState>) value -> {
                final StringValueParser parser = new StringValueParser();
                return InstanceState.valueOf(parser.parse(value));
            }),
            orderableField("members.role", "user.role", (FieldValueParser<InstanceMemberRole>) value -> {
                final StringValueParser parser = new StringValueParser();
                return InstanceMemberRole.valueOf(parser.parse(value));
            })
        );
    }

}
