package com.radman.shop;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Slf4j
public abstract class AbstractContainerBaseTest {

    @LocalServerPort
    protected int port;

    protected RestTestClient client;

    @Container
    @ServiceConnection
    static final PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("bill")
                    .withUsername("bill")
                    .withPassword("password");

    @BeforeAll
    static void logContainerInfo() {
        log.info("=== Testcontainers PostgreSQL ===");
        log.info("JDBC URL: {}", postgres.getJdbcUrl());
        log.info("Database: {}", postgres.getDatabaseName());
        log.info("Username: {}", postgres.getUsername());
        log.info("=================================");
    }
}