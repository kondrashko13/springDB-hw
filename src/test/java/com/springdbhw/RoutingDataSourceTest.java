package com.springdbhw;

import com.springdbhw.datasource.ReplicaAwareRoutingDataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RoutingDataSourceTest {
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
