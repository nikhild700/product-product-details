package com.example.service;

import com.example.model.ProductDetails;
import com.example.model.Product;
import com.example.config.HikariConnectionPool;
import com.example.dao.ProductRepository;
import com.example.dto.CreateProductDTO;
import com.example.dto.GetProductWithinPriceRangeDTO;
import com.example.dto.UpdateProductDTO;
import com.example.dto.RemoveProductDTO;
import com.example.dto.GetProductBySkuDTO;
import com.example.dto.GetProductByNameDTO;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class ProductServiceMockTest {

    @Test
    public void testGetProductsWithinPriceRange_returnsProductDetails() throws SQLException {

        Product product = new Product("Test Product", "SKU-123");
        product.setProductId(1);
        ProductDetails productDetails = new ProductDetails(product, "Test Description", 50.0);
        productDetails.setDetailId(1);

        var mockRepo = mock(ProductRepository.class);
        when(mockRepo.getProductsWithinPriceRange(any(), anyDouble(), anyDouble()))
                .thenReturn(List.of(productDetails));

        var productService = new ProductService();
        productService.productRepository = mockRepo;

        GetProductWithinPriceRangeDTO dto = new GetProductWithinPriceRangeDTO(10.0, 100.0);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {
            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            List<ProductDetails> result = productService.getProductsWithinPriceRange(dto);

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("Test Product", result.get(0).getProduct().getName());
            assertEquals("SKU-123", result.get(0).getProduct().getSku());
        }
    }

    @Test
    public void testGetProductsWithinPriceRange_throwsException() throws SQLException {

        var mockRepo = mock(ProductRepository.class);
        when(mockRepo.getProductsWithinPriceRange(any(), anyDouble(), anyDouble()))
                .thenThrow(new SQLException("Database error"));

        var productService = new ProductService();
        productService.productRepository = mockRepo;

        GetProductWithinPriceRangeDTO dto = new GetProductWithinPriceRangeDTO(10.0, 100.0);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {
            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            assertThrows(RuntimeException.class, () -> productService.getProductsWithinPriceRange(dto));
        }
    }

    @Test
    void testInsertProductAndProductDetails_success() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        CreateProductDTO dto = new CreateProductDTO("Name", "SKU", "Desc", 99.0);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            when(mockRepo.insertProduct(eq(mockConn), any())).thenReturn(10);

            int id = productService.insertProductAndProductDetails(dto);

            assertEquals(10, id);
            verify(mockConn).setAutoCommit(false);
            verify(mockConn).commit();

            verify(mockRepo).insertProduct(eq(mockConn), any());
            verify(mockRepo).insertProductDetails(eq(mockConn), any());
        }
    }

    @Test
    void testInsertProductAndProductDetails_throwsException() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        CreateProductDTO dto = new CreateProductDTO("Name", "SKU", "Desc", 99.0);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            when(mockRepo.insertProduct(eq(mockConn), any())).thenThrow(new SQLException("DB error"));

            assertThrows(RuntimeException.class, () -> productService.insertProductAndProductDetails(dto));

            verify(mockConn).rollback();
            verify(mockConn).close();
        }
    }

    @Test
    void testUpdateProductAndProductDetails_success() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        UpdateProductDTO dto = new UpdateProductDTO(10, 55, "Name", "SKU", "Desc", 99.0);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            productService.updateProductAndProductDetails(dto);

            verify(mockConn).setAutoCommit(false);
            verify(mockConn).commit();

            verify(mockRepo).updateProduct(eq(mockConn), any());
            verify(mockRepo).updateProductDetails(eq(mockConn), any());
        }
    }

    @Test
    void testUpdateProductAndProductDetails_throwsException() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        UpdateProductDTO dto = new UpdateProductDTO(10, 55, "Name", "SKU", "Desc", 99.0);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            doThrow(new SQLException("DB error")).when(mockRepo).updateProduct(eq(mockConn), any());

            assertThrows(RuntimeException.class, () -> productService.updateProductAndProductDetails(dto));

            verify(mockConn).rollback();
            verify(mockConn).close();
        }
    }

    @Test
    void testUpdateProduct_success() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        UpdateProductDTO dto = new UpdateProductDTO(10, 0, "Name", "SKU", null, 0);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            productService.updateProduct(dto);

            verify(mockRepo).updateProduct(eq(mockConn), any());
            verify(mockConn).close();
        }
    }

    @Test
    void testUpdateProduct_throwsException() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        UpdateProductDTO dto = new UpdateProductDTO(10, 0, "Name", "SKU", null, 0);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            doThrow(new SQLException("DB error")).when(mockRepo).updateProduct(eq(mockConn), any());

            assertThrows(RuntimeException.class, () -> productService.updateProduct(dto));

            verify(mockConn).close();
        }
    }

    @Test
    void testUpdateProductDetails_success() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        UpdateProductDTO dto = new UpdateProductDTO(10, 55, null, null, "Desc", 99.0);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            productService.updateProductDetails(dto);

            verify(mockRepo).updateProductDetails(eq(mockConn), any());
            verify(mockConn).close();
        }
    }

    @Test
    void testUpdateProductDetails_throwsException() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        UpdateProductDTO dto = new UpdateProductDTO(10, 55, null, null, "Desc", 99.0);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            doThrow(new SQLException("DB error")).when(mockRepo).updateProductDetails(eq(mockConn), any());

            assertThrows(RuntimeException.class,
                    () -> productService.updateProductDetails(dto));

            verify(mockConn).close();
        }
    }

    @Test
    void testDeleteProduct_success() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        RemoveProductDTO dto = new RemoveProductDTO(999);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            productService.deleteProduct(dto);

            verify(mockConn).setAutoCommit(false);
            verify(mockRepo).deleteProduct(eq(mockConn), any());
            verify(mockConn).commit();
            verify(mockConn).close();
        }
    }

    @Test
    void testDeleteProduct_throwsException() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        RemoveProductDTO dto = new RemoveProductDTO(999);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            doThrow(new SQLException("DB error")).when(mockRepo).deleteProduct(eq(mockConn), any());

            assertThrows(RuntimeException.class, () -> productService.deleteProduct(dto));

            verify(mockConn).rollback();
            verify(mockConn).close();
        }
    }

    @Test
    void testInsertBatch_success() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        List<CreateProductDTO> dtos = List.of(
                new CreateProductDTO("A", "SKU1", "D1", 10.0),
                new CreateProductDTO("B", "SKU2", "D2", 20.0));

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            when(mockRepo.insertProductsBatch(eq(mockConn), any())).thenReturn(List.of(10, 20));

            productService.insertBatch(dtos);

            verify(mockConn).setAutoCommit(false);
            verify(mockRepo).insertProductsBatch(eq(mockConn), any());
            verify(mockRepo).insertProductDetailsBatch(eq(mockConn), any());
            verify(mockConn).commit();
        }
    }

    @Test
    void testInsertBatch_throwsException() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        List<CreateProductDTO> dtos = List.of(
                new CreateProductDTO("A", "SKU1", "D1", 10.0));

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            when(mockRepo.insertProductsBatch(eq(mockConn), any()))
                    .thenThrow(new SQLException("DB error"));

            assertThrows(RuntimeException.class, () -> productService.insertBatch(dtos));

            verify(mockConn).rollback();
            verify(mockConn).close();
        }
    }

    @Test
    void testGetProductBySku_success() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        Product product = new Product("Name", "SKU");
        product.setProductId(10);
        ProductDetails details = new ProductDetails(product, "Desc", 99.0);
        details.setDetailId(55);

        Connection mockConn = mock(Connection.class);
        when(mockRepo.getProductBySku(eq(mockConn), eq("SKU"))).thenReturn(details);

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            ProductDetails result = productService.getProductBySku(new GetProductBySkuDTO("SKU"));

            assertNotNull(result);
            assertEquals(55, result.getDetailId());
            verify(mockConn).close();
        }
    }

    @Test
    void testGetProductBySku_throwsException() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        Connection mockConn = mock(Connection.class);
        when(mockRepo.getProductBySku(eq(mockConn), eq("SKU")))
                .thenThrow(new SQLException("DB error"));

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            assertThrows(RuntimeException.class,
                    () -> productService.getProductBySku(new GetProductBySkuDTO("SKU")));

            verify(mockConn).close();
        }
    }

    @Test
    void testGetProductByName_success() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        Product product = new Product("Name", "SKU");
        product.setProductId(10);
        ProductDetails details = new ProductDetails(product, "Desc", 99.0);

        Connection mockConn = mock(Connection.class);
        when(mockRepo.getProductByName(eq(mockConn), eq("Name")))
                .thenReturn(List.of(details));

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            List<ProductDetails> result = productService.getProductByName(new GetProductByNameDTO("Name"));

            assertEquals(1, result.size());
            verify(mockConn).close();
        }
    }

    @Test
    void testGetProductByName_throwsException() throws Exception {

        var mockRepo = mock(ProductRepository.class);
        var productService = new ProductService();
        productService.productRepository = mockRepo;

        when(mockRepo.getProductByName(any(), anyString()))
                .thenThrow(new SQLException("DB error"));

        try (MockedStatic<HikariConnectionPool> mocked = mockStatic(HikariConnectionPool.class)) {

            Connection mockConn = mock(Connection.class);
            mocked.when(HikariConnectionPool::getConnection).thenReturn(mockConn);

            assertThrows(RuntimeException.class,
                    () -> productService.getProductByName(new GetProductByNameDTO("Name")));

            verify(mockConn).close();
        }
    }
}
