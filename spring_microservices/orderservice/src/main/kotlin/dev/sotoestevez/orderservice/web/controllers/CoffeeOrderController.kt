package dev.sotoestevez.orderservice.web.controllers

import dev.sotoestevez.orderservice.services.CoffeeOrderService
import dev.sotoestevez.orderservice.web.model.CoffeeOrderDto
import dev.sotoestevez.orderservice.web.model.CoffeeOrderPagedList
import org.springframework.data.domain.PageRequest
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RequestMapping("/api/v1/customers/{customerId}/")
@RestController
class CoffeeOrderController(val orderService: CoffeeOrderService) {

    object DEFAULT {
        const val PAGE_NUMBER = 0
        const val PAGE_SIZE = 25
    }

    @GetMapping("orders")
    fun listOrders(
        @PathVariable("customerId") customerId: UUID,
        @RequestParam(value = "pageNumber") pageNumber: Int?,
        @RequestParam(value = "pageSize") pageSize: Int?
    ): CoffeeOrderPagedList {
        val page = pageNumber ?: DEFAULT.PAGE_NUMBER
        val size = pageSize ?: DEFAULT.PAGE_SIZE

        return orderService.listOrders(customerId, PageRequest.of(page, size))
    }

    @PostMapping("orders")
    @ResponseStatus(HttpStatus.CREATED)
    fun placeOrder(
        @PathVariable("customerId") customerId: UUID,
        @RequestBody coffeeOrderDto: CoffeeOrderDto
    ): CoffeeOrderDto {
        return orderService.placeOrder(customerId, coffeeOrderDto)
    }

    @GetMapping("orders/{orderId}")
    fun getOrder(
        @PathVariable("customerId") customerId: UUID,
        @PathVariable("orderId") orderId: UUID
    ): CoffeeOrderDto {
        return orderService.getOrderById(customerId, orderId)
    }

    @PatchMapping("/orders/{orderId}/pickup")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun pickupOrder(
        @PathVariable("customerId") customerId: UUID,
        @PathVariable("orderId") orderId: UUID
    ) {
        orderService.pickUpOrder(customerId, orderId)
    }

}