package dev.sotoestevez.orderservice.web.model

import java.time.OffsetDateTime
import java.util.*

class CustomerDto(
    val name: String,
    id: UUID? = null,
    version: Long? = null,
    createdDate: OffsetDateTime? = null,
    lastModifiedDate: OffsetDateTime? = null
) : BaseItem(id, version, createdDate, lastModifiedDate)