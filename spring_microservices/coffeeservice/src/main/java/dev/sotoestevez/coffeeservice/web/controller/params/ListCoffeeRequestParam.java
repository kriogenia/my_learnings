package dev.sotoestevez.coffeeservice.web.controller.params;

import dev.sotoestevez.coffeeservice.web.model.CoffeeBody;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter
@Setter
public class ListCoffeeRequestParam {

    private static final Integer DEFAULT_PAGE_NUMBER = 0;
    private static final Integer DEFAULT_PAGE_SIZE = 25;

    private Integer pageNumber = DEFAULT_PAGE_NUMBER;
    private Integer pageSize = DEFAULT_PAGE_SIZE;
    private Boolean quantityOnHand = false;

    private String name;
    private String body;

}
