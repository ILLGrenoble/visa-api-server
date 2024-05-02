package eu.ill.visa.scheduler.jobs;

import eu.ill.visa.core.entity.InstanceAuthenticationToken;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import eu.ill.visa.business.services.InstanceAuthenticationTokenService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@ApplicationScoped
public class InstanceAuthenticationTokenDeleteJob {

    private static final Logger logger = LoggerFactory.getLogger(InstanceAuthenticationTokenDeleteJob.class);
    private final InstanceAuthenticationTokenService instanceAuthenticationTokenService;

    @Inject
    public InstanceAuthenticationTokenDeleteJob(InstanceAuthenticationTokenService instanceAuthenticationTokenService) {
        this.instanceAuthenticationTokenService = instanceAuthenticationTokenService;
    }

    // Run every 15 seconds
    @Scheduled(cron="0/15 * * * * ?",  concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    public void execute() {
        final AtomicInteger count = new AtomicInteger(0);
        final List<InstanceAuthenticationToken> tokens = this.instanceAuthenticationTokenService.getAll();
        tokens.stream().filter(token -> token.isExpired(30)).forEach(token -> {
            instanceAuthenticationTokenService.delete(token);
            count.incrementAndGet();
        });
        logger.debug("Removed {} expired instance authentication tokens", count);
    }
}
