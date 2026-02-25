package com.springdbhw;

import com.springdbhw.datasource.ReplicaAwareRoutingDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.testcontainers.containers.ComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Testcontainers
class RoutingDataSourceTest {

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

    private DataSource primaryMock;
    private DataSource replicaMock;
    private ReplicaAwareRoutingDataSource routingDataSource;

    @BeforeEach
    void setUp() {
        primaryMock = mock(DataSource.class);
        replicaMock = mock(DataSource.class);

        routingDataSource = new ReplicaAwareRoutingDataSource(primaryMock, replicaMock);
        routingDataSource.afterPropertiesSet();
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    void givenReadOnlyTransaction_whenReplicaFails_thenFallbackToPrimary() throws SQLException {
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(true);

        Connection mockPrimaryConnection = mock(Connection.class);

        when(replicaMock.getConnection()).thenThrow(new SQLException());
        when(primaryMock.getConnection()).thenReturn(mockPrimaryConnection);

        Connection actualConnection = routingDataSource.getConnection();

        assertEquals(mockPrimaryConnection, actualConnection);

        verify(replicaMock, times(1)).getConnection();
        verify(primaryMock, times(1)).getConnection();
    }

    @Test
    void givenWriteTransaction_whenPrimaryFails_thenThrowException() throws SQLException {
        TransactionSynchronizationManager.setCurrentTransactionReadOnly(false);

        when(primaryMock.getConnection()).thenThrow(new SQLException());

        assertThrows(SQLException.class, () -> routingDataSource.getConnection());

        verify(replicaMock, never()).getConnection();
    }
}
