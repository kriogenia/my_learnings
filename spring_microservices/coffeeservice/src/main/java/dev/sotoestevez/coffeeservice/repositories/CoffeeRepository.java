package dev.sotoestevez.coffeeservice.repositories;

import dev.sotoestevez.coffeeservice.domain.Coffee;
import dev.sotoestevez.coffeeservice.web.model.CoffeeBody;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;
import java.util.UUID;

public interface CoffeeRepository extends PagingAndSortingRepository<Coffee, UUID> {

    Optional<Coffee> findByUpc(String upc);
    Page<Coffee> findCoffeeByNameAndBody(String name, String body, PageRequest pageRequest);
    Page<Coffee> findCoffeeByName(String name, PageRequest pageRequest);
    Page<Coffee> findCoffeeByBody(String body, PageRequest pageRequest);


}
