package com.example.dto;

public record UpdateProductDTO(Integer productId, Integer detailId, String sku, String name, String description,
        double price) implements ProductDTO {}
