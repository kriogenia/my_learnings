package dev.sotoestevez.coffeeservice.web.mappers;

import dev.sotoestevez.coffeeservice.domain.Coffee;
import dev.sotoestevez.coffeeservice.web.model.CoffeeDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {DateMapper.class})
public interface CoffeeMapper {
    CoffeeDto map(Coffee coffee);
    Coffee map(CoffeeDto item);

}
