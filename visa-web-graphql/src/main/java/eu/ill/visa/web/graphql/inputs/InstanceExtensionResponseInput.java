package eu.ill.visa.web.graphql.inputs;

import jakarta.validation.constraints.NotNull;
import org.eclipse.microprofile.graphql.Input;

@Input("InstanceExtensionResponseInput")
public class InstanceExtensionResponseInput {

    private @NotNull String handlerId;
    private String handlerComments;
    private @NotNull String terminationDate;
    private @NotNull Boolean accepted;

    public String getHandlerId() {
        return handlerId;
    }

    public void setHandlerId(String handlerId) {
        this.handlerId = handlerId;
    }

    public String getHandlerComments() {
        return handlerComments;
    }

    public void setHandlerComments(String handlerComments) {
        this.handlerComments = handlerComments;
    }

    public String getTerminationDate() {
        return terminationDate;
    }

    public void setTerminationDate(String terminationDate) {
        this.terminationDate = terminationDate;
    }

    public Boolean getAccepted() {
        return accepted;
    }

    public void setAccepted(Boolean accepted) {
        this.accepted = accepted;
    }
}
