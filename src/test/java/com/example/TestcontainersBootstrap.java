package com.example;

import org.testcontainers.Testcontainers;

public class TestcontainersBootstrap {
    static {
        Testcontainers.exposeHostPorts(5432); // or nothing, just force init
    }
}