package dev.sotoestevez.orderservice.bootstrap

import dev.sotoestevez.orderservice.domain.Customer
import dev.sotoestevez.orderservice.repositories.CustomerRepository
import org.slf4j.LoggerFactory
import org.springframework.boot.CommandLineRunner
import org.springframework.stereotype.Component
import java.util.*

const val TASTING_ROOM = "Tasting Room"

@Component
class CoffeeOrderBootstrap(val customerRepository: CustomerRepository): CommandLineRunner {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun run(vararg args: String?) {
        //addTastingRoomCustomer()
        customerRepository.findAll().forEach{ log.info("Added customer: ${it.id}")}
    }

    private fun addTastingRoomCustomer() {
        if (customerRepository.count() == 0L) {
            val customer = customerRepository.save(
                Customer(
                    name = TASTING_ROOM,
                    apiKey = UUID.randomUUID(),
                    orders = emptySet()
                )
            )
            log.info("Created customer ${customer.id}")
        }
    }

}