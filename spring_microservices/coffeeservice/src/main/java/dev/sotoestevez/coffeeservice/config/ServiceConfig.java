package dev.sotoestevez.coffeeservice.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ServiceConfig {

    @Bean
    @ConfigurationProperties(prefix = "dev.sotoestevez.inventory")
    public Data inventoryServiceData() {
        return new Data();
    }

    @Getter
    @Setter
    public class Data {
        private String host;
        private String user;
        private String password;
    }

}
