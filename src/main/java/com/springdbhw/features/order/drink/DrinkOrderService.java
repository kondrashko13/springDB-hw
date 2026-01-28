package com.springdbhw.features.order.drink;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DrinkOrderService {

    private final DrinkOrderRepository drinkOrderRepository;

    @Transactional
    public void createDrinkOrder(DrinkOrder order) {
        drinkOrderRepository.save(order);
    }

    @Transactional
    public List<DrinkOrder> getAllDrinkOrders() {
        return drinkOrderRepository.findAll();
    }
}