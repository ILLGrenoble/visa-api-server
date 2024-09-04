package eu.ill.visa.web.graphql.resources;

import eu.ill.preql.exception.InvalidQueryException;
import eu.ill.visa.business.services.InstanceExtensionRequestService;
import eu.ill.visa.business.services.UserService;
import eu.ill.visa.core.entity.InstanceExtensionRequest;
import eu.ill.visa.core.entity.Role;
import eu.ill.visa.core.entity.User;
import eu.ill.visa.core.entity.enumerations.InstanceExtensionRequestState;
import eu.ill.visa.web.graphql.exceptions.DataFetchingException;
import eu.ill.visa.web.graphql.exceptions.EntityNotFoundException;
import eu.ill.visa.web.graphql.exceptions.ValidationException;
import eu.ill.visa.web.graphql.inputs.InstanceExtensionResponseInput;
import eu.ill.visa.web.graphql.types.InstanceExtensionRequestType;
import io.smallrye.graphql.api.AdaptToScalar;
import io.smallrye.graphql.api.Scalar;
import jakarta.annotation.security.RolesAllowed;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@GraphQLApi
@RolesAllowed(Role.ADMIN_ROLE)
public class InstanceExtensionRequestResource {

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    private final InstanceExtensionRequestService instanceExtensionRequestService;
    private final UserService userService;

    @Inject
    public InstanceExtensionRequestResource(final InstanceExtensionRequestService instanceExtensionRequestService,
                                            final UserService userService) {
        this.instanceExtensionRequestService = instanceExtensionRequestService;
        this.userService = userService;
    }

    @Query
    public @NotNull List<InstanceExtensionRequestType> instanceExtensionRequests() throws DataFetchingException {
        try {
            return this.instanceExtensionRequestService.getAll().stream()
                .map(InstanceExtensionRequestType::new)
                .toList();
        } catch (InvalidQueryException exception) {
            throw new DataFetchingException(exception.getMessage());
        }
    }

    @Mutation
    public @NotNull InstanceExtensionRequestType handleInstanceExtensionRequest(@NotNull @AdaptToScalar(Scalar.Int.class) Long requestId, @NotNull @Valid InstanceExtensionResponseInput response) throws EntityNotFoundException {
        InstanceExtensionRequest request = this.instanceExtensionRequestService.getById(requestId);
        if (request == null) {
            throw new EntityNotFoundException("Extension request not found for the given id");
        }
        User user = this.userService.getById(response.getHandlerId());
        if (user == null) {
            throw new EntityNotFoundException("The user who handled the request could not be found");
        }

        try {
            request.setState(response.getAccepted() ? InstanceExtensionRequestState.ACCEPTED : InstanceExtensionRequestState.REFUSED);
            request.setHandledOn(new Date());
            request.setHandler(user);
            request.setHandlerComments(response.getHandlerComments());
            if (response.getAccepted()) {
                Date terminationDate = DATE_FORMAT.parse(response.getTerminationDate());
                request.setExtensionDate(terminationDate);

                this.instanceExtensionRequestService.grantExtension(request.getInstance(), terminationDate, response.getHandlerComments(), true);

            } else {
                this.instanceExtensionRequestService.refuseExtension(request.getInstance(), response.getHandlerComments());
            }

            // Update the request
            this.instanceExtensionRequestService.save(request);

            return new InstanceExtensionRequestType(request);

        } catch (ParseException e) {
            throw new ValidationException(e);
        }

    }


}
