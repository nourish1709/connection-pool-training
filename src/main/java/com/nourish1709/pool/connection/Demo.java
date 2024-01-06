package com.nourish1709.pool.connection;

import com.nourish1709.pool.connection.entity.User;
import com.nourish1709.pool.connection.exception.DaoException;

import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadLocalRandom;

public class Demo {

    private static final String INSERT_SQL = "INSERT INTO users (name) VALUES (?);";
    private static final String SELECT_SQL = "SELECT * FROM users WHERE id =?";

    public static void main(String[] args) {
        var pooledDataSource = getPooledDataSource();
        var futures = new ArrayList<CompletableFuture<?>>();

        for (int i = 0; i < 15; i++) {
            var future = CompletableFuture.supplyAsync(() -> insertUser(pooledDataSource))
                    .thenApply((id -> getUser(id, pooledDataSource)))
                    .thenAccept(System.out::println);

            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new))
                .join();
    }

    private static long insertUser(PooledDataSource pooledDataSource) {
        try (var connection = pooledDataSource.getConnection()) {
            try (var statement = connection.prepareStatement(INSERT_SQL, Statement.RETURN_GENERATED_KEYS)) {
                statement.setString(1, "name + " + ThreadLocalRandom.current().nextInt(50));

                statement.executeUpdate();
                var generatedKeys = statement.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getLong(1);
                }

                return 0L;
            }
        } catch (SQLException e) {
            throw new DaoException("Exception occurred while inserting a new user", e);
        }
    }

    private static User getUser(Long id, PooledDataSource pooledDataSource) {
        try (var connection = pooledDataSource.getConnection()) {
            try (var statement = connection.prepareStatement(SELECT_SQL)) {
                statement.setLong(1, id);
                var result = statement.executeQuery();
                if (result.next()) {
                    var name = result.getString(2);
                    return new User(id, name);
                }
                throw new DaoException("Could not find user with id: " + id);
            }
        } catch (SQLException e) {
            throw new DaoException("Exception occurred while retrieving a user by id: " + id, e);
        }
    }

    private static PooledDataSource getPooledDataSource() {
        return new PooledDataSource("jdbc:postgresql://localhost:5432/connection_pool_training", "postgres", null);
    }
}
