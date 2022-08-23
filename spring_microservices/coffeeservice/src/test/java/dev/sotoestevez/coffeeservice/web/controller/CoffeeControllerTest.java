package dev.sotoestevez.coffeeservice.web.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.sotoestevez.coffeeservice.service.CoffeeService;
import dev.sotoestevez.coffeeservice.web.model.CoffeeBody;
import dev.sotoestevez.coffeeservice.web.model.CoffeeDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CoffeeController.class)
class CoffeeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    CoffeeService coffeeService;

    @Test
    void getCoffeeById() throws Exception {
        given(coffeeService.getById(any())).willReturn(getValidCoffeeDto());

        mockMvc.perform(get(CoffeeController.PATH + "/" + UUID.randomUUID()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

    }

    @Test
    void saveNewCoffee() throws Exception {
        String beerDtoJson = objectMapper.writeValueAsString(getValidCoffeeDto());

        given(coffeeService.saveNewCoffee(any())).willReturn(getValidCoffeeDto());

        mockMvc.perform(post(CoffeeController.PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(beerDtoJson))
                .andExpect(status().isCreated());
    }

    @Test
    void updateBeerById() throws Exception {
        given(coffeeService.updateCoffee(any(), any())).willReturn(getValidCoffeeDto());

        String beerDtoJson = objectMapper.writeValueAsString(getValidCoffeeDto());

        mockMvc.perform(put(CoffeeController.PATH + "/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(beerDtoJson))
                .andExpect(status().isNoContent());
    }

    CoffeeDto getValidCoffeeDto(){
        return CoffeeDto.builder()
                .name("name")
                .body(CoffeeBody.BIG)
                .variety("variety")
                .country("Spain")
                .price(new BigDecimal("2.99"))
                .upc("1234567890")
                .build();
    }
}
