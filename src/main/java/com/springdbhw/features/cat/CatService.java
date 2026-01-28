package com.springdbhw.features.cat;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatService {

    private final CatRepository catRepository;

    public CatService(CatRepository catRepository) {
        this.catRepository = catRepository;
    }

    public List<Cat> getAllCats() {
        return catRepository.findAll();
    }

    public Cat getCatById(Long id) {
        return catRepository.findById(id);
    }

    public void createCat(Cat cat) {
        catRepository.save(cat);
    }

    public void updateCat(Cat cat) {
        catRepository.update(cat);
    }

    public void deleteCat(Long id) {
        catRepository.delete(id);
    }
}