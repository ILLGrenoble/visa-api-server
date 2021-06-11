package eu.ill.visa.business.concurrent;

import eu.ill.visa.core.domain.Instance;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;

public class InstanceActionFuture extends FutureTask<Future<Instance>> {

    private InstanceActionRunner runner;

    public InstanceActionFuture(InstanceActionDispatcher dispatcher, InstanceActionRunner runner) {
        super(() -> dispatcher.execute(runner));
        this.runner = runner;
    }

    public InstanceActionRunner getRunner() {
        return runner;
    }

    public Instance getFutureInstance() throws ExecutionException, InterruptedException {
        Future<Instance> future = super.get();
        return future.get();
    }

}
