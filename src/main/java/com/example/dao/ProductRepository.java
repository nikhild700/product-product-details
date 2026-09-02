package com.example.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.example.model.Product;
import com.example.model.ProductDetails;

public class ProductRepository {

    public ProductRepository() {
    }

    public List<ProductDetails> getProductsWithinPriceRange(Connection connection,
            double minPrice, double maxPrice) throws SQLException {

        String sql = """
                    SELECT p.product_id, p.sku, p.name,
                           d.detail_id, d.description, d.price
                    FROM product p
                    INNER JOIN product_details d ON p.product_id = d.product_id
                    WHERE d.price BETWEEN ? AND ?
                """;

        List<ProductDetails> products = new ArrayList<>();

        try (PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setDouble(1, minPrice);
            ps.setDouble(2, maxPrice);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {

                    Product product = new Product(
                            rs.getString("name"),
                            rs.getString("sku"));
                    product.setProductId(rs.getInt("product_id"));

                    ProductDetails details = new ProductDetails(
                            product,
                            rs.getString("description"),
                            rs.getDouble("price"));
                    details.setDetailId(rs.getInt("detail_id"));

                    products.add(details);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Unable to fetch products within price range", e);
        }

        return products;
    }

    public ProductDetails getProductBySku(Connection conn, String sku) throws SQLException {
        String sql = """
                        SELECT p.*, d.detail_id, d.description, d.price
                        FROM product p INNER JOIN product_details d ON p.product_id = d.product_id
                        WHERE p.sku = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sku);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    var product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setSku(rs.getString("sku"));
                    product.setName(rs.getString("name"));

                    var details = new ProductDetails();
                    details.setProduct(product);
                    details.setDescription(rs.getString("description"));
                    details.setPrice(rs.getDouble("price"));
                    details.setDetailId(rs.getInt("detail_id"));
                    return details;
                }
            }
        }
        return null; // or throw an exception if not found
    }

    public List<ProductDetails> getProductByName(Connection conn, String name) throws SQLException {
        String sql = """
                        SELECT p.*, d.detail_id, d.description, d.price
                        FROM product p INNER JOIN product_details d ON p.product_id = d.product_id
                        WHERE p.name LIKE ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + name + "%");
            try (ResultSet rs = ps.executeQuery()) {
                List<ProductDetails> products = new ArrayList<>();
                while (rs.next()) {
                    var product = new Product();
                    product.setProductId(rs.getInt("product_id"));
                    product.setSku(rs.getString("sku"));
                    product.setName(rs.getString("name"));

                    var details = new ProductDetails();
                    details.setProduct(product);
                    details.setDescription(rs.getString("description"));
                    details.setPrice(rs.getDouble("price"));
                    details.setDetailId(rs.getInt("detail_id"));
                    products.add(details);
                }
                return products;
            }
        }
    }

    public int insertProduct(Connection conn, Product product) throws SQLException {
        String sql = "INSERT INTO product (sku, name) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, product.getSku());
            ps.setString(2, product.getName());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 1) {
                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        return generatedKeys.getInt(1);
                    }
                }
            }
        }
        throw new SQLException("Failed to insert product");
    }

    public void insertProductDetails(Connection conn, ProductDetails details) throws SQLException {
        String sql = "INSERT INTO product_details (product_id, description, price) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, details.getProduct().getProductId());
            ps.setString(2, details.getDescription());
            ps.setDouble(3, details.getPrice());
            ps.executeUpdate();
        }
    }

    public List<Integer> insertProductsBatch(Connection conn, List<Product> products) throws SQLException {
        String sql = "INSERT INTO product (name, sku) VALUES (?, ?)";

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            for (Product p : products) {
                ps.setString(1, p.getName());
                ps.setString(2, p.getSku());
                ps.addBatch();
            }

            ps.executeBatch();

            List<Integer> ids = new ArrayList<>();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                while (rs.next()) {
                    ids.add(rs.getInt(1));
                }
            }

            return ids;
        }
    }

    public void insertProductDetailsBatch(Connection conn, List<ProductDetails> detailsList) throws SQLException {
        String sql = "INSERT INTO product_details (description, price, product_id) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (ProductDetails d : detailsList) {
                ps.setString(1, d.getDescription());
                ps.setDouble(2, d.getPrice());
                ps.setInt(3, d.getProduct().getProductId());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    public void updateProductDetails(Connection conn, ProductDetails details) throws SQLException {
        String sql = "UPDATE product_details SET description = ?, price = ?, product_id = ? WHERE detail_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, details.getDescription());
            ps.setDouble(2, details.getPrice());
            ps.setInt(3, details.getProduct().getProductId());
            ps.setInt(4, details.getDetailId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to update product details with ID: " + details.getDetailId());
            }
        }
    }

    public void updateProduct(Connection conn, Product product) throws SQLException {
        String sql = "UPDATE product SET sku = ?, name = ? WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getSku());
            ps.setString(2, product.getName());
            ps.setInt(3, product.getProductId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to update product with ID: " + product.getProductId());
            }
        }
    }

    public void deleteProduct(Connection conn, Product product) throws SQLException {
        String sql = "DELETE FROM product WHERE product_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getProductId());
            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Failed to delete product with ID: " + product.getProductId());
            }
        }
    }

}
