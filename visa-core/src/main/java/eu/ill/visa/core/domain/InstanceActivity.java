package eu.ill.visa.core.domain;

import eu.ill.visa.core.domain.enumerations.InstanceActivityType;

public class InstanceActivity extends Timestampable {
    private Long   id;
    private User user;
    private Instance instance;
    private InstanceActivityType instanceActivityType;

    public InstanceActivity() {

    }

    public InstanceActivity(InstanceActivityType type) {
        this.instanceActivityType = type;
    }

    public InstanceActivity(User user, Instance instance, InstanceActivityType type) {
        this.user = user;
        this.instance = instance;
        this.instanceActivityType = type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }

    public InstanceActivityType getInstanceActivityType() {
        return instanceActivityType;
    }

    public void setInstanceActivityType(InstanceActivityType instanceActivityType) {
        this.instanceActivityType = instanceActivityType;
    }
}
