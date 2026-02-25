package com.springdbhw.features.cat;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class CatService{

    private final CatRepository catRepository;

    @Transactional
    public void createCat(Cat cat) throws InterruptedException {
        Thread.sleep(1000);
        catRepository.save(cat);
    }

    @Transactional
    public void delete(Cat cat) {
        catRepository.delete(cat);
    }

    @Transactional(readOnly = true)
    public List<Cat> getAll() {
        return catRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Cat getCat(Long catId) {
        return catRepository.findById(catId).orElseThrow();
    }
}