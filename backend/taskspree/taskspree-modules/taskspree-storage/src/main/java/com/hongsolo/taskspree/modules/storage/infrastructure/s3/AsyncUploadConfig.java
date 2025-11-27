package com.hongsolo.taskspree.modules.storage.infrastructure.s3;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Slf4j
@Configuration
@EnableAsync
@EnableRetry
public class AsyncUploadConfig {

    /**
     * Thread pool for async file uploads
     */
    @Bean(name = "uploadExecutor")
    public Executor uploadExecutor() {
        log.info("Configuring upload thread pool executor");

        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Core pool size - threads to keep alive even when idle
        executor.setCorePoolSize(4);

        // Max pool size - maximum number of threads
        executor.setMaxPoolSize(8);

        // Queue capacity - tasks waiting when all threads are busy
        executor.setQueueCapacity(50);

        // Thread name prefix for debugging
        executor.setThreadNamePrefix("upload-");

        // Wait for tasks to complete on shutdown
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);

        // Rejection policy - run in caller's thread if pool is full
        executor.setRejectedExecutionHandler((r, e) -> {
            log.warn("Upload thread pool is full, running task in caller's thread");
            if (!e.isShutdown()) {
                r.run();
            }
        });

        executor.initialize();

        log.info("Upload executor configured: corePoolSize={}, maxPoolSize={}, queueCapacity={}",
                executor.getCorePoolSize(), executor.getMaxPoolSize(), executor.getQueueCapacity());

        return executor;
    }
}
