package dev.sotoestevez.orderservice.web.model

import java.time.OffsetDateTime
import java.util.*

class CoffeeOrderLineDto(
    val orderQuantity: Int = 0,
    val coffeeName: String? = null,
    val coffeeId: UUID? = null,
    val upc: String? = null,
    id: UUID? = null,
    version: Long? = null,
    createdDate: OffsetDateTime? = null,
    lastModifiedDate: OffsetDateTime? = null
): BaseItem(id, version, createdDate, lastModifiedDate)
