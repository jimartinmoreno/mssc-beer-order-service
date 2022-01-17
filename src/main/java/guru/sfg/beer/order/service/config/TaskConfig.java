package guru.sfg.beer.order.service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @EnableAsync Enables Spring's asynchronous method execution capability, similar to functionality found in Spring's  XML namespace.
 * @EnableScheduling Enables Spring's scheduled task execution capability, similar to functionality found in Spring's  XML namespace.
 * Task Configuration - enable asyc tasks
 */
@EnableScheduling
@EnableAsync
@Configuration
public class TaskConfig {
    @Bean
    TaskExecutor taskExecutor() {
        return new SimpleAsyncTaskExecutor();
    }
}
