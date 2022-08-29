package dev.sotoestevez.orderservice.domain

import java.sql.Timestamp
import java.util.*
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class CoffeeOrderLine(

    val orderQuantity: Int,
    val allocatedQuantity: Int,
    val coffeeId: UUID? = null,

    @ManyToOne
    var order: CoffeeOrder? = null,
) : BaseEntity()