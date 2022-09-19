package dev.sotoestevez.orderservice.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class ServicesConfig {

    @Bean
    @ConfigurationProperties("dev.sotoestevez.coffee")
    fun coffeeServiceData(): Data {
        return Data()
    }

    class Data {
        lateinit var host: String
        lateinit var user: String
        lateinit var password: String
    }

}