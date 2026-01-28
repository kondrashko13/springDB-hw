package com.springdbhw.features.order.drink;

import com.springdbhw.features.order.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "drink_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DrinkOrder extends Order {

    @NotNull
    @Positive
    private Integer volume;

    @NotNull
    private Boolean alcoholic;
}
