package dev.sotoestevez.orderservice.repositories

import dev.sotoestevez.orderservice.domain.Customer
import org.springframework.data.jpa.repository.JpaRepository
import java.util.UUID

interface CustomerRepository: JpaRepository<Customer, UUID> {

    fun findAllByNameLike(name: String): List<Customer>

}