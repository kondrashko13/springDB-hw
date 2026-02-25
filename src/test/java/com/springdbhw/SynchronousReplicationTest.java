package com.springdbhw;

import com.springdbhw.features.cat.Cat;
import com.springdbhw.features.cat.CatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.File;
import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class SynchronousReplicationTest {

    @Container
    public static ComposeContainer environment = new ComposeContainer(
            new File("src/test/resources/compose.yaml")
    )
            .withExposedService("postgres-primary", 5432)
            .withExposedService("postgres-replica", 5432)
            .waitingFor("postgres-primary", Wait.forListeningPort())
            .waitingFor("postgres-replica", Wait.forListeningPort());

    @DynamicPropertySource
    static void registerDatabaseProperties(DynamicPropertyRegistry registry) {
        String primaryHost = environment.getServiceHost("postgres-primary", 5432);
        Integer primaryPort = environment.getServicePort("postgres-primary", 5432);

        registry.add("spring.datasource.primary.jdbc-url",
                () -> String.format("jdbc:postgresql://%s:%d/mydb", primaryHost, primaryPort));

        String replicaHost = environment.getServiceHost("postgres-replica", 5432);
        Integer replicaPort = environment.getServicePort("postgres-replica", 5432);

        registry.add("spring.datasource.replica.jdbc-url",
                () -> String.format("jdbc:postgresql://%s:%d/mydb", replicaHost, replicaPort));
    }

    @Autowired
    private CatService catService;

    @Autowired
    @Qualifier("primaryDataSource")
    private DataSource primaryDataSource;

    @Autowired
    @Qualifier("replicaDataSource")
    private DataSource replicaDataSource;

    @BeforeEach
    void setUp() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(primaryDataSource);
        jdbcTemplate.execute("DELETE FROM cats");
    }

    // 1. + 3. Changing primary also changes replica
    @Test
    void testSynchronousReplication() throws Exception {
        DataSource[] sources = {primaryDataSource, replicaDataSource};

        Cat cat = Cat.builder().name("Whiskers").build();
        catService.createCat(cat);

        for(DataSource source : sources) {
            try (Connection conn = source.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM cats WHERE name = ?")) {

                ps.setString(1, "Whiskers");
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertEquals(1, rs.getInt(1));
                }
            }
        }

        catService.delete(cat);

        for(DataSource source : sources) {
            try (Connection conn = source.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT count(*) FROM cats WHERE name = ?")) {

                ps.setString(1, "Whiskers");
                try (ResultSet rs = ps.executeQuery()) {
                    rs.next();
                    assertEquals(0, rs.getInt(1));
                }
            }
        }
    }
}