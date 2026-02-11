package com.springdbhw.feature.cat;

import com.springdbhw.features.cat.*;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;

@SpringBootTest
@Testcontainers
class AdvancedCatServiceTest {

    @Container
    static PostgreSQLContainer postgresContainer = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("testdb")
            .withUsername("user")
            .withPassword("password");

    @DynamicPropertySource
    static void setDatasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);
    }

    @Autowired
    private EntityManager em;

    @Autowired
    private AdvancedCatService catService;

    @Autowired
    private CatRepository catRepository;

    @Autowired
    private OwnerRepository ownerRepository;

    static AdvancedCatService staticCatService;

    @BeforeAll
    static void beforeAll(@Autowired AdvancedCatService catService) {
        staticCatService = catService;
    }

    private Cat aliveCat;
    private Cat deadCat;

    @BeforeEach
    void setUp() {
        catRepository.deleteAll();
        ownerRepository.deleteAll();

        Owner owner = new Owner();
        owner.setName("John");
        ownerRepository.save(owner);

        aliveCat = new Cat();
        aliveCat.setName("AliveCat");
        aliveCat.setAlive(true);
        aliveCat.setAge(2);
        aliveCat.setBreed(Breed.ORANGE);
        aliveCat.setOwner(owner);
        catRepository.save(aliveCat);

        deadCat = new Cat();
        deadCat.setName("DeadCat");
        deadCat.setAlive(false);
        deadCat.setAge(3);
        deadCat.setBreed(Breed.CUTE);
        deadCat.setOwner(owner);
        catRepository.save(deadCat);
    }

    static Stream<Runnable> deleteMethods() {
        return Stream.of(
                staticCatService::deleteDeadJPQL,
                staticCatService::deleteDeadNamedQuery,
                staticCatService::deleteDeadCriteria,
                staticCatService::deleteDeadNative,
                staticCatService::deleteDeadJooq
        );
    }

    @ParameterizedTest
    @MethodSource("deleteMethods")
    void testDeleteDeadCats(Runnable deleteMethod) {
        deleteMethod.run();

        List<Cat> cats = catRepository.findAll();
        assertThat(cats.stream().map(Cat::getId)).contains(aliveCat.getId());
        assertThat(cats.stream().map(Cat::getId)).doesNotContain(deadCat.getId());
    }

    static Stream<Runnable> updateMethods() {
        return Stream.of(
                staticCatService::upAgeIfAliveJPQL,
                staticCatService::upAgeIfAliveNamedQuery,
                staticCatService::upAgeIfAliveCriteria,
                staticCatService::upAgeIfAliveNative,
                staticCatService::upAgeIfAliveJooq
        );
    }

    @ParameterizedTest
    @MethodSource("updateMethods")
    void testUpdateAliveCats(Runnable updateMethod) {
        updateMethod.run();

        Cat updatedCat = catRepository.findById(aliveCat.getId()).orElseThrow();
        Cat deadCatCheck = catRepository.findById(deadCat.getId()).orElseThrow();

        assertThat(updatedCat.getAge()).isEqualTo(aliveCat.getAge() + 1);
        assertThat(deadCatCheck.getAge()).isEqualTo(deadCat.getAge());
    }

    @FunctionalInterface
    interface CatSearchMethod {
        List<Cat> search(String name, Integer age);
    }

    static Stream<CatSearchMethod> searchByNameMethods() {
        return java.util.stream.Stream.of(
                (name, age) -> staticCatService.findDynamicallyJPQL(name, age),
                (name, age) -> staticCatService.findDynamicallyCriteria(name, age),
                (name, age) -> staticCatService.findDynamicallyNative(name, age),
                (name, age) -> staticCatService.findDynamicallyJOOQ(name, age)
        );
    }

    @ParameterizedTest
    @MethodSource("searchByNameMethods")
    void testFindDynamically(CatSearchMethod searchMethod) {
        List<Cat> filtered = searchMethod.search("Alive", 1);
        List<Cat> all = searchMethod.search(null, null);
        assertThat(filtered.stream().map(Cat::getId)).containsExactly(aliveCat.getId());
        assertThat(filtered.stream().map(Cat::getId)).doesNotContain(deadCat.getId());
        assertThat(all.size()).isEqualTo(2);
    }

    static Stream<Supplier<List<Cat>>> findOlderThanAverageMethods() {
        return Stream.of(
                staticCatService::findOlderThanAverageJPQL,
                staticCatService::findOlderThanAverageNamedQuery,
                staticCatService::findOlderThanAverageCriteria,
                staticCatService::findOlderThanAverageNative,
                staticCatService::findOlderThanAverageJooq
        );
    }

    @ParameterizedTest
    @MethodSource("findOlderThanAverageMethods")
    void testFindOlderThanAverage(Supplier<List<Cat>> method) {
        List<Cat> result = method.get();
        assertThat(result)
                .extracting(Cat::getId)
                .containsExactly(deadCat.getId());
    }

    static Stream<Supplier<List<Object[]>>> countByBreedMethods() {
        return Stream.of(
                staticCatService::countByBreedJPQL,
                staticCatService::countByBreedNamedQuery,
                staticCatService::countByBreedCriteria,
                staticCatService::countByBreedNative,
                staticCatService::countCatsByAgeJooq
        );
    }

    @ParameterizedTest
    @MethodSource("countByBreedMethods")
    void testCountByBreed(Supplier<List<Object[]>> method) {
        List<Object[]> result = method.get();

        Map<Breed, Long> counts = result.stream()
                .collect(Collectors.toMap(
                        row -> Breed.toBreed(row[0]),
                        row -> ((Number) row[1]).longValue()
                ));

        assertThat(counts)
                .containsEntry(Breed.ORANGE, 1L)
                .containsEntry(Breed.CUTE, 1L);
    }

    static Stream<Supplier<Map<Owner, List<Cat>>>> findGroupedByOwnerMethods() {
        return Stream.of(
                staticCatService::findCatsGroupedByOwnerJPQL,
                staticCatService::findCatsGroupedByOwnerNamedQuery,
                staticCatService::findCatsGroupedByOwnerCriteria,
                staticCatService::findCatsGroupedByOwnerNative,
                staticCatService::findCatsGroupedByOwnerJOOQ
        );
    }

    @ParameterizedTest
    @MethodSource("findGroupedByOwnerMethods")
    void testFindCatsGroupedByOwner(Supplier<Map<Owner, List<Cat>>> method) {
        Map<Owner, List<Cat>> result = method.get();

        assertThat(result).hasSize(1);

        Map.Entry<Owner, List<Cat>> entry = result.entrySet().iterator().next();

        assertThat(entry.getKey().getName()).isEqualTo("John");
        assertThat(entry.getValue())
                .extracting(Cat::getId)
                .containsExactlyInAnyOrder(aliveCat.getId(), deadCat.getId());
    }


}
