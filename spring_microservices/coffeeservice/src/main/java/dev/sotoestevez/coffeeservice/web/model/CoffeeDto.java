package dev.sotoestevez.coffeeservice.web.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Builder
public record CoffeeDto(

    @Null
    UUID id,

    @Null
    Integer version,

    @Null
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ", shape=JsonFormat.Shape.STRING)
    OffsetDateTime createdDate,

    @Null
    @JsonFormat(pattern="yyyy-MM-dd'T'HH:mm:ssZ", shape=JsonFormat.Shape.STRING)
    OffsetDateTime lastModifiedDate,

    @NotBlank
    String name,

    @NotNull
    CoffeeBody body,

    @NotBlank
    String country,

    @NotBlank
    String variety,

    @NotNull
    String upc,

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    @Positive
    @NotNull
    BigDecimal price,

    Integer quantityOnHand

) {}