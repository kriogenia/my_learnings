package dev.sotoestevez.orderservice.domain

import org.hibernate.annotations.Fetch
import org.hibernate.annotations.FetchMode
import java.sql.Timestamp
import java.util.*
import javax.persistence.CascadeType
import javax.persistence.Entity
import javax.persistence.ManyToOne
import javax.persistence.OneToMany

@Entity
class CoffeeOrder(
    val customerRef: String,

    @OneToMany(mappedBy = "order", cascade = [CascadeType.ALL])
    @Fetch(FetchMode.JOIN)
    val orderLines: List<CoffeeOrderLine>,

    val statusCallbackUrl: String? = null,
    var orderStatus: OrderStatus = OrderStatus.NEW,

    @ManyToOne
    var customer: Customer? = null,
) : BaseEntity()