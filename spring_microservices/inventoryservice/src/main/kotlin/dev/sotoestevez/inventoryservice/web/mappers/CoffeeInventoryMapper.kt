package dev.sotoestevez.inventoryservice.web.mappers

import dev.sotoestevez.inventoryservice.domain.CoffeeInventory
import dev.sotoestevez.inventoryservice.web.model.CoffeeInventoryDto
import org.springframework.stereotype.Component

@Component
class CoffeeInventoryMapper(val dateMapper: DateMapper) {

    fun map(toMap: CoffeeInventory): CoffeeInventoryDto {
        return CoffeeInventoryDto(
            coffeeId = toMap.coffeeId,
            upc = toMap.upc,
            quantityOnHand = toMap.quantityOnHand,
            id = toMap.id,
            createdDate = dateMapper.map(toMap.createdDate),
            lastModifiedDate = dateMapper.map(toMap.lastModifiedDate)
        )
    }

    fun map(toMap: CoffeeInventoryDto): CoffeeInventory {
        return CoffeeInventory(
            coffeeId = toMap.coffeeId,
            upc = toMap.upc,
            quantityOnHand = toMap.quantityOnHand
        ).apply {
            id = toMap.id
            toMap.createdDate?.let { createdDate = dateMapper.map(toMap.createdDate) }
            toMap.lastModifiedDate?.let { lastModifiedDate = dateMapper.map(toMap.lastModifiedDate) }
        }
    }

}