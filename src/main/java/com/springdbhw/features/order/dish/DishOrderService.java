package com.springdbhw.features.order.dish;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DishOrderService {

    private final DishOrderRepository dishOrderRepository;

    public DishOrder create(DishOrder order) {
        return dishOrderRepository.save(order);
    }

    @Transactional
    public List<DishOrder> getAll() {
        return dishOrderRepository.findAll();
    }
}
