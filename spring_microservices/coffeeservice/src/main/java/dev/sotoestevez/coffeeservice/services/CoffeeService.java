package dev.sotoestevez.coffeeservice.services;

import dev.sotoestevez.coffeeservice.web.model.CoffeeBody;
import dev.sotoestevez.coffeeservice.web.model.CoffeeDto;
import dev.sotoestevez.coffeeservice.web.model.CoffeePagedList;
import org.springframework.data.domain.PageRequest;

import java.util.UUID;

public interface CoffeeService {
    CoffeeDto getById(UUID id);

    CoffeeDto saveNewCoffee(CoffeeDto dto);

    CoffeeDto updateCoffee(UUID id, CoffeeDto dto);

    CoffeePagedList listCoffees(String name, String body, PageRequest pageRequest, Boolean showQuantityOnHand);
}
