package eu.ill.visa.broker.brokers.redis;


import eu.ill.visa.core.domain.Timer;
import io.quarkus.redis.datasource.pubsub.PubSubCommands;
import io.smallrye.mutiny.subscription.Cancellable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class RedisPubSubHealthMonitor {

    private static final Logger logger = LoggerFactory.getLogger(RedisPubSubHealthMonitor.class);

    private final static String CHANNEL = "VISA_REDIS_HEARTBEAT" + UUID.randomUUID();
    private final static String PING_MESSAGE = "ping";
    private static final int PING_INTERVAL_TIME_SECONDS = 10;
    private static final int PING_TIMEOUT_SECONDS = 5;
    private static final int CONNECTION_RETRY_SECONDS = 5;

    private final PubSubCommands<RedisMessageCarrier> publisher;
    private PubSubCommands.RedisSubscriber subscriber;

    private Cancellable redisPingWaitTimer = null;
    private Cancellable redisPingTimeout = null;
    private final Runnable onDisconnected;
    private boolean isRunning = false;

    public RedisPubSubHealthMonitor(final PubSubCommands<RedisMessageCarrier> publisher, final Runnable onDisconnected) {
        this.publisher = publisher;
        this.onDisconnected = onDisconnected;

        // Start the ping interval
        this.start();
    }

    public void start() {
        if (!this.isRunning) {
            this.isRunning = true;
            logger.info("Starting Redis pub/sub health monitor...");
            this.subscribeAndStart();
        }
    }

    public void stop() {
        if (!this.isRunning) {
            return;
        }

        this.isRunning = false;
        if (this.redisPingWaitTimer != null) {
            this.redisPingWaitTimer.cancel();
            this.redisPingWaitTimer = null;
        }
        if (this.redisPingTimeout != null) {
            this.redisPingTimeout.cancel();
            this.redisPingTimeout = null;
        }

        this.unsubscribe();
    }

    private void startPingOperation() {
        if (this.subscriber != null && this.redisPingWaitTimer == null) {
            this.redisPingWaitTimer = Timer.setTimeout(this::pingRedis, PING_INTERVAL_TIME_SECONDS, TimeUnit.SECONDS);
        }
    }

    private void pingRedis() {
        this.redisPingWaitTimer = null;
        if (this.redisPingTimeout == null) {
            try {
                this.redisPingTimeout = Timer.setTimeout(this::onPingTimeout, PING_TIMEOUT_SECONDS, TimeUnit.SECONDS);
                publisher.publish(CHANNEL, new RedisMessageCarrier(PING_MESSAGE));

            } catch (Exception e) {
                logger.error("Error while pinging Redis pub/sub health monitor");
                if (this.redisPingTimeout != null) {
                    this.redisPingTimeout.cancel();
                    this.redisPingTimeout = null;
                }
                this.startPingOperation();
            }
        }
    }

    private synchronized void onPingMessage(RedisMessageCarrier message) {
        final String data = message.getData();
        if (PING_MESSAGE.equals(data)) {
            if (this.redisPingTimeout != null) {
                this.redisPingTimeout.cancel();
                this.redisPingTimeout = null;
            }

            // Start the next ping operation
            this.startPingOperation();
        }
    }

    private synchronized void onPingTimeout() {
        if (this.redisPingTimeout != null) {
            logger.warn("Redis ping timeout, reconnecting subscriber...");
            this.onDisconnected.run();

            // Reconnect to Redis pub/sub and start the ping operation again
            this.redisPingTimeout = null;
            this.subscribeAndStart();
        }
    }

    private void subscribeAndStart() {
        try {
            this.unsubscribe();
            this.subscriber = this.publisher.subscribe(CHANNEL, this::onPingMessage);

            logger.info("... subscribed to Redis pub/sub health monitor channel, starting health monitor");

            this.startPingOperation();

        } catch (Exception e) {
            if (this.isRunning) {
                logger.error("Error while subscribing Redis pub/sub health monitor. Retrying in {} seconds...", CONNECTION_RETRY_SECONDS);
                Timer.setTimeout(this::subscribeAndStart, CONNECTION_RETRY_SECONDS, TimeUnit.SECONDS);
            }
        }
    }

    private void unsubscribe() {
        if (this.subscriber != null) {
            try {
                this.subscriber.unsubscribe(CHANNEL);
            } catch (Exception e) {
                // Ignore
            }
            this.subscriber = null;
        }
    }
}

