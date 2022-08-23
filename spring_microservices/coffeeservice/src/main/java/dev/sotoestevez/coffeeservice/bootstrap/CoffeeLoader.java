package dev.sotoestevez.coffeeservice.bootstrap;

import dev.sotoestevez.coffeeservice.domain.Coffee;
import dev.sotoestevez.coffeeservice.repositories.CoffeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
public class CoffeeLoader implements CommandLineRunner {

    private final CoffeeRepository repository;

    @Override
    public void run(String... args) {
        var faker = new Faker();
        for (int i = 0; i < 10; i++) {
            var coffee = faker.coffee();
            var item = Coffee.builder()
                    .name(coffee.blendName())
                    .body(Objects.equals(coffee.body(), "tea-like") ? "FULL" : coffee.body().toUpperCase())
                    .country(coffee.country())
                    .variety(coffee.variety())
                    .upc(faker.numerify("##########"))
                    .price(new BigDecimal(faker.numerify("#.##")))
                    .minOnHand(0)
                    .quantityToBuild(0)
                    .build();
            repository.save(item);
        }

        repository.findAll().forEach(i -> log.info(i.toString()));
    }

}
