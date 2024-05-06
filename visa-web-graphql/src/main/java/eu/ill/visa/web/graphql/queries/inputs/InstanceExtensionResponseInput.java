package eu.ill.visa.web.graphql.queries.inputs;

import jakarta.validation.constraints.NotNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

public class InstanceExtensionResponseInput {

    public final static DateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm");

    @NotNull
    private String handlerId;

    @NotNull
    private String handlerComments;

    @NotNull
    private String terminationDate;

    @NotNull
    private Boolean accepted;

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
