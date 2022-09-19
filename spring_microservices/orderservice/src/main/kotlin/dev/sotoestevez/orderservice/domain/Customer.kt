package dev.sotoestevez.orderservice.domain

import org.hibernate.annotations.Type
import java.sql.Timestamp
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
class Customer(

    val name: String,

    @field:Column(length = 36, columnDefinition = "varchar", updatable = false, nullable = false)
    @field:Type(type = "org.hibernate.type.UUIDCharType")
    val apiKey: UUID,

    @OneToMany(mappedBy = "customer")
    val orders: Set<CoffeeOrder>,

) : BaseEntity()