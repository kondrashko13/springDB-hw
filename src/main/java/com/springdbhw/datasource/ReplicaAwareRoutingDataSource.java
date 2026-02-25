package com.springdbhw.datasource;

import org.jspecify.annotations.NullMarked;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class ReplicaAwareRoutingDataSource extends AbstractRoutingDataSource {

    private final DataSource primaryDataSource;

    public ReplicaAwareRoutingDataSource(DataSource primary, DataSource replica) {
        this.primaryDataSource = primary;
        Map<Object, Object> dataSources = new HashMap<>();
        dataSources.put(DataSourceType.PRIMARY, primary);
        dataSources.put(DataSourceType.REPLICA, replica);

        this.setTargetDataSources(dataSources);
        this.setDefaultTargetDataSource(primary);
    }

    @Override
    protected Object determineCurrentLookupKey() {
        boolean isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
        return isReadOnly ? DataSourceType.REPLICA : DataSourceType.PRIMARY;
    }

    @Override
    @NullMarked
    public Connection getConnection() throws SQLException {
        try {
            return super.getConnection();
        } catch (SQLException e) {
            if (determineCurrentLookupKey() == DataSourceType.REPLICA) {
                return primaryDataSource.getConnection();
            }
            throw e;
        }
    }
}
