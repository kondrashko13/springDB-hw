package com.springdbhw.features.order;

import com.springdbhw.features.order.dish.DishOrder;
import com.springdbhw.features.order.dish.DishOrderRepository;
import com.springdbhw.features.order.drink.DrinkOrder;
import com.springdbhw.features.order.drink.DrinkOrderRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
class OrderInheritanceTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
    }

    @Autowired
    private DrinkOrderRepository drinkOrderRepository;

    @Autowired
    private DishOrderRepository dishOrderRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Test
    void createDrinkOrderAndShowAllTables() {
        DrinkOrder drinkOrder = DrinkOrder.builder()
                .name("Assortment")
                .description("Tea")
                .price(new BigDecimal("40.00"))
                .volume(300)
                .alcoholic(false)
                .build();

        drinkOrderRepository.save(drinkOrder);

        DishOrder dishOrder = DishOrder.builder()
                .name("Pasta")
                .description("Tomato sauce")
                .price(new BigDecimal("120.00"))
                .weight(400)
                .spiciness(0)
                .build();

        dishOrderRepository.save(dishOrder);

        List<Order> allOrders = orderRepository.findAll();
        assertThat(allOrders).hasSize(2);

        List<DrinkOrder> drinks = drinkOrderRepository.findAll();
        List<DishOrder> dishes = dishOrderRepository.findAll();

        assertThat(drinks).hasSize(1);
        assertThat(dishes).hasSize(1);

        System.out.println("----------------------All Orders----------------------");
        allOrders.forEach(order -> {
            System.out.println(order.getName());
        });

        System.out.println("---------------------Drink Orders---------------------");
        drinks.forEach(order -> {
            System.out.println(order.getName());
        });

        System.out.println("----------------------Dish Orders---------------------");
        dishes.forEach(order -> {
            System.out.println(order.getName());
        });
    }
}