package eu.ill.visa.vdi.business.services;


import jakarta.inject.Singleton;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Supplier;

@Singleton
public class DesktopExecutorService {

    private final ExecutorService executor = Executors.newFixedThreadPool(8, Thread.ofPlatform().name("vdi-thread-").factory());

    public <T> CompletableFuture<T> runAsync(Supplier<T> task) throws CompletionException {
        return CompletableFuture.supplyAsync(task, this.executor);
    }

    public CompletableFuture<Void> runAsync(Runnable task) throws CompletionException {
        return CompletableFuture.runAsync(task, this.executor);
    }

}
