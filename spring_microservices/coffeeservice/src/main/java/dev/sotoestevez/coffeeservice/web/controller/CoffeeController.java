package dev.sotoestevez.coffeeservice.web.controller;

import dev.sotoestevez.coffeeservice.service.CoffeeService;
import dev.sotoestevez.coffeeservice.web.model.CoffeeDto;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/{id}")
    public ResponseEntity<CoffeeDto> getCoffeeById(@PathVariable("id") UUID id) {
        return new ResponseEntity<>(coffeeService.getById(id), HttpStatus.OK);
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

}