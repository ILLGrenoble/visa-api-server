package eu.ill.visa.business.concurrent;

import eu.ill.visa.business.concurrent.actions.InstanceAction;

public interface InstanceActionListener {

    void onActionStart(InstanceAction action);
    void onActionTerminated(InstanceAction action);
    void onActionFailed(InstanceAction action);
}
