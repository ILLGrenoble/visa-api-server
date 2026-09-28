package eu.ill.visa.vdi.business.services;


import eu.ill.visa.vdi.VirtualDesktopConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Singleton
public class DesktopExecutorService {

    private final ExecutorService executor;

    @Inject
    public DesktopExecutorService(final VirtualDesktopConfiguration configuration) {
        this.executor = Executors.newFixedThreadPool(configuration.numberOfWsThreads(), Thread.ofPlatform().name("vdi-ws-thread-", 0).factory());
    }

    public <T> CompletableFuture<T> runAsync(Supplier<T> task) throws CompletionException {
        return CompletableFuture.supplyAsync(task, this.executor);
    }

    public CompletableFuture<Void> runAsync(Runnable task) throws CompletionException {
        return CompletableFuture.runAsync(task, this.executor);
    }

}
