package eu.ill.visa.core.domain;

public class InstanceJupyterSession extends Timestampable {

    private Long id;
    private Instance instance;
    private User user;
    private String kernelId;
    private String sessionId;
    private boolean active = false;

    public InstanceJupyterSession() {
    }

    public InstanceJupyterSession(Instance instance, User user, String kernelId, String sessionId) {
        this.instance = instance;
        this.user = user;
        this.kernelId = kernelId;
        this.sessionId = sessionId;
        this.active = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getKernelId() {
        return kernelId;
    }

    public void setKernelId(String kernelId) {
        this.kernelId = kernelId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

}
