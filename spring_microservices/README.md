# Spring Microservices

Little learning project oriented in the creation of different microservices to manage the distribution and serving of coffees.

## Services

| Name              | Port | Path                                  | Type |
|:------------------|:-----|:--------------------------------------|------|
| Coffee Service    | 8080 | [coffeeservice](/coffeeservice)       | REST |
| Order Service     | 8081 | [orderservice](/orderservice)         | REST |
| Inventory Service | 8082 | [inventoryservice](/inventoryservice) | REST |
| BOM               |      | [bom](/bom)                           | BOM  |

## Features
* REST APIs to create, read or update coffees and coffee orders with microservice communication to provide all the relevant data to the requests.
* BOM project to centralize and manage dependency versions, builds and enforcements of Maven for all the projects.

