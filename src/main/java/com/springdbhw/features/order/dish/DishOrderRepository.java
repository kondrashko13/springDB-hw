package com.springdbhw.features.order.dish;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DishOrderRepository extends JpaRepository<DishOrder, Long> {
}
