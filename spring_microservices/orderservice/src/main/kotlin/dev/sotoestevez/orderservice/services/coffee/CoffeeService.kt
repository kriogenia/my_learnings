package dev.sotoestevez.orderservice.services.coffee

import dev.sotoestevez.orderservice.services.coffee.model.CoffeeDto

interface CoffeeService {

    fun getCoffeeByUpc(upc: String): CoffeeDto

}