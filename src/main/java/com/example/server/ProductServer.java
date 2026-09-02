package com.example.server;

import com.example.dto.*;
import com.example.service.ProductService;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


public class ProductServer {
    private static final Logger logger = LogManager.getLogger(ProductServer.class);
    private final Gson gson;

    public ProductServer() {
        gson = new GsonBuilder().setPrettyPrinting().create();
    }

    public static void main(String[] args) throws IOException {
        new ProductServer().start();
    }

    public void start() throws IOException {
        final ProductService productService = new ProductService();
        HttpServer server = HttpServer.create(new InetSocketAddress(9080), 0);

        server.createContext("/products/sku", exchange -> {
            if (!validateGet(exchange))
                return;

            String sku = getSingleQueryParam(exchange, "value");
            if (sku == null || sku.isBlank()) {
                sendResponse(exchange, 400, "Missing required query parameter: value");
                return;
            }

            var dto = new GetProductBySkuDTO(sku);
            var product = productService.getProductBySku(dto);

            if (product == null) {
                sendResponse(exchange, 404, "Product not found for SKU: " + sku);
            } else {
                sendResponse(exchange, 200, gson.toJson(product));
            }
        });

        server.createContext("/products/name", exchange -> {
            if (!validateGet(exchange))
                return;

            String name = getSingleQueryParam(exchange, "value");
            if (name == null || name.isBlank()) {
                sendResponse(exchange, 400, "Missing required query parameter: value");
                return;
            }

            var dto = new GetProductByNameDTO(name);
            var products = productService.getProductByName(dto);

            if (products == null || products.isEmpty()) {
                sendResponse(exchange, 404, "Product not found for name: " + name);
            } else {
                sendResponse(exchange, 200, gson.toJson(products));
            }
        });

        server.createContext("/products/price", exchange -> {
            if (!validateGet(exchange))
                return;

            Double min = getQueryDouble(exchange, "min");
            Double max = getQueryDouble(exchange, "max");

            if (min == null || max == null) {
                sendResponse(exchange, 400, "Missing required query parameters: min and/or max");
                return;
            }

            var dto = new GetProductWithinPriceRangeDTO(min, max);
            var products = productService.getProductsWithinPriceRange(dto);

            if (products == null || products.isEmpty()) {
                sendResponse(exchange, 404, "No products found within the specified price range.");
            } else {
                sendResponse(exchange, 200, gson.toJson(products));
            }
        });

        server.createContext("/products", exchange -> {
            if (!"POST".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            logger.info("Received request body: {}", body);
            var dto = gson.fromJson(body, CreateProductDTO.class);

            var id = productService.insertProductAndProductDetails(dto);
            sendResponse(exchange, 201, "Created product with id: " + id);
        });

        server.createContext("/products/product", exchange -> {
            if (!"PUT".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            UpdateProductDTO dto = gson.fromJson(body, UpdateProductDTO.class);
            productService.updateProduct(dto);
            sendResponse(exchange, 200, "Updated product with id: " + dto.productId());
        });

        server.createContext("/products/details", exchange -> {
            if (!"PUT".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            String body = new String(exchange.getRequestBody().readAllBytes());
            UpdateProductDTO dto = gson.fromJson(body, UpdateProductDTO.class);
            productService.updateProductDetails(dto);
            sendResponse(exchange, 200, "Updated product details with id: " + dto.detailId());
        });

        server.createContext("/products/delete", exchange -> {
            if (!"DELETE".equals(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed");
                return;
            }

            Integer id = getQueryInt(exchange, "id");
            if (id == null) {
                sendResponse(exchange, 400, "Missing required query parameter: id");
                return;
            }

            productService.deleteProduct(new RemoveProductDTO(id));
            sendResponse(exchange, 200, "Product deleted");
        });

        server.start();
        logger.info("Server running on port 9080...");
    }

    private boolean validateGet(HttpExchange exchange) throws IOException {
        if (!"GET".equals(exchange.getRequestMethod())) {
            sendResponse(exchange, 405, "Method Not Allowed");
            return false;
        }
        return true;
    }

    private String getSingleQueryParam(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null)
            return null;

        String prefix = key + "=";
        if (query.startsWith(prefix)) {
            return query.substring(prefix.length());
        }
        return null;
    }

    private Double getQueryDouble(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null)
            return null;

        for (String param : query.split("&")) {
            if (param.startsWith(key + "=")) {
                try {
                    return Double.parseDouble(param.substring((key + "=").length()));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private Integer getQueryInt(HttpExchange exchange, String key) {
        String query = exchange.getRequestURI().getQuery();
        if (query == null)
            return null;

        for (String param : query.split("&")) {
            if (param.startsWith(key + "=")) {
                try {
                    return Integer.parseInt(param.substring((key + "=").length()));
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }

    private void sendResponse(HttpExchange exchange, int status, String body) throws IOException {
        exchange.sendResponseHeaders(status, body.length());
        exchange.getResponseBody().write(body.getBytes());
        exchange.close();
    }

}
