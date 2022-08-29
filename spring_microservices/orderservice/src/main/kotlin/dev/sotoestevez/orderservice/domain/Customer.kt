package dev.sotoestevez.orderservice.domain

import java.sql.Timestamp
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.OneToMany

@Entity
class Customer(

    val name: String,

    @Column(length = 36, columnDefinition = "varchar")
    val apiKey: UUID,

    @OneToMany(mappedBy = "customer")
    val orders: Set<CoffeeOrder>,

) : BaseEntity()