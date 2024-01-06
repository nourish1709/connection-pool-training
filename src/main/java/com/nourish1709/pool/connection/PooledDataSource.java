package com.nourish1709.pool.connection;

import com.nourish1709.pool.connection.exception.ConnectionTimeoutException;
import com.nourish1709.pool.connection.exception.DaoException;
import com.nourish1709.pool.connection.exception.MethodNotImplementedException;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class PooledDataSource implements DataSource {

    private static final int DEFAULT_POOL_SIZE = 10;

    private final BlockingQueue<Connection> connectionPool;

    private final String user;
    private final String password;

    public PooledDataSource(String url, String user, String password) {
        this.connectionPool = new ArrayBlockingQueue<>(DEFAULT_POOL_SIZE);
        this.user = user;
        this.password = password;

        for (int i = 0; i < DEFAULT_POOL_SIZE; i++) {
            try {
                this.connectionPool.add(
                        new NonCloseableConnection(DriverManager.getConnection(url, user, password), this.connectionPool));
            } catch (SQLException e) {
                throw new DaoException("Failed to get connection to %s for user %s".formatted(url, user), e);
            }
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return Optional.ofNullable(connectionPool.poll(5, TimeUnit.SECONDS))
                    .orElseThrow(() -> new ConnectionTimeoutException("Timeout while waiting for a connection from the connection pool"));
        } catch (InterruptedException e) {
            throw new DaoException("Thread interrupted while waiting for the connection", e);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        if (this.user.equals(username) && this.password.equals(password))
            return getConnection();

        throw new MethodNotImplementedException("getConnection by new username and password is not supported yet. Use getConnection() instead.");
    }

    @Override
    public PrintWriter getLogWriter() {
        throw new MethodNotImplementedException();
    }

    @Override
    public void setLogWriter(PrintWriter out) {
        throw new MethodNotImplementedException();
    }

    @Override
    public void setLoginTimeout(int seconds) {
        throw new MethodNotImplementedException();
    }

    @Override
    public int getLoginTimeout() {
        throw new MethodNotImplementedException();
    }

    @Override
    public Logger getParentLogger() {
        throw new MethodNotImplementedException();
    }

    @Override
    public <T> T unwrap(Class<T> iface) {
        throw new MethodNotImplementedException();
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) {
        throw new MethodNotImplementedException();
    }
}
