package dev.sotoestevez.inventoryservice.domain

import dev.sotoestevez.inventoryservice.domain.BaseEntity
import org.hibernate.annotations.Type
import java.util.UUID
import javax.persistence.Column
import javax.persistence.Entity

@Entity
class CoffeeInventory(

    @field:Column(length = 36, columnDefinition = "varchar", nullable = false)
    @field:Type(type = "org.hibernate.type.UUIDCharType")
    val coffeeId: UUID,

    val upc: String,
    val quantityOnHand: Int

): BaseEntity()