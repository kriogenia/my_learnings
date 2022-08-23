package dev.sotoestevez.coffeeservice.service;

import dev.sotoestevez.coffeeservice.domain.Coffee;
import dev.sotoestevez.coffeeservice.repositories.CoffeeRepository;
import dev.sotoestevez.coffeeservice.web.controller.NotFoundException;
import dev.sotoestevez.coffeeservice.web.mappers.CoffeeMapper;
import dev.sotoestevez.coffeeservice.web.model.CoffeeDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class CoffeeServiceImpl implements CoffeeService {
    private final CoffeeRepository coffeeRepository;
    private final CoffeeMapper coffeeMapper;

    @Override
    public CoffeeDto getById(UUID id) {
        return coffeeMapper.map(
                coffeeRepository.findById(id).orElseThrow(NotFoundException::new)
        );
    }

    @Override
    public CoffeeDto saveNewCoffee(CoffeeDto dto) {
        return coffeeMapper.map(
                coffeeRepository.save(coffeeMapper.map(dto))
        );
    }

    @Override
    public CoffeeDto updateCoffee(UUID id, CoffeeDto dto) {
        Coffee coffee = coffeeRepository.findById(id).orElseThrow(NotFoundException::new);

        coffee.setName(dto.name());
        coffee.setBody(dto.body().name());
        coffee.setCountry(dto.country());
        coffee.setVariety(dto.variety());
        coffee.setPrice(dto.price());
        coffee.setUpc(dto.upc());

        return coffeeMapper.map(coffeeRepository.save(coffee));
    }
}
