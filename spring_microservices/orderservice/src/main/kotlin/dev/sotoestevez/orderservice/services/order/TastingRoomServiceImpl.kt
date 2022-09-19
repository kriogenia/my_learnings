package dev.sotoestevez.orderservice.services.order

import dev.sotoestevez.orderservice.bootstrap.TASTING_ROOM
import dev.sotoestevez.orderservice.domain.Customer
import dev.sotoestevez.orderservice.repositories.CustomerRepository
import dev.sotoestevez.orderservice.web.model.CoffeeOrderDto
import dev.sotoestevez.orderservice.web.model.CoffeeOrderLineDto
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.UUID

@Service
class TastingRoomServiceImpl(
    val customerRepository: CustomerRepository,
    //val orderRepository: CoffeeOrderRepository,
    val orderService: CoffeeOrderService
) : TastingRoomService {

    private val log = LoggerFactory.getLogger(javaClass)

    val upcs= arrayOf("9665547023", "6143870099", "8185948350")

    @Transactional
    @Scheduled(fixedRate = 2000)
    override fun placeTastingRoomOrder() {
        val customers = customerRepository.findAllByNameLike(TASTING_ROOM)

        if (customers.size == 1) {
            placeOrder(customers[0])
        } else {
            log.error("Wrong number of tasting rooms, must be only one.")
        }
    }

    private fun placeOrder(customer: Customer) {
        val upc = upcs.random()
        val orderLineDto = CoffeeOrderLineDto(
            upc = upc,
            orderQuantity = (0..6).random()
        )
        val orderLineSet = listOf(orderLineDto)

        val order = CoffeeOrderDto(
            customerId = customer.id!!,
            customerRef = UUID.randomUUID().toString(),
            orderLines = orderLineSet
        )
        orderService.placeOrder(customer.id!!, order)
    }

}