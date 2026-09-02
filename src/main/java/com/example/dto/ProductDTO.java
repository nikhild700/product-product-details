package com.example.dto;

public sealed interface ProductDTO
    permits CreateProductDTO, UpdateProductDTO, RemoveProductDTO {
}
