package com.springdbhw;

import com.springdbhw.features.cat.Cat;
import com.springdbhw.features.cat.CatRepository;
import com.springdbhw.features.owner.Owner;
import com.springdbhw.features.owner.OwnerRepository;
import com.springdbhw.features.vet_visit.VetVisitService;
import com.springdbhw.features.vet_visit.VetVisit;
import com.springdbhw.features.vet_visit.VetVisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest
public class MongodbQueriesTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @Autowired
    private OwnerRepository ownerRepository;
    @Autowired
    private CatRepository catRepository;
    @Autowired
    private VetVisitRepository vetVisitRepository;
    @Autowired
    private VetVisitService templateService;

    @BeforeEach
    void setUp() {
        ownerRepository.deleteAll();
        catRepository.deleteAll();
        vetVisitRepository.deleteAll();
    }

    // 5: Derived Queries ----------------------------------------------------------------------------------------------
    @Test
    void testDerivedQueries() {
        // 5.1: findByEmail
        ownerRepository.save(Owner.builder().fullName("John").email("john@mail.com").build());
        assertThat(ownerRepository.findByEmail("john@mail.com")).isPresent();

        // 5.2: deleteByOwnerId
        catRepository.save(Cat.builder().name("Barsik").ownerId("owner-1").build());
        catRepository.save(Cat.builder().name("Murzik").ownerId("owner-1").build());
        long deletedCount = catRepository.deleteByOwnerId("owner-1");
        assertThat(deletedCount).isEqualTo(2);
        assertThat(catRepository.findAll()).isEmpty();

        // 5.3: findByCatIdAndVisitDateAfter
        vetVisitRepository.save(VetVisit.builder().catId("cat-1").visitDate(LocalDate.of(2023, 1, 1)).build());
        vetVisitRepository.save(VetVisit.builder().catId("cat-1").visitDate(LocalDate.of(2023, 12, 1)).build());
        assertThat(vetVisitRepository.findByCatIdAndVisitDateAfter("cat-1", LocalDate.of(2023, 6, 1))).hasSize(1);
    }

    // 6: @Query -------------------------------------------------------------------------------------------------------
    @Test
    void testCustomQueries() {
        // 6.1: findOwnersByFullNameRegex
        ownerRepository.save(Owner.builder().fullName("Alice Smith").email("1@m.com").build());
        ownerRepository.save(Owner.builder().fullName("Bob Smith").email("2@m.com").build());
        assertThat(ownerRepository.findOwnersByFullNameRegex("smith")).hasSize(2);

        // 6.2: findCatsOlderThan
        catRepository.save(Cat.builder().name("Old Cat").age(10).build());
        catRepository.save(Cat.builder().name("Young Cat").age(2).build());
        assertThat(catRepository.findCatsOlderThan(5)).hasSize(1);

        // 6.3: deleteFreeVisits
        vetVisitRepository.save(VetVisit.builder().catId("cat-1").visitDate(LocalDate.now()).cost(0.0).build());
        vetVisitRepository.save(VetVisit.builder().catId("cat-2").visitDate(LocalDate.now()).cost(50.0).build());
        long deletedCount = vetVisitRepository.deleteFreeVisits();
        assertThat(deletedCount).isEqualTo(1);
        assertThat(vetVisitRepository.findAll()).hasSize(1);
    }

    // 7: MongoTemplate ------------------------------------------------------------------------------------------------
    @Test
    void testMongoTemplateQueries() {
        // 7.1: addNewCat (Insert)
        Cat newCat = Cat.builder().name("Template Cat").age(3).build();
        Cat savedCat = templateService.addNewCat(newCat);
        assertThat(savedCat.getId()).isNotNull();
        assertThat(catRepository.findById(savedCat.getId())).isPresent();

        // 7.2: deleteVisitsByDiagnosis (Remove)
        vetVisitRepository.save(VetVisit.builder().catId("cat-1").visitDate(LocalDate.now()).diagnosis("Fleas").build());
        vetVisitRepository.save(VetVisit.builder().catId("cat-2").visitDate(LocalDate.now()).diagnosis("Healthy").build());
        long deletedCount = templateService.deleteVisitsByDiagnosis("Fleas");
        assertThat(deletedCount).isEqualTo(1);
        assertThat(vetVisitRepository.findAll().getFirst().getDiagnosis()).isEqualTo("Healthy");

        // 7.3: findOwnersByEmailDomain (Complex Find)
        ownerRepository.save(Owner.builder().fullName("A").email("user1@gmail.com").build());
        ownerRepository.save(Owner.builder().fullName("B").email("user2@yahoo.com").build());
        assertThat(templateService.findOwnersByEmailDomain("gmail.com")).hasSize(1);
    }
}
