package dev.sotoestevez.orderservice.web.mappers

import dev.sotoestevez.orderservice.domain.CoffeeOrderLine
import dev.sotoestevez.orderservice.web.model.CoffeeOrderLineDto
import org.springframework.stereotype.Component

@Component
class CoffeeOrderLineMapper(val dateMapper: DateMapper) {

    fun map(orderLine: CoffeeOrderLine): CoffeeOrderLineDto {
        return CoffeeOrderLineDto(
            coffeeId = orderLine.coffeeId,
            orderQuantity = orderLine.orderQuantity,
            id = orderLine.id,
            version = orderLine.version,
            createdDate = dateMapper.map(orderLine.createdDate!!),
            lastModifiedDate = dateMapper.map(orderLine.lastModifiedDate!!)
        )
    }

    fun map(dto: CoffeeOrderLineDto): CoffeeOrderLine {
        return CoffeeOrderLine(
            coffeeId = dto.coffeeId,
            orderQuantity = dto.orderQuantity,
            allocatedQuantity = 0
        )
    }

}