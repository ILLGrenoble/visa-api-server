package eu.ill.visa.cloud.domain;

public class CloudInstanceFault {
    private String message;
    private Integer code;
    private String details;
    private String created;

    public CloudInstanceFault() {

    }

    public CloudInstanceFault(String message, Integer code, String details, String created) {
        this.message = message;
        this.code = code;
        this.details = details;
        this.created = created;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }
}
