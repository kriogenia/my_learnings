package dev.sotoestevez.orderservice.web.model

import dev.sotoestevez.orderservice.domain.OrderStatus
import java.time.OffsetDateTime
import java.util.*

class OrderStatusUpdate(
    val orderId: UUID,
    val customerRef: String,
    val status: OrderStatus,
    id: UUID? = null,
    version: Long? = null,
    createdDate: OffsetDateTime? = null,
    lastModifiedDate: OffsetDateTime? = null
) : BaseItem(id, version, createdDate, lastModifiedDate)