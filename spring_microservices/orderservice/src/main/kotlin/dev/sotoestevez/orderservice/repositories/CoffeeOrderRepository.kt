package dev.sotoestevez.orderservice.repositories

import dev.sotoestevez.orderservice.domain.CoffeeOrder
import dev.sotoestevez.orderservice.domain.Customer
import dev.sotoestevez.orderservice.domain.OrderStatus
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import java.util.*
import javax.persistence.LockModeType

interface CoffeeOrderRepository: JpaRepository<CoffeeOrder, UUID> {

    fun findAllByCustomer(customer: Customer, pageable: Pageable): Page<CoffeeOrder>

    fun findAllByOrderStatus(status: OrderStatus): List<CoffeeOrder>

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findOneById(id: UUID): CoffeeOrder?

}