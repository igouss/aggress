package com.naxsoft.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;


public class Scheduler {
    private final static Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private final ScheduledExecutorService scheduler;
    private final Set<ScheduledFuture<?>> tasks;

    public Scheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        tasks = new HashSet<>();
    }

    public void add(Runnable command, int initialDelay, int period, TimeUnit unit) {
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
        tasks.add(scheduledFuture);

        // Monitor the scheduled future completion
        Mono.fromCallable(() -> {
                    try {
                        scheduledFuture.get(); // Wait for completion
                        return scheduledFuture;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe(
                        v -> tasks.remove(scheduledFuture),
                        e -> LOGGER.error("Scheduling error", e),
                        () -> LOGGER.info("Scheduling action completed")
                );
    }

    public void stop() {
        for (ScheduledFuture<?> task : tasks) {
            scheduler.schedule((Runnable) () -> task.cancel(true), 0, SECONDS);
        }
    }
}
