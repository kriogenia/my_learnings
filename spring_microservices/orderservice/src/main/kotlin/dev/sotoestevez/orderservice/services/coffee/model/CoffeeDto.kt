package dev.sotoestevez.orderservice.services.coffee.model

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.util.*

data class CoffeeDto(
    val id: UUID,
    val version: Int,
    val createdDate: OffsetDateTime,
    val lastModifiedDate: OffsetDateTime,
    val name: String,
    val body: String,
    val country: String,
    val variety: String,
    val upc: String,
    val price: BigDecimal,
    val quantityOnHand: Int?
)