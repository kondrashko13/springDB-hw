package com.springdbhw.features.order.dish;

import com.springdbhw.features.order.Order;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "dish_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class DishOrder extends Order {

    @NotNull
    @Positive
    private Integer weight;

    @NotNull
    @Min(0)
    @Max(5)
    private Integer spiciness;
}
