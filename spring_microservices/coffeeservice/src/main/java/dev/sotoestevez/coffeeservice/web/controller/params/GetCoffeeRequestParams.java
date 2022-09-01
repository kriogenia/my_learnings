package dev.sotoestevez.coffeeservice.web.controller.params;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@NoArgsConstructor
@Getter
@Setter
public class GetCoffeeRequestParams {
    private UUID id;
    private Boolean quantityOnHand = false;
}
