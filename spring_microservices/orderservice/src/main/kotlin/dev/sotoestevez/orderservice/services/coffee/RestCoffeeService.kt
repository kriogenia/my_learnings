package dev.sotoestevez.orderservice.services.coffee

import dev.sotoestevez.orderservice.config.ServicesConfig
import dev.sotoestevez.orderservice.services.coffee.model.CoffeeDto
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate

@Service
@Profile("!local-discovery")
class RestCoffeeService(
    restTemplateBuilder: RestTemplateBuilder,
    data: ServicesConfig.Data
): CoffeeService {

    val coffeeByUpcPath = "/api/v1/coffee/upc/{upc}"

    private val restTemplate: RestTemplate
    private val host: String

    init {
        restTemplate = restTemplateBuilder.basicAuthentication(data.user, data.password).build()
        host = data.host
    }

    override fun getCoffeeByUpc(upc: String): CoffeeDto {
        return  restTemplate.getForEntity(host + coffeeByUpcPath, CoffeeDto::class.java, upc).body
            ?: throw RuntimeException("No coffee matches the given UPC")
    }


}