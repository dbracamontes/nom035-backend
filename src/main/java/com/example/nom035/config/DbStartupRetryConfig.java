package com.example.nom035.config;

import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Configuration that creates a DataSource bean which waits for the database
 * to become reachable before returning the DataSource. This avoids early
 * application failures when the database container is still starting.
 */
@Configuration
public class DbStartupRetryConfig {

    @Value("${db.startup.timeout:60}")
    private int timeoutSeconds;

    @Value("${db.startup.retry-interval:1}")
    private int retryIntervalSeconds;

    @Bean
    @Primary
    public DataSource dataSource(DataSourceProperties properties) {
        HikariDataSource ds = properties.initializeDataSourceBuilder()
                .type(HikariDataSource.class)
                .build();

        long deadline = System.currentTimeMillis() + timeoutSeconds * 1000L;

        while (true) {
            try (Connection c = ds.getConnection()) {
                // Success: DB is reachable, return configured DataSource
                return ds;
            } catch (SQLException e) {
                if (System.currentTimeMillis() > deadline) {
                    throw new IllegalStateException("Timed out waiting for database after " + timeoutSeconds + " seconds", e);
                }
                try {
                    Thread.sleep(retryIntervalSeconds * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException("Interrupted while waiting for database", ie);
                }
            }
        }
    }
}
