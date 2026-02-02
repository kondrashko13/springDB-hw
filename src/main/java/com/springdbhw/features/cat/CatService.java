package com.springdbhw.features.cat;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CatService {

    private final CatEntityManagerDao dao;

    public CatService(CatEntityManagerDao dao) {
        this.dao = dao;
    }

    public List<Cat> getAll() {
        return dao.findAll();
    }

    public Cat getById(Long id) {
        return dao.find(id);
    }

    public void create(Cat cat) {
        dao.persist(cat);
    }

    public void update(Cat cat) {
        dao.merge(cat);
    }

    public void delete(Long id) {
        dao.remove(id);
    }
}