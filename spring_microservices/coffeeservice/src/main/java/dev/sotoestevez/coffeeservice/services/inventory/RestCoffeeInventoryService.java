package dev.sotoestevez.coffeeservice.services.inventory;

import dev.sotoestevez.coffeeservice.config.ServiceConfig;
import dev.sotoestevez.coffeeservice.services.inventory.model.CoffeeInventoryDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.Name;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Profile;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
@Profile("!local-discovery")
public class RestCoffeeInventoryService implements CoffeeInventoryService {

    public static final String INVENTORY_PATH = "/api/v1/coffee/{coffeeId}/inventory";

    private final RestTemplate restTemplate;
    private final String host;

    public RestCoffeeInventoryService(RestTemplateBuilder restTemplateBuilder,
                                      @Qualifier("inventoryServiceData") ServiceConfig.Data data) {
        this.host = data.getHost();
        this.restTemplate = restTemplateBuilder.basicAuthentication(data.getUser(), data.getPassword()).build();
    }

    @Override
    public Integer getOnHandInventory(UUID coffeeId) {
        log.debug("Calling Inventory Service to get inventory of {}", coffeeId);

        ResponseEntity<List<CoffeeInventoryDto>> responseEntity = restTemplate.exchange(
                host + INVENTORY_PATH,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {},
                coffeeId);

        Integer onHand = Objects.requireNonNull(responseEntity.getBody())
                .stream()
                .mapToInt(CoffeeInventoryDto::getQuantityOnHand)
                .sum();

        return onHand;
    }
}

