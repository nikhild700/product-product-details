package com.example.dto;

public record CreateProductDTO(String sku, String name, String description, double price)
implements ProductDTO {}
