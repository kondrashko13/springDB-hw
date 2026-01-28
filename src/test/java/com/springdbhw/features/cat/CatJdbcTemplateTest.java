package com.springdbhw.features.cat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class CatJdbcTemplateTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:latest")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");

    @DynamicPropertySource
    static void registerDatasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "none");
        registry.add("spring.sql.init.mode", () -> "always");
    }

    @Autowired
    private CatRepository catRepository;

    @Test
    void shouldReturnAllCats() {
        List<Cat> cats = catRepository.findAll();

        assertThat(cats).hasSize(3);
    }

    @Test
    void shouldFindCatById() {
        Cat cat = catRepository.findById(1L);

        assertThat(cat).isNotNull();
        assertThat(cat.getName()).isEqualTo("Whiskers");
        assertThat(cat.getAge()).isEqualTo(3);
    }

    @Test
    void shouldSaveNewCat() {
        Cat newCat = new Cat(null, "Tom", 4);

        catRepository.save(newCat);

        List<Cat> cats = catRepository.findAll();
        assertThat(cats).hasSize(4);

        assertThat(cats)
                .extracting(Cat::getName)
                .contains("Tom");
    }
}
