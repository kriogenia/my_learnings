package dev.sotoestevez.orderservice.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.task.SimpleAsyncTaskExecutor
import org.springframework.core.task.TaskExecutor
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@EnableAsync
@Configuration
class TaskConfig {

    @Bean
    fun taskExecutor(): TaskExecutor = SimpleAsyncTaskExecutor()

}