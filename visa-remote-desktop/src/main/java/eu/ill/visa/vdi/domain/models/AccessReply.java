package eu.ill.visa.vdi.domain.models;

public class AccessReply {
    String id;
    String response;

    public AccessReply(String id, String response) {
        this.id = id;
        this.response = response;
    }

    public AccessReply() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public Role getRole() {
        if (this.response.equals("GUEST")) {
            return Role.GUEST;
        } else if (this.response.equals("SUPPORT")) {
            return Role.SUPPORT;
        }
        return Role.NONE;
    }
}
