package dev.sotoestevez.orderservice.domain

import org.hibernate.annotations.Type
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.ManyToOne

@Entity
class CoffeeOrderLine(

    val orderQuantity: Int,
    val allocatedQuantity: Int,
    val upc: String? = null,

    @ManyToOne
    var order:CoffeeOrder? = null,
) : BaseEntity()