package eu.ill.visa.web.rest.converters;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.entity.User;

import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ext.ParamConverter;

@ApplicationScoped
public class UserParamConverter implements ParamConverter<User> {

    private final UserService userService;

    @Inject
    public UserParamConverter(final UserService userService) {
        this.userService = userService;
    }

    @Override
    public User fromString(final String value) {
        if (value.matches("\\d+")) {
            final User user = userService.getById(value);
            if (user == null) {
                throw new NotFoundException("User not found");
            }
            return user;
        }
        throw new NotFoundException("User not found");
    }

    @Override
    public String toString(final User value) {
        return value.toString();
    }
}
