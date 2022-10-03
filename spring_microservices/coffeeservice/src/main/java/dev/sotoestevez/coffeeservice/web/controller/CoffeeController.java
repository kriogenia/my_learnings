package dev.sotoestevez.coffeeservice.web.controller;

import dev.sotoestevez.coffeeservice.services.CoffeeService;
import dev.sotoestevez.coffeeservice.web.controller.params.GetCoffeeRequestParams;
import dev.sotoestevez.coffeeservice.web.controller.params.ListCoffeeRequestParams;
import dev.sotoestevez.coffeeservice.web.model.CoffeeDto;
import dev.sotoestevez.coffeeservice.web.model.CoffeePagedList;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequiredArgsConstructor
@RequestMapping(CoffeeController.PATH)
@RestController
public class CoffeeController {

    public static final String PATH = "/api/v1/coffee";

    private final CoffeeService coffeeService;

    @GetMapping(value = "/{id}")
    @Cacheable(cacheNames = "coffee-id", key = "#id", condition = "#params.quantityOnHand == false")
    @ResponseStatus(HttpStatus.OK)
    public CoffeeDto getCoffeeById(@PathVariable UUID id, GetCoffeeRequestParams params) {
        return coffeeService.getById(id, params.getQuantityOnHand());
    }

    @GetMapping(value = "/upc/{upc}")
    @Cacheable(cacheNames = "coffee-upc", key = "#upc", condition = "#params.quantityOnHand == false")
    @ResponseStatus(HttpStatus.OK)
    public CoffeeDto getCoffeeByUpc(@PathVariable String upc, GetCoffeeRequestParams params) {
        return coffeeService.getByUpc(upc, params.getQuantityOnHand());
    }

    @PostMapping
    public ResponseEntity<?> saveNewCoffee(@RequestBody @Validated CoffeeDto dto) {
        CoffeeDto coffee = coffeeService.saveNewCoffee(dto);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Location", PATH + "/" + coffee.id());

        return new ResponseEntity<>(coffee, headers, HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCoffeeById(@PathVariable("id") UUID id,
                                            @RequestBody @Validated CoffeeDto dto) {
        return new ResponseEntity<>(coffeeService.updateCoffee(id, dto), HttpStatus.NO_CONTENT);
    }

    @GetMapping(produces = { "application/json" })
    @Cacheable(cacheNames = "coffeeList", condition = "#params.quantityOnHand = false")
    @ResponseStatus(HttpStatus.OK)
    public CoffeePagedList listCoffees(ListCoffeeRequestParams params) {
        CoffeePagedList pagedList = coffeeService.listCoffees(params.getName(), params.getBody(),
                PageRequest.of(params.getPageNumber(), params.getPageSize()), params.getQuantityOnHand());

        return pagedList;
    }


}