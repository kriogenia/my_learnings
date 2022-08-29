package dev.sotoestevez.inventoryservice.bootstrap

import dev.sotoestevez.inventoryservice.domain.CoffeeInventory
import dev.sotoestevez.inventoryservice.repositories.InventoryRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class InventoryBootLoader(val inventoryRepository: InventoryRepository): CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    val db = mapOf(
        "1891513014" to UUID.fromString("c7fa7e28-27e4-11ed-a261-0242ac120002"),
        "1938126311" to UUID.fromString("2b4b098e-27e5-11ed-a261-0242ac120002"),
        "3433241019" to UUID.fromString("c44cca0e-27e6-11ed-a261-0242ac120002"),
    )

    override fun run(vararg args: String?) {
        if (inventoryRepository.count() == 0L) {
            loadInventory()
        }
    }

    private fun loadInventory() {
        for ((upc, id) in db.entries) {
            val inventory = inventoryRepository.save(CoffeeInventory(id, upc, 50))
            log.info("Loaded ${inventory.id} into database")
        }
    }

}