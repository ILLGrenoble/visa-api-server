package eu.ill.visa.web.converters.http;


import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstanceMemberService;
import eu.ill.visa.core.domain.InstanceMember;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.ext.ParamConverter;


public class InstanceMemberParamConverter implements ParamConverter<InstanceMember> {

    private final InstanceMemberService instanceMemberService;

    @Inject
    public InstanceMemberParamConverter(InstanceMemberService instanceMemberService) {
        this.instanceMemberService = instanceMemberService;
    }

    @Override
    public InstanceMember fromString(final String value) {
        if (value.matches("\\d+")) {
            final Long id = Long.parseLong(value);
            final InstanceMember instanceMember = instanceMemberService.getById(id);
            if (instanceMember == null) {
                throw new NotFoundException("InstanceMember not found");
            }

            return instanceMember;
        }
        throw new NotFoundException("InstanceMember not found");
    }

    @Override
    public String toString(final InstanceMember value) {
        return value.toString();
    }
}
