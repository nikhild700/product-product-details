package com.example.dao;

import com.example.model.ProductDetails;
import com.example.model.Product;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

class ProductRepositoryMockTest {

    @Test
    void testGetProductsWithinPriceRange_returnsProductDetails() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        // Simulate two rows
        when(rs.next()).thenReturn(true, true, false);

        when(rs.getInt("product_id")).thenReturn(1, 2);
        when(rs.getString("sku")).thenReturn("SKU-1", "SKU-2");
        when(rs.getString("name")).thenReturn("Product A", "Product B");
        when(rs.getInt("detail_id")).thenReturn(10, 20);
        when(rs.getString("description")).thenReturn("Desc A", "Desc B");
        when(rs.getDouble("price")).thenReturn(5.0, 15.0);

        ProductRepository repo = new ProductRepository();

        List<ProductDetails> list = repo.getProductsWithinPriceRange(conn, 1, 20);

        assertEquals(2, list.size());
        assertEquals("SKU-1", list.get(0).getProduct().getSku());
        assertEquals("SKU-2", list.get(1).getProduct().getSku());
    }

    @Test
    void testGetProductsWithinPriceRange_throwsSQLException() throws Exception {
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenThrow(new SQLException("Database error"));

        ProductRepository repo = new ProductRepository();

        try {
            repo.getProductsWithinPriceRange(conn, 1, 20);
        } catch (RuntimeException e) {
            assertTrue(e.getMessage().contains("Unable to fetch products within price range"));
        }
        // Verify JDBC interactions
        verify(conn, times(1)).prepareStatement(anyString());
        verify(ps, times(1)).executeQuery();
    }

    @ParameterizedTest
    @CsvSource({
            "SKU-123, Test Product, A product description, 99.99, 10, 55",
            "SKU-999, Another Product, Another description, 49.50, 20, 88"
    })
    void getProductBySku_returnsProductDetails(
            String sku,
            String name,
            String description,
            double price,
            int productId,
            int detailId) throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        // Simulate one row returned
        when(rs.next()).thenReturn(true);

        // Mock column values
        when(rs.getInt("product_id")).thenReturn(productId);
        when(rs.getString("sku")).thenReturn(sku);
        when(rs.getString("name")).thenReturn(name);
        when(rs.getString("description")).thenReturn(description);
        when(rs.getDouble("price")).thenReturn(price);
        when(rs.getInt("detail_id")).thenReturn(detailId);

        ProductRepository repo = new ProductRepository();

        ProductDetails details = repo.getProductBySku(conn, sku);

        assertNotNull(details);
        assertEquals(detailId, details.getDetailId());
        assertEquals(price, details.getPrice());

        Product product = details.getProduct();
        assertEquals(productId, product.getProductId());
        assertEquals(sku, product.getSku());
        assertEquals(name, product.getName());

        verify(ps).setString(1, sku);
        verify(ps).executeQuery();
        verify(rs).next();
    }

    @Test
    void getProductBySku_throwsSQLException() throws Exception {

        // Arrange
        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        String sku = "SKU-123";

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenThrow(new SQLException("Database error"));

        ProductRepository repo = new ProductRepository();

        assertThrows(SQLException.class,
                () -> repo.getProductBySku(conn, sku));

        // Verify JDBC interactions
        verify(conn, times(1)).prepareStatement(anyString());
        verify(ps, times(1)).executeQuery();
    }

    @Test
    void testGetProductByName_returnsProductDetails() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet rs = mock(ResultSet.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenReturn(rs);

        when(rs.next()).thenReturn(true, false);

        when(rs.getInt("product_id")).thenReturn(100);
        when(rs.getString("sku")).thenReturn("SKU-100");
        when(rs.getString("name")).thenReturn("Test Name");
        when(rs.getInt("detail_id")).thenReturn(200);
        when(rs.getString("description")).thenReturn("Desc");
        when(rs.getDouble("price")).thenReturn(50.0);

        ProductRepository repo = new ProductRepository();

        List<ProductDetails> list = repo.getProductByName(conn, "Test");

        assertEquals(1, list.size());
        assertEquals("SKU-100", list.get(0).getProduct().getSku());
    }

    @Test
    void testGetProductByName_throwsSQLException() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeQuery()).thenThrow(new SQLException("Database error"));

        ProductRepository repo = new ProductRepository();

        assertThrows(SQLException.class, () -> repo.getProductByName(conn, "Test"));

        // Verify JDBC interactions
        verify(conn, times(1)).prepareStatement(anyString());
        verify(ps, times(1)).executeQuery();
    }

    @Test
    void testInsertProduct_returnsProductDetails() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet keys = mock(ResultSet.class);

        Product p = new Product("SKU-1", "Name");

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(true);
        when(keys.getInt(1)).thenReturn(999);

        ProductRepository repo = new ProductRepository();

        int id = repo.insertProduct(conn, p);

        assertEquals(999, id);
    }

    @Test
    void testInsertProduct_throwsSQLException() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet keys = mock(ResultSet.class);

        Product p = new Product("SKU-1", "Name");

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);

        // Simulate failure
        when(ps.executeUpdate()).thenReturn(0);
        when(ps.getGeneratedKeys()).thenReturn(keys);
        when(keys.next()).thenReturn(false);

        ProductRepository repo = new ProductRepository();

        assertThrows(SQLException.class, () -> repo.insertProduct(conn, p));

        // FIXED: match actual repository parameter order
        verify(ps).setString(1, "Name");
        verify(ps).setString(2, "SKU-1");

        verify(ps).executeUpdate();
    }

    @Test
    void testInsertProductsBatch() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);
        ResultSet keys = mock(ResultSet.class);

        List<Product> products = List.of(
                new Product("SKU-1", "A"),
                new Product("SKU-2", "B"));

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);
        when(ps.executeBatch()).thenReturn(new int[] { 1, 1 });
        when(ps.getGeneratedKeys()).thenReturn(keys);

        when(keys.next()).thenReturn(true, true, false);
        when(keys.getInt(1)).thenReturn(10, 20);

        ProductRepository repo = new ProductRepository();

        List<Integer> ids = repo.insertProductsBatch(conn, products);

        assertEquals(List.of(10, 20), ids);
    }

    @Test
    void testInsertProductsBatch_throwsSQLException() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        List<Product> products = List.of(
                new Product("SKU-1", "A"),
                new Product("SKU-2", "B"));

        when(conn.prepareStatement(anyString(), anyInt())).thenReturn(ps);

        // Simulate failure
        when(ps.executeBatch()).thenThrow(new SQLException("Batch insert failed"));

        ProductRepository repo = new ProductRepository();

        assertThrows(SQLException.class, () -> repo.insertProductsBatch(conn, products));

        // Only verify the batch call — nothing else
        verify(ps).executeBatch();
    }

    @Test
    void testInsertProductDetailsBatch() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        Product p1 = new Product("SKU1", "A");
        p1.setProductId(1);

        Product p2 = new Product("SKU2", "B");
        p2.setProductId(2);

        List<ProductDetails> list = List.of(
                new ProductDetails(p1, "D1", 10.0),
                new ProductDetails(p2, "D2", 20.0));

        when(conn.prepareStatement(anyString())).thenReturn(ps);

        ProductRepository repo = new ProductRepository();
        repo.insertProductDetailsBatch(conn, list);

        verify(ps, times(2)).addBatch();
        verify(ps).executeBatch();
    }

    @Test
    void testInsertProductDetailsBatch_throwsSQLException() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        Product p1 = new Product("SKU1", "A");
        p1.setProductId(1);

        Product p2 = new Product("SKU2", "B");
        p2.setProductId(2);

        List<ProductDetails> list = List.of(
                new ProductDetails(p1, "D1", 10.0),
                new ProductDetails(p2, "D2", 20.0));

        when(conn.prepareStatement(anyString())).thenReturn(ps);

        // Simulate failure
        when(ps.executeBatch()).thenThrow(new SQLException("Batch failed"));

        ProductRepository repo = new ProductRepository();

        assertThrows(SQLException.class, () -> repo.insertProductDetailsBatch(conn, list));

        // Only verify the failing call
        verify(ps).executeBatch();
    }

    @Test
    void testUpdateProductDetails() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        Product p = new Product("SKU", "Name");
        p.setProductId(10);

        ProductDetails d = new ProductDetails(p, "Desc", 99.0);
        d.setDetailId(55);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        ProductRepository repo = new ProductRepository();
        repo.updateProductDetails(conn, d);

        verify(ps).setString(1, "Desc");
        verify(ps).setDouble(2, 99.0);
        verify(ps).setInt(3, 10);
        verify(ps).setInt(4, 55);
    }

    @Test
    void testUpdateProductDetails_throwsSQLException() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        Product p = new Product("SKU", "Name");
        p.setProductId(10);

        ProductDetails d = new ProductDetails(p, "Desc", 99.0);
        d.setDetailId(55);

        when(conn.prepareStatement(anyString())).thenReturn(ps);

        // Simulate failure: no rows updated
        when(ps.executeUpdate()).thenReturn(0);

        ProductRepository repo = new ProductRepository();

        assertThrows(SQLException.class, () -> repo.updateProductDetails(conn, d));

        // Only verify the failing call
        verify(ps).executeUpdate();
    }

    @Test
    void testUpdateProduct() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        Product p = new Product("SKU", "Name");
        p.setProductId(100);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        ProductRepository repo = new ProductRepository();
        repo.updateProduct(conn, p);

        verify(ps).setString(1, p.getSku());
        verify(ps).setString(2, p.getName());
        verify(ps).setInt(3, 100);
    }

    @Test
    void testUpdateProduct_throwsSQLException() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        Product p = new Product("SKU", "Name");
        p.setProductId(100);

        when(conn.prepareStatement(anyString())).thenReturn(ps);

        // Simulate failure: no rows updated
        when(ps.executeUpdate()).thenReturn(0);

        ProductRepository repo = new ProductRepository();

        assertThrows(SQLException.class, () -> repo.updateProduct(conn, p));

        // Only verify the failing call
        verify(ps).executeUpdate();
    }

    @Test
    void testDeleteProduct() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        Product p = new Product("SKU", "Name");
        p.setProductId(999);

        when(conn.prepareStatement(anyString())).thenReturn(ps);
        when(ps.executeUpdate()).thenReturn(1);

        ProductRepository repo = new ProductRepository();
        repo.deleteProduct(conn, p);

        verify(ps).setInt(1, 999);
        verify(ps).executeUpdate();
    }

    @Test
    void testDeleteProduct_throwsSQLException() throws Exception {

        Connection conn = mock(Connection.class);
        PreparedStatement ps = mock(PreparedStatement.class);

        Product p = new Product("SKU", "Name");
        p.setProductId(999);

        when(conn.prepareStatement(anyString())).thenReturn(ps);

        // Simulate failure: no rows deleted
        when(ps.executeUpdate()).thenReturn(0);

        ProductRepository repo = new ProductRepository();

        assertThrows(SQLException.class, () -> repo.deleteProduct(conn, p));

        // Only verify the failing call
        verify(ps).executeUpdate();
    }

}