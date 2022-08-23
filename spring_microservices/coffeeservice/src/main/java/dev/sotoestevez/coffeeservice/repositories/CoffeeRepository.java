package dev.sotoestevez.coffeeservice.repositories;

import dev.sotoestevez.coffeeservice.domain.Coffee;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.UUID;

public interface CoffeeRepository extends PagingAndSortingRepository<Coffee, UUID> { }
