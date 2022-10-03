package dev.sotoestevez.coffeeservice.web.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

public class CoffeePagedList extends PageImpl<CoffeeDto> {

    @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
    public CoffeePagedList(@JsonProperty("content") List<CoffeeDto> content,
                           @JsonProperty("number") int number,
                           @JsonProperty("size") int size,
                           @JsonProperty("totalElements") Long totalElements,
                           @JsonProperty("pageable") JsonNode pageable,
                           @JsonProperty("last") boolean last,
                           @JsonProperty("totalPages") int totalPages,
                           @JsonProperty("sort") JsonNode sort,
                           @JsonProperty("first") boolean first,
                           @JsonProperty("numberOfElements") int numberOfElements) {

        super(content, PageRequest.of(number, size), totalElements);
    }

    public CoffeePagedList(List<CoffeeDto> content, Pageable pageable, long total) {
        super(content, pageable, total);
    }

    public CoffeePagedList(List<CoffeeDto> content) {
        super(content);
    }
}
