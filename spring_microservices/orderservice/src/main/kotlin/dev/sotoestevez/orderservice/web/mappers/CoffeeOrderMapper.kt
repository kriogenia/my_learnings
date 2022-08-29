package dev.sotoestevez.orderservice.web.mappers

import dev.sotoestevez.orderservice.domain.CoffeeOrder
import dev.sotoestevez.orderservice.web.model.CoffeeOrderDto
import org.springframework.stereotype.Component

@Component
class CoffeeOrderMapper(
    val orderLineMapper: CoffeeOrderLineMapper,
    val dateMapper: DateMapper
) {

    fun map(coffeeOrder: CoffeeOrder): CoffeeOrderDto {
        return CoffeeOrderDto(
            customerId = coffeeOrder.customer!!.id!!,
            orderLines = coffeeOrder.orderLines.map(orderLineMapper::map),
            status = coffeeOrder.orderStatus,
            statusCallbackUrl = coffeeOrder.statusCallbackUrl,
            customerRef = coffeeOrder.customerRef,
            id = coffeeOrder.id,
            version = coffeeOrder.version,
            createdDate = dateMapper.map(coffeeOrder.createdDate),
            lastModifiedDate = dateMapper.map(coffeeOrder.lastModifiedDate)
        )
    }

    fun map(dto: CoffeeOrderDto): CoffeeOrder {
        return CoffeeOrder(
            customerRef = dto.customerRef,
            orderLines = dto.orderLines.map(orderLineMapper::map),
            statusCallbackUrl = dto.statusCallbackUrl,
            orderStatus = dto.status,
        ).apply {
            id = dto.id
            dto.version?.let { version = it }
            dto.createdDate?.let { createdDate = dateMapper.map(it) }
            dto.lastModifiedDate?.let { lastModifiedDate = dateMapper.map(it) }
        }
    }

}