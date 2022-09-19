package dev.sotoestevez.orderservice.web.mappers

import dev.sotoestevez.orderservice.domain.CoffeeOrderLine
import dev.sotoestevez.orderservice.services.coffee.CoffeeService
import dev.sotoestevez.orderservice.web.model.CoffeeOrderLineDto
import org.springframework.stereotype.Component

@Component
class CoffeeOrderLineMapper(
    private val dateMapper: DateMapper,
    private val coffeeService: CoffeeService,
) {

    fun map(orderLine: CoffeeOrderLine): CoffeeOrderLineDto {
        val coffeeData = coffeeService.getCoffeeByUpc(orderLine.upc!!)
        return CoffeeOrderLineDto(
            coffeeId = coffeeData.id,
            coffeeName = coffeeData.name,
            orderQuantity = orderLine.orderQuantity,
            id = orderLine.id,
            upc = orderLine.upc,
            version = orderLine.version,
            createdDate = dateMapper.map(orderLine.createdDate),
            lastModifiedDate = dateMapper.map(orderLine.lastModifiedDate)
        )
    }

    fun map(dto: CoffeeOrderLineDto): CoffeeOrderLine {
        return CoffeeOrderLine(
            upc = dto.upc,
            orderQuantity = dto.orderQuantity,
            allocatedQuantity = 0
        )
    }

}