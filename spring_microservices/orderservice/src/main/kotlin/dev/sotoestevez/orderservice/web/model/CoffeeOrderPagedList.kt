package dev.sotoestevez.orderservice.web.model

import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable

class CoffeeOrderPagedList : PageImpl<CoffeeOrderDto> {

    constructor(content: MutableList<CoffeeOrderDto>): super(content)
    constructor(content: MutableList<CoffeeOrderDto>, pageable: Pageable, total: Long): super(content, pageable, total)
}