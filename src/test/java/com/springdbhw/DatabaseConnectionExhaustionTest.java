package com.springdbhw;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class DatabaseConnectionExhaustionTest {

    @Container
    public static ComposeContainer environment = new ComposeContainer(
            new File("src/test/resources/compose.yaml")
    )
            .withExposedService("postgres-primary", 5432)
            .withExposedService("postgres-replica", 5432)
            .waitingFor("postgres-primary", Wait.forListeningPort());

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
    @Qualifier("primaryDataSource")
    private DataSource primaryDataSource;

    // 5. Not enough DB connections
    @Test
    void testDatabaseMaxConnectionsExhaustion() {
        List<Connection> connections = new ArrayList<>();

        assertThrows(SQLTransientConnectionException.class, () -> {
            for (int i = 0; i < 20; i++) {
                connections.add(primaryDataSource.getConnection());
            }
        });

        for (Connection c : connections) {
            try { c.close(); } catch (SQLException ignored) {}
        }
    }
}
