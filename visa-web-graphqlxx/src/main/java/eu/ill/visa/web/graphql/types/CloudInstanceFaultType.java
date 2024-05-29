package eu.ill.visa.web.graphql.types;

import eu.ill.visa.cloud.domain.CloudInstanceFault;

public class CloudInstanceFaultType {
    private final String message;
    private final Integer code;
    private final String details;
    private final String created;

    public CloudInstanceFaultType(final CloudInstanceFault fault) {
        this.message = fault.getMessage();
        this.code = fault.getCode();
        this.details = fault.getDetails();
        this.created = fault.getCreated();;
    }

    public String getMessage() {
        return message;
    }

    public Integer getCode() {
        return code;
    }

    public String getDetails() {
        return details;
    }

    public String getCreated() {
        return created;
    }
}
