package com.springdbhw.features.order.drink;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DrinkOrderRepository extends JpaRepository<DrinkOrder, Long> {
}
