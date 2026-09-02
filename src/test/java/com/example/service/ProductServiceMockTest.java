package com.example.service;

import com.example.model.ProductDetails;
import com.example.model.Product;
import com.example.service.ProductService;
import com.example.config.HikariConnectionPool;
import com.example.dao.ProductRepository;
import com.example.dto.GetProductWithinPriceRangeDTO;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceMockTest {

    @Test
    public void testGetProductsWithinPriceRange_returnsProductDetails() throws SQLException {

        // Create a sample ProductDetails object to return
        Product product = new Product("Test Product", "SKU-123");
        product.setProductId(1);
        ProductDetails productDetails = new ProductDetails(product, "Test Description", 50.0);
        productDetails.setDetailId(1);
        
        // Mock the ProductRepository
        var mockRepo = mock(ProductRepository.class);
        // Mock the behavior of getProductsWithinPriceRange
        when(mockRepo.getProductsWithinPriceRange(any(), anyDouble(), anyDouble()))
                .thenReturn(List.of(productDetails));

        // Create an instance of ProductService with the mocked repository
        var productService = new ProductService();
        productService.productRepository = mockRepo; // Inject the mock repository

        GetProductWithinPriceRangeDTO dto = new GetProductWithinPriceRangeDTO(10.0, 100.0);
        
        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {
            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection)
                    .thenReturn(mockConn);

            // ProductService call happens here
            List<ProductDetails> result = productService.getProductsWithinPriceRange(dto);
            
            // Verify the result
            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Test Product", result.get(0).getProduct().getName());
            assertEquals("SKU-123", result.get(0).getProduct().getSku());
        }
    }
}
