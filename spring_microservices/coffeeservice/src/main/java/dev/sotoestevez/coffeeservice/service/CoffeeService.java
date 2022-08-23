package dev.sotoestevez.coffeeservice.service;

import dev.sotoestevez.coffeeservice.web.model.CoffeeDto;

import java.util.UUID;

public interface CoffeeService {
    CoffeeDto getById(UUID id);

    CoffeeDto saveNewCoffee(CoffeeDto dto);

    CoffeeDto updateCoffee(UUID id, CoffeeDto dto);
}
