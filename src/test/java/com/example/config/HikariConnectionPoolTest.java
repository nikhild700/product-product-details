package com.example.config;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.*;
import org.mockito.MockedStatic;
import org.junit.jupiter.api.Test;

import java.sql.Connection;

class HikariConnectionPoolTest {

    @Test
    void testMockStaticGetConnection() throws Exception {

        Connection mockConn = mock(Connection.class);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            mocked.when(HikariConnectionPool::getConnection)
                    .thenReturn(mockConn);

            // Now any call to HikariConnectionPool.getConnection() returns mockConn
            Connection conn = HikariConnectionPool.getConnection();

            assertSame(mockConn, conn);
        }
    }
}
