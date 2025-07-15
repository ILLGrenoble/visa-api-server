package eu.ill.visa.security.tokens;


import eu.ill.visa.core.entity.Instance;

import java.security.Principal;

public class InstanceToken implements Principal {

    private Instance instance;

    public InstanceToken() {
    }

    public InstanceToken(final Instance instance) {
        this.instance = instance;
    }

    @Override
    public String getName() {
        return instance.getName();
    }

    public Instance getInstance() {
        return instance;
    }

    public void setInstance(Instance instance) {
        this.instance = instance;
    }
}

