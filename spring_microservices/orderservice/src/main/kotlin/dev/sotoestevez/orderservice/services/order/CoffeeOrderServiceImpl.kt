package dev.sotoestevez.orderservice.services.order

import dev.sotoestevez.orderservice.domain.CoffeeOrder
import dev.sotoestevez.orderservice.domain.Customer
import dev.sotoestevez.orderservice.domain.OrderStatus
import dev.sotoestevez.orderservice.repositories.CoffeeOrderRepository
import dev.sotoestevez.orderservice.repositories.CustomerRepository
import dev.sotoestevez.orderservice.services.coffee.CoffeeService
import dev.sotoestevez.orderservice.web.mappers.CoffeeOrderMapper
import dev.sotoestevez.orderservice.web.model.CoffeeOrderDto
import dev.sotoestevez.orderservice.web.model.CoffeeOrderPagedList
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.lang.RuntimeException
import java.util.*
import java.util.stream.Collectors


@Service
class CoffeeOrderServiceImpl(
    private val orderRepository: CoffeeOrderRepository,
    private val customerRepository: CustomerRepository,
    private val coffeeOrderMapper: CoffeeOrderMapper,
    //private val publisher: ApplicationEventPublisher
) : CoffeeOrderService {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun listOrders(customerId: UUID, pageable: Pageable): CoffeeOrderPagedList {
        val customerOptional = customerRepository.findById(customerId)

        return if (customerOptional.isPresent) {
            val orderPage: Page<CoffeeOrder> = orderRepository.findAllByCustomer(customerOptional.get(), pageable)

            CoffeeOrderPagedList(
                orderPage.stream().map(coffeeOrderMapper::map).collect(Collectors.toList()),
                PageRequest.of(orderPage.pageable.pageNumber, orderPage.pageable.pageSize),
                orderPage.totalElements
            )
        } else {
            CoffeeOrderPagedList(mutableListOf())
        }
    }

    @Transactional
    override fun placeOrder(customerId: UUID, coffeeOrderDto: CoffeeOrderDto): CoffeeOrderDto {
        val customer = getCustomer(customerId)

        var order = coffeeOrderMapper.map(coffeeOrderDto)
        order.id = null
        order.customer = customer
        order.orderStatus = OrderStatus.NEW
        order.orderLines.forEach { it.order = order }

        val savedOrder = orderRepository.save(order)
        log.info("Saved order ${savedOrder.id}")

        return coffeeOrderMapper.map(savedOrder)
    }

    override fun getOrderById(customerId: UUID, orderId: UUID): CoffeeOrderDto {
        return coffeeOrderMapper.map(getOrder(customerId, orderId))
    }

    override fun pickUpOrder(customerId: UUID, orderId: UUID) {
        var order = getOrder(customerId, orderId)
        order.orderStatus = OrderStatus.PICKED_UP
        orderRepository.save(order)
    }

    private fun getOrder(customerId: UUID, orderId: UUID): CoffeeOrder {
        val customer = getCustomer(customerId)
        val order = orderRepository.findById(orderId).orElseThrow {
            RuntimeException("Order not found")
        }
        return if (order.customer!! == customer) { order } else throw RuntimeException("Order not found")
    }

    private fun getCustomer(customerId: UUID): Customer {
        return customerRepository.findById(customerId).orElseThrow {
            RuntimeException("Customer not found")
        }
    }

}