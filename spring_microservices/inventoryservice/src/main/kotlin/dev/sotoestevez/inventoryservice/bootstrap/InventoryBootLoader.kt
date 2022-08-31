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
        "9665547023" to UUID.fromString("29242b7c-07b1-4b73-88a8-17b60421d764"),
        "6143870099" to UUID.fromString("bb60ad3a-0f64-46ba-bc34-d5b193a96cb1"),
        "8185948350" to UUID.fromString("213e72c8-6c6f-4d3f-9a70-f86b4e40d00a"),
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