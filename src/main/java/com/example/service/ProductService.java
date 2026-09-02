package com.example.service;

import com.example.dto.CreateProductDTO;
import com.example.dto.GetProductBySkuDTO;
import com.example.dto.GetProductByNameDTO;
import com.example.dto.ProductDTO;
import com.example.dto.RemoveProductDTO;
import com.example.dto.GetProductWithinPriceRangeDTO;
import com.example.dto.UpdateProductDTO;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import com.example.config.HikariConnectionPool;
import com.example.dao.ProductRepository;
import com.example.model.Product;
import com.example.model.ProductDetails;

public class ProductService {
    private static final Logger logger = LogManager.getLogger(ProductService.class);
    protected ProductRepository productRepository;

    public ProductService() {
        this.productRepository = new ProductRepository();
    }

    public void processProduct(ProductDTO dto) {
        if (dto instanceof CreateProductDTO createDto) {
            insertProductAndProductDetails(createDto);
        } else if (dto instanceof UpdateProductDTO updateDto) {
            updateProductAndProductDetails(updateDto);
        } else if (dto instanceof RemoveProductDTO removeDto) {
            deleteProduct(removeDto);
        } else {
            throw new IllegalArgumentException("Unknown DTO type: " + dto.getClass().getName());
        }
    }

    public int insertProductAndProductDetails(CreateProductDTO dto) {
        // Create Product and ProductDetails objects
        Product product = new Product(dto.name(), dto.sku());
        ProductDetails details = new ProductDetails(product, dto.description(), dto.price());

        Connection connection = null; // <-- declare outside try
        try {
            connection = HikariConnectionPool.getConnection();
            connection.setAutoCommit(false); // transaction management
            int productId = productRepository.insertProduct(connection, product);
            product.setProductId(productId); // Set the generated product ID in the Product object
            productRepository.insertProductDetails(connection, details);
            connection.commit();
        } catch (SQLException e) {
            logger.error("Error inserting product and product details", e);
            rollbackTransaction(connection);
            throw new RuntimeException("Unable to insert product into database", e);
        } finally {
            closeConnection(connection);
        }
        return product.getProductId(); // Return the generated product ID
    }

    // This is transactional, so if either update fails, the whole operation is
    // rolled back
    public void updateProductAndProductDetails(UpdateProductDTO dto) {
        Product product = new Product(dto.name(), dto.sku());
        product.setProductId(dto.productId());

        ProductDetails details = new ProductDetails(product, dto.description(), dto.price());
        details.setDetailId(dto.detailId());

        Connection connection = null;
        try {
            connection = HikariConnectionPool.getConnection();
            connection.setAutoCommit(false);
            productRepository.updateProduct(connection, product);
            productRepository.updateProductDetails(connection, details);
            connection.commit();
        } catch (SQLException e) {
            rollbackTransaction(connection);
            throw new RuntimeException("Unable to update product and product details", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void updateProduct(UpdateProductDTO dto) {
        Product product = new Product(dto.name(), dto.sku());
        product.setProductId(dto.productId());
        Connection connection = null;

        try {
            connection = HikariConnectionPool.getConnection();
            productRepository.updateProduct(connection, product);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to update product", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void updateProductDetails(UpdateProductDTO dto) {
        Product product = new Product();
        product.setProductId(dto.productId());
        ProductDetails details = new ProductDetails(product, dto.description(), dto.price());
        details.setDetailId(dto.detailId());
        Connection connection = null;
        try {
            connection = HikariConnectionPool.getConnection();
            productRepository.updateProductDetails(connection, details);
        } catch (SQLException e) {
            throw new RuntimeException("Unable to update product details", e);
        } finally {
            closeConnection(connection);
        }
    }

    public void deleteProduct(RemoveProductDTO dto) {

        Product product = new Product();
        product.setProductId(dto.productId());
        Connection connection = null;
        try {
            connection = HikariConnectionPool.getConnection();
            connection.setAutoCommit(false);
            productRepository.deleteProduct(connection, product);
            connection.commit();
        } catch (SQLException e) {
            rollbackTransaction(connection);
            throw new RuntimeException("Unable to delete product", e);

        } finally {
            closeConnection(connection);
        }
    }

    public void insertBatch(List<CreateProductDTO> dtos) {

        Connection conn = null;

        try {
            conn = HikariConnectionPool.getConnection();
            conn.setAutoCommit(false);

            // Convert DTOs → Product objects
            List<Product> products = new ArrayList<>();
            for (CreateProductDTO dto : dtos) {
                products.add(new Product(dto.name(), dto.sku()));
            }

            // Insert products batch
            List<Integer> productIds = productRepository.insertProductsBatch(conn, products);

            // Now set the generated product IDs back to the Product objects
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                int id = productIds.get(i);
                p.setProductId(id);
            }

            // Build details list
            List<ProductDetails> detailsList = new ArrayList<>();
            for (int i = 0; i < dtos.size(); i++) {
                var dto = dtos.get(i);
                var product = products.get(i);

                detailsList.add(new ProductDetails(product, dto.description(), dto.price()));
            }

            // Insert details batch
            productRepository.insertProductDetailsBatch(conn, detailsList);

            conn.commit();

        } catch (Exception e) {
            rollbackTransaction(conn);
            throw new RuntimeException("Batch insert failed", e);

        } finally {
            closeConnection(conn);
        }
    }

    public ProductDetails getProductBySku(GetProductBySkuDTO dto) {
        Connection connection = null;
        try {
            connection = HikariConnectionPool.getConnection();
            return productRepository.getProductBySku(connection, dto.sku());
        } catch (SQLException e) {
            throw new RuntimeException("Unable to retrieve product by SKU", e);
        } finally {
            closeConnection(connection);
        }
    }

    public List<ProductDetails> getProductByName(GetProductByNameDTO dto) {
        Connection connection = null;
        try {
            connection = HikariConnectionPool.getConnection();
            return productRepository.getProductByName(connection, dto.name());
        } catch (SQLException e) {
            throw new RuntimeException("Unable to retrieve product by name", e);
        } finally {
            closeConnection(connection);
        }
    }

    public List<ProductDetails> getProductsWithinPriceRange(GetProductWithinPriceRangeDTO dto) {
        Connection connection = null;
        try {
            connection = HikariConnectionPool.getConnection();
            return productRepository.getProductsWithinPriceRange(connection, dto.minPrice(), dto.maxPrice());
        } catch (SQLException e) {
            throw new RuntimeException("Unable to retrieve products within price range", e);
        } finally {
            closeConnection(connection);
        }
    }

    private void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
            } catch (SQLException e) {
                throw new RuntimeException("Unable to rollback transaction", e);
            }
        }
    }

    private void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                // log or ignore
            }
        }
    }
}
