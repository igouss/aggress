package com.naxsoft.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.schedulers.Schedulers;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.SECONDS;

/**
 * Copyright NAXSoft 2015
 */
public class Scheduler {
    private final static Logger LOGGER = LoggerFactory.getLogger(Scheduler.class);
    private final ScheduledExecutorService scheduler;
    private Set<ScheduledFuture<?>> tasks;

    public Scheduler() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        tasks = new HashSet<>();
    }

    public void add(Runnable command, int initialDelay, int period, TimeUnit unit) {
        ScheduledFuture<?> scheduledFuture = scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);
        tasks.add(scheduledFuture);

        rx.Observable.from(scheduledFuture)
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.immediate())
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
