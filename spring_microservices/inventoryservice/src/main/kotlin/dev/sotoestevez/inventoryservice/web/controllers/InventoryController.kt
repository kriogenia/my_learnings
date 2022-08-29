package dev.sotoestevez.inventoryservice.web.controllers

import dev.sotoestevez.inventoryservice.repositories.InventoryRepository
import dev.sotoestevez.inventoryservice.web.mappers.CoffeeInventoryMapper
import dev.sotoestevez.inventoryservice.web.model.CoffeeInventoryDto
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import java.util.UUID

@RequestMapping("api/v1/coffee/{coffeeId}/inventory")
@RestController
class InventoryController(
    val inventoryRepository: InventoryRepository,
    val coffeeInventoryMapper: CoffeeInventoryMapper
) {

    private val log = LoggerFactory.getLogger(javaClass)

    @GetMapping
    fun listCoffeesById(@PathVariable("coffeeId") coffeeId: UUID): List<CoffeeInventoryDto> {
        log.debug("Finding inventory for coffee $coffeeId")

        return inventoryRepository.findAllByCoffeeId(coffeeId).map(coffeeInventoryMapper::map)
    }

}