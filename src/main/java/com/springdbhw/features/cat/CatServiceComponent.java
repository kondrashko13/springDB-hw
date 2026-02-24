package com.springdbhw.features.cat;

import com.springdbhw.features.owner.Owner;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceContext;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@AllArgsConstructor
public class CatServiceComponent {

    @PersistenceContext
    private EntityManager em;

    private final TransactionTemplate txTemplate;

    private final EntityManagerFactory emf;

    @Transactional
    public Long saveOwner(String name) {
        Owner o = new Owner();
        o.setName(name);
        em.persist(o);
        return o.getId();
    }

    @Transactional
    public Long saveCat(String name) {
        Cat c = new Cat();
        c.setName(name);
        em.persist(c);
        return c.getId();
    }

    @Transactional
    public void connect(Long ownerId, Long catId) {
        Owner o = em.find(Owner.class, ownerId);
        Cat c = em.find(Cat.class, catId);
        c.setOwner(o);
    }

    public Long saveOwnerTemplate(String name) {
        return txTemplate.execute(status -> {
            Owner o = new Owner();
            o.setName(name);
            em.persist(o);
            return o.getId();
        });
    }

    public Long registerFelineTemplate(String name) {
        return txTemplate.execute(status -> {
            Cat c = new Cat();
            c.setName(name);
            em.persist(c);
            return c.getId();
        });
    }

    public void connectTemplate(Long ownerId, Long catId) {
        txTemplate.execute(status -> {
            Owner o = em.find(Owner.class, ownerId);
            Cat c = em.find(Cat.class, catId);
            c.setOwner(o);
            return c.getId();
        });
    }

    public Long saveOwnerManually(String name) {
        EntityManager localEm = emf.createEntityManager();
        EntityTransaction tx = localEm.getTransaction();
        try (localEm) {
            tx.begin();
            Owner o = new Owner();
            o.setName(name);
            localEm.persist(o);
            tx.commit();
            return o.getId();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    public Long saveCatManually(String name) {
        EntityManager localEm = emf.createEntityManager();
        EntityTransaction tx = localEm.getTransaction();
        try(localEm) {
            tx.begin();
            Cat c = new Cat();
            c.setName(name);
            localEm.persist(c);
            tx.commit();
            return c.getId();
        } catch (Exception e) {
            tx.rollback();
            throw e;
        }
    }

    public void connectManually(Long ownerId, Long catId) {
        EntityManager localEm = emf.createEntityManager();
        EntityTransaction tx = localEm.getTransaction();
        try(localEm) {
            tx.begin();
            Owner owner = localEm.find(Owner.class, ownerId);
            Cat cat = localEm.find(Cat.class, catId);
            cat.setOwner(owner);
            localEm.merge(cat);
            tx.commit();
        } catch (Exception e) {
            tx.rollback();
        }
    }
}