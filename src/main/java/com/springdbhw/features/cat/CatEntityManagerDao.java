package com.springdbhw.features.cat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Transactional
public class CatEntityManagerDao {

    @PersistenceContext
    private EntityManager entityManager;

    public List<Cat> findAll() {
        return entityManager
                .createQuery("SELECT c FROM Cat c", Cat.class)
                .getResultList();
    }

    public Cat find(Long id) {
        return entityManager.find(Cat.class, id);
    }

    public void persist(Cat cat) {
        entityManager.persist(cat);
    }

    public Cat merge(Cat cat) {
        return entityManager.merge(cat);
    }

    public void remove(Long id) {
        Cat cat = find(id);
        if (cat != null) {
            entityManager.remove(cat);
        }
    }

    public void detach(Cat cat) {
        entityManager.detach(cat);
    }

    public void refresh(Cat cat) {
        entityManager.refresh(cat);
    }
}

