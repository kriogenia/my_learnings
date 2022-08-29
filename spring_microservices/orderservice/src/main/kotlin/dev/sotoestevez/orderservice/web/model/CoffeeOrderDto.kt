package dev.sotoestevez.orderservice.web.model

import dev.sotoestevez.orderservice.domain.OrderStatus
import java.time.OffsetDateTime
import java.util.*

class CoffeeOrderDto(
    val customerId: UUID,
    val orderLines: List<CoffeeOrderLineDto>,
    val customerRef: String,
    val status: OrderStatus = OrderStatus.NEW,
    val statusCallbackUrl: String? = null,
    id: UUID? = null,
    version: Long? = null,
    createdDate: OffsetDateTime? = null,
    lastModifiedDate: OffsetDateTime? = null
): BaseItem(id, version, createdDate, lastModifiedDate)
