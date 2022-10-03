package dev.sotoestevez.orderservice.repositories

import dev.sotoestevez.orderservice.domain.CoffeeOrderLine
import org.springframework.data.repository.PagingAndSortingRepository
import java.util.UUID

interface CoffeeOrderLineRepository: PagingAndSortingRepository<CoffeeOrderLine, UUID> {
}