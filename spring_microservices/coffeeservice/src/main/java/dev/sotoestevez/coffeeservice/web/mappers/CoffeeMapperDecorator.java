package dev.sotoestevez.coffeeservice.web.mappers;

import dev.sotoestevez.coffeeservice.domain.Coffee;
import dev.sotoestevez.coffeeservice.services.inventory.CoffeeInventoryService;
import dev.sotoestevez.coffeeservice.web.model.CoffeeDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public abstract class CoffeeMapperDecorator implements CoffeeMapper {

    private CoffeeInventoryService coffeeInventoryService;
    private CoffeeMapper delegate;

    @Autowired
    public void setCoffeeInventoryService(CoffeeInventoryService coffeeInventoryService) {
        this.coffeeInventoryService = coffeeInventoryService;
    }

    @Autowired
    @Qualifier("delegate")
    public void setDelegate(CoffeeMapper delegate) {
        this.delegate = delegate;
    }

    @Override
    public CoffeeDto mapWithInventory(Coffee coffee) {
        Integer quantityOnHand = coffeeInventoryService.getOnHandInventory(coffee.getId());
        return delegate.map(coffee).withQuantityOnHand(quantityOnHand);
    }

}
