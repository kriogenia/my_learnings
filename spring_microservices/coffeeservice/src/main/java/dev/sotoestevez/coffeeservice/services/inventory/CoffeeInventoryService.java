package dev.sotoestevez.coffeeservice.services.inventory;

import java.util.UUID;

public interface CoffeeInventoryService {

    Integer getOnHandInventory(UUID coffeeId);

}
