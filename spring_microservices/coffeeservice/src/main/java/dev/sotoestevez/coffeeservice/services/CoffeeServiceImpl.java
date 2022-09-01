package dev.sotoestevez.coffeeservice.services;

import dev.sotoestevez.coffeeservice.domain.Coffee;
import dev.sotoestevez.coffeeservice.repositories.CoffeeRepository;
import dev.sotoestevez.coffeeservice.web.controller.NotFoundException;
import dev.sotoestevez.coffeeservice.web.mappers.CoffeeMapper;
import dev.sotoestevez.coffeeservice.web.model.CoffeeBody;
import dev.sotoestevez.coffeeservice.web.model.CoffeeDto;
import dev.sotoestevez.coffeeservice.web.model.CoffeePagedList;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CoffeeServiceImpl implements CoffeeService {
    private final CoffeeRepository coffeeRepository;
    private final CoffeeMapper coffeeMapper;

    @Override
    public CoffeeDto getById(UUID id, Boolean showQuantityOnHand) {
        return getMapper(showQuantityOnHand).apply(
                coffeeRepository.findById(id).orElseThrow(NotFoundException::new)
        );
    }

    @Override
    public CoffeeDto getByUpc(String upc, Boolean showQuantityOnHand) {
        return getMapper(showQuantityOnHand).apply(
                coffeeRepository.findByUpc(upc).orElseThrow(NotFoundException::new)
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

    @Override
    public CoffeePagedList listCoffees(String name, String body, PageRequest pageRequest,
                                       Boolean showQuantityOnHand) {
        Page<Coffee> page;
        if (StringUtils.hasText(name)) {
            if (body != null) {
                page = coffeeRepository.findCoffeeByNameAndBody(name, body, pageRequest);
            } else {
                page = coffeeRepository.findCoffeeByName(name, pageRequest);
            }
        } else {
            if (body != null) {
                page = coffeeRepository.findCoffeeByBody(body, pageRequest);
            } else {
                page = coffeeRepository.findAll(pageRequest);
            }
        }

        return new CoffeePagedList(
                page.getContent().stream().map(getMapper(showQuantityOnHand)).collect(Collectors.toList()),
                PageRequest.of(page.getPageable().getPageNumber(), page.getPageable().getPageSize()),
                page.getTotalElements()
        );
    }

    private Function<Coffee, CoffeeDto> getMapper(Boolean quantityOnHand) {
        return (quantityOnHand) ? coffeeMapper::mapWithInventory : coffeeMapper::map;
    }

}
