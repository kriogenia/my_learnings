package dev.sotoestevez.inventoryservice.web.model

import java.time.OffsetDateTime
import java.util.UUID

data class CoffeeInventoryDto(
    val coffeeId: UUID,
    val upc: String,
    val quantityOnHand: Int,
    val id: UUID? = null,
    val createdDate: OffsetDateTime? = null,
    val lastModifiedDate: OffsetDateTime? = null
)