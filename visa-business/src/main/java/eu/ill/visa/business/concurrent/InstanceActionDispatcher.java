package eu.ill.visa.business.concurrent;

import eu.ill.visa.business.BusinessConfiguration;
import eu.ill.visa.core.entity.Instance;
import io.quarkus.runtime.Startup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Startup
@ApplicationScoped
public class InstanceActionDispatcher {

    private static final Logger log = LoggerFactory.getLogger(InstanceActionDispatcher.class);

    private final ThreadPoolExecutor threadPoolExecutor;

    @Inject
    public InstanceActionDispatcher(BusinessConfiguration businessConfiguration) {
        Integer threadPoolSize = businessConfiguration.numberInstanceActionThreads();
        log.info("Starting the InstanceActionDispatcher with " + threadPoolSize + " threads") ;
        this.threadPoolExecutor = new ThreadPoolExecutor(threadPoolSize, threadPoolSize, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>(), new InstanceActionThreadFactory());
    }

    public synchronized Future<Instance> execute(InstanceActionRunner runner) {
        return this.threadPoolExecutor.submit(runner);
    }
}
