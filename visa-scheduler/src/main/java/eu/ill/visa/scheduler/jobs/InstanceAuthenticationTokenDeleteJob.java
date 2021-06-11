package eu.ill.visa.scheduler.jobs;

import com.google.inject.Inject;
import eu.ill.visa.business.services.InstanceAuthenticationTokenService;
import eu.ill.visa.core.domain.InstanceAuthenticationToken;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@DisallowConcurrentExecution
public class InstanceAuthenticationTokenDeleteJob implements Job {

    private static final Logger logger = LoggerFactory.getLogger(InstanceAuthenticationTokenDeleteJob.class);
    private InstanceAuthenticationTokenService instanceAuthenticationTokenService;

    @Inject
    public InstanceAuthenticationTokenDeleteJob(InstanceAuthenticationTokenService instanceAuthenticationTokenService) {
        this.instanceAuthenticationTokenService = instanceAuthenticationTokenService;
    }

    @Override
    public void execute(JobExecutionContext context) {
        final AtomicInteger count = new AtomicInteger(0);
        final List<InstanceAuthenticationToken> tokens = this.instanceAuthenticationTokenService.getAll();
        tokens.stream().filter(token -> token.isExpired(30)).forEach(token -> {
            instanceAuthenticationTokenService.delete(token);
            count.incrementAndGet();
        });
        logger.debug("Removed {} expired instance authentication tokens", count);
    }
}
