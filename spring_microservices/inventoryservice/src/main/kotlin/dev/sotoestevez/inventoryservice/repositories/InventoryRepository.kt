package dev.sotoestevez.inventoryservice.repositories

import dev.sotoestevez.inventoryservice.domain.CoffeeInventory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface InventoryRepository: JpaRepository<CoffeeInventory, UUID> {

    fun findAllByCoffeeId(coffeeId: UUID): List<CoffeeInventory>

}