package dev.sotoestevez.orderservice.services.order

import dev.sotoestevez.orderservice.web.model.CoffeeOrderDto
import dev.sotoestevez.orderservice.web.model.CoffeeOrderPagedList
import org.springframework.data.domain.Pageable
import java.util.UUID

interface CoffeeOrderService {

    fun listOrders(customerId: UUID, pageable: Pageable): CoffeeOrderPagedList
    fun placeOrder(customerId: UUID, coffeeOrderDto: CoffeeOrderDto): CoffeeOrderDto
    fun getOrderById(customerId: UUID, orderId: UUID): CoffeeOrderDto
    fun pickUpOrder(customerId: UUID, orderId: UUID)

}