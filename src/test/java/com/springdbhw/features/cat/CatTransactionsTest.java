package com.springdbhw.features.cat;

import com.springdbhw.features.owner.Owner;
import com.springdbhw.features.owner.OwnerRuntimeException;
import com.springdbhw.features.owner.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class CatTransactionsTest {

    @Container
    private static final PostgreSQLContainer db = new PostgreSQLContainer("postgres:latest")
            .withDatabaseName("test_db")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void injectDatasourceCoordinates(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", db::getJdbcUrl);
        registry.add("spring.datasource.username", db::getUsername);
        registry.add("spring.datasource.password", db::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private CatService catService;
    @Autowired
    private OwnerService ownerService;
    @Autowired
    private CatServiceComponent catServiceComponent;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private TransactionTemplate transactionTemplate;

    @BeforeEach
    void purgeSchemas() {
        jdbcTemplate.execute("TRUNCATE TABLE cats, owners RESTART IDENTITY CASCADE");
    }


    // 1. Rollback -----------------------------------------------------------------------------------------------------
    @Test
    void shouldRollbackTransactionOnException() {
        Cat cat = Cat.builder().name("Schrodinger").build();

        assertThrows(CatCheckedException.class, () -> catService.saveCatWithRollback(cat));

        Integer numOfCats = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM cats WHERE name = 'Schrodinger'", Integer.class);
        assertThat(numOfCats).isZero();
    }

    // 2. No rollback --------------------------------------------------------------------------------------------------
    @Test
    void shouldNotRollbackTransactionOnRuntimeException() {
        Cat cat = Cat.builder().name("Einstein").build();

        assertThrows(OwnerRuntimeException.class, () -> catService.saveCatWithoutRollback(cat));

        Integer numOfCats = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM cats WHERE name = 'Einstein'", Integer.class);
        assertThat(numOfCats).isEqualTo(1);
    }

    // 3.1. No transaction inside --------------------------------------------------------------------------------------
    @Test
    void shouldNotCreateTransactionOnCallingTransactionalMethodFromInside() {
        boolean transactionExists = catService.triggerInternalTransaction();
        assertThat(transactionExists).isFalse();
    }

    // 3.2. Yes transaction inside -------------------------------------------------------------------------------------
    @Test
    void shouldCreateTransactionOnCallingExternalTransactionalMethod() {
        boolean isProtected = catService.triggerExternalTransaction();
        assertThat(isProtected).isTrue();
    }

    // 4. Serialization block ------------------------------------------------------------------------------------------
    @Test
    void shouldThrowExceptionOnConcurrentUpdateWithSerializableIsolation() throws InterruptedException {
        Long ownerId = catServiceComponent.saveOwner("Initial Owner");

        int threadCount = 2;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch doneLatch = new CountDownLatch(threadCount);
        AtomicInteger exceptionCount = new AtomicInteger(0);

        Runnable renameTask = () -> {
            try {
                startLatch.await();
                catService.renameOwner(ownerId, "New Name from " + Thread.currentThread().getName());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } catch (Exception e) { // Serialization exception
                exceptionCount.incrementAndGet();
            } finally {
                doneLatch.countDown();
            }
        };

        executor.submit(renameTask);
        executor.submit(renameTask);

        startLatch.countDown();
        boolean finishedInTime = doneLatch.await(5, TimeUnit.SECONDS);

        assertThat(finishedInTime).isTrue();
        assertThat(exceptionCount.get()).isGreaterThanOrEqualTo(1);

        executor.shutdown();
    }

    // 5. @Transactional -----------------------------------------------------------------------------------------------
    @Test
    void shouldConnectCatAndOwnerTransactional() {
        Long ownerId = catServiceComponent.saveOwner("Dave");
        Long catId = catServiceComponent.saveCat("Whiskers");

        catServiceComponent.connect(ownerId, catId);

        assertConnected(ownerId, catId);
    }

    // 6. Transaction Template -----------------------------------------------------------------------------------------
    @Test
    void shouldConnectCatAndOwnerTxTemplate() {
        Long ownerId = catServiceComponent.saveOwnerTemplate("PPaul");
        Long catId = catServiceComponent.registerFelineTemplate("Paws");

        catServiceComponent.connectTemplate(ownerId, catId);

        assertConnected(ownerId, catId);
    }

    // 7. Manual Transaction -------------------------------------------------------------------------------------------
    @Test
    void shouldConnectCatAndOwnerManual() {
        Long ownerId = catServiceComponent.saveOwnerManually("Mary");
        Long catId = catServiceComponent.saveCatManually("Claws");

        catServiceComponent.connectManually(ownerId, catId);

        assertConnected(ownerId, catId);
    }

    private void assertConnected(Long ownerId, Long catId) {
        Long savedOwnerId = jdbcTemplate.queryForObject(
                "SELECT owner_id FROM cats WHERE id = ?", Long.class, catId);
        assertThat(savedOwnerId).isEqualTo(ownerId);
    }

    // 8.1 Propagation Mandatory ----------------------------------------------------------------------------------------
    @Test
    void shouldNotExecuteWithoutExternalTransaction() {
        Owner owner = Owner.builder().name("Michel").build();

        assertThrows(IllegalTransactionStateException.class,
                () -> ownerService.saveInAnotherTransaction(owner));
    }

    // 8.2 - 8.3 Propagation Required and Requires_New -----------------------------------------------------------------
    @Test
    void shouldCreateSeparateTransactionInside() {
        Owner owner = Owner.builder().name("Tymko").build();
        Cat cat = Cat.builder().name("Zorchyk").build();

        try {
            transactionTemplate.execute(status -> {
                catService.saveTogether(cat, owner);
                status.setRollbackOnly();
                return null;
            });
        } catch (Exception ignored) {}

        Integer catCount = jdbcTemplate.queryForObject("SELECT count(*) FROM cats", Integer.class);
        Integer ownerCount = jdbcTemplate.queryForObject("SELECT count(*) FROM owners", Integer.class);

        assertThat(catCount).isZero();
        assertThat(ownerCount).isEqualTo(1);
    }
}
