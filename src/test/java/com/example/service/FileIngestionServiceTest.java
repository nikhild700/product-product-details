package com.example.service;

import com.example.dto.CreateProductDTO;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.BufferedReader;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

public class FileIngestionServiceTest {

    @Test
    void testProcessFileMultiThread_success() throws Exception {

        // Mock ProductService
        ProductService mockProductService = mock(ProductService.class);

        // Create service
        FileIngestionService ingestionService = new FileIngestionService(mockProductService);

        // Fake file content
        String fakeFile = "A,SKU1,Desc1,10.0\n" +
                "B,SKU2,Desc2,20.0\n" +
                "C,SKU3,Desc3,30.0\n";

        BufferedReader fakeReader = new BufferedReader(new StringReader(fakeFile));

        // Mock Files.newBufferedReader
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            mockedFiles.when(() -> Files.newBufferedReader(any(Path.class)))
                    .thenReturn(fakeReader);

            // Act
            ingestionService.processFileMultiThread("mock/mock.txt");

            // Allow executor to run tasks
            Thread.sleep(200);

            // Assert: insertBatch should be called once with a chunk of size 3
            verify(mockProductService, times(1))
                    .insertBatch(argThat(chunk -> chunk.size() == 3));
        }
    }

        @Test
    void testProcessFileMultiThread_failure() throws Exception {

        // Mock ProductService
        ProductService mockProductService = mock(ProductService.class);

        // Make insertBatch throw
        doThrow(new RuntimeException("DB error"))
                .when(mockProductService)
                .insertBatch(any());

        FileIngestionService ingestionService = new FileIngestionService(mockProductService);

        // Fake file content
        String fakeFile =
                "A,SKU1,Desc1,10.0\n" +
                "B,SKU2,Desc2,20.0\n";

        BufferedReader fakeReader = new BufferedReader(new StringReader(fakeFile));

        // Mock Files.newBufferedReader
        try (MockedStatic<Files> mockedFiles = mockStatic(Files.class)) {

            mockedFiles.when(() -> Files.newBufferedReader(any(Path.class)))
                    .thenReturn(fakeReader);

            // Act
            ingestionService.processFileMultiThread("mock/mock.txt");

            // Allow executor to run tasks
            Thread.sleep(200);

            // Assert: insertBatch was still called
            verify(mockProductService, times(1))
                    .insertBatch(argThat(chunk -> chunk.size() == 2));
        }
    }
}

