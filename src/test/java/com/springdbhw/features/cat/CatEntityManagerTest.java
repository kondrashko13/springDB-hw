package com.springdbhw.features.cat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@Import(CatEntityManagerDao.class)
public class CatEntityManagerTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest")
                    .withDatabaseName("test_db")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void registerDatasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private CatEntityManagerDao dao;

    @Autowired
    private EntityManager entityManager;

    @Test
    void persistThenFindReturnsEntity() {
        Cat c = new Cat();
        c.setName("Orange");
        c.setAge(11);

        dao.persist(c);
        entityManager.flush();
        entityManager.clear();

        Cat found = dao.find(c.getId());

        assertNotNull(found);
        assertEquals("Orange", found.getName());
    }

    @Test
    void mergeUpdatesDetachedEntity() {
        Cat c = new Cat();
        c.setName("Snowball");
        c.setAge(3);
        dao.persist(c);

        entityManager.flush();
        entityManager.clear();

        c.setAge(4);
        Cat merged = dao.merge(c);

        entityManager.flush();
        entityManager.clear();

        Cat updated = dao.find(merged.getId());

        assertEquals(4, updated.getAge());
    }

    @Test
    void detachPreventsUpdate() {
        Cat c = new Cat();
        c.setName("Babyboy");
        c.setAge(0);
        dao.persist(c);

        dao.detach(c);
        c.setAge(1);

        entityManager.flush();
        entityManager.clear();

        Cat fromDb = dao.find(c.getId());

        assertEquals(0, fromDb.getAge());
    }

    @Test
    void refreshDiscardsLocalChanges() {
        Cat c = new Cat();
        c.setName("Meowser");
        c.setAge(10);
        dao.persist(c);

        c.setAge(11);
        dao.refresh(c);

        assertEquals(10, c.getAge());
    }

    @Test
    void removeDeletesEntity() {
        Cat c = new Cat();
        c.setName("Crow");
        c.setAge(16);
        dao.persist(c);

        Long id = c.getId();
        dao.remove(id);

        entityManager.flush();
        entityManager.clear();

        Cat deleted = dao.find(id);

        assertNull(deleted);
    }
}
