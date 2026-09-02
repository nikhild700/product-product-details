package com.example.dao;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.example.model.Product;

@Testcontainers
public class ProductRepositoryIT {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.3.0");

    static ProductRepository repo;
    static HikariDataSource dataSource;

    @BeforeAll
    static void setup() throws Exception {
        // DO NOT call mysql.start() — Testcontainers handles it

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(mysql.getJdbcUrl());
        config.setUsername(mysql.getUsername());
        config.setPassword(mysql.getPassword());

        dataSource = new HikariDataSource(config);
        repo = new ProductRepository();
    }

    @AfterAll
    static void tearDown() {
        // DO NOT call mysql.stop() — Testcontainers handles it
        dataSource.close();
    }

    @Test
    void insertProduct_returnsGeneratedId() throws Exception {
        Product p = new Product("SKU-123", "Test Product");

        try (Connection conn = dataSource.getConnection()) {
            int id = repo.insertProduct(conn, p);
            Assertions.assertTrue(id > 0);
        }
    }
}
