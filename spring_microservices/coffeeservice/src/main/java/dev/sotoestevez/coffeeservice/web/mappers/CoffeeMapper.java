package dev.sotoestevez.coffeeservice.web.mappers;

import dev.sotoestevez.coffeeservice.domain.Coffee;
import dev.sotoestevez.coffeeservice.web.model.CoffeeDto;
import org.mapstruct.DecoratedWith;
import org.mapstruct.Mapper;

@Mapper(uses = {DateMapper.class})
@DecoratedWith(CoffeeMapperDecorator.class)
public interface CoffeeMapper {
    CoffeeDto map(Coffee coffee);
    CoffeeDto mapWithInventory(Coffee coffee);
    Coffee map(CoffeeDto item);

}
