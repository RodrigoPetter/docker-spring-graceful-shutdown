package com.example.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
@EnableScheduling
public class SchedulingConfiguration implements SchedulingConfigurer {

    @Value("${spring.lifecycle.timeout-per-shutdown-phase:20s}")
    private String shutdownTimeout;

    private static final int POOL_SIZE = 4;

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        taskRegistrar.setScheduler(taskScheduler());
    }

    @Bean
    public ThreadPoolTaskScheduler taskScheduler() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setThreadNamePrefix("custom-scheduler");
        taskScheduler.setAwaitTerminationSeconds(Integer.parseInt(shutdownTimeout.replaceAll("\\D+", "")));
        taskScheduler.setWaitForTasksToCompleteOnShutdown(true);
        taskScheduler.setPoolSize(POOL_SIZE);
        return taskScheduler;
    }
}
