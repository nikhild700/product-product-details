package com.example.model;

public class ProductDetails {

    private Integer detailId;
    private String description;
    private Double price;
    private Product product;

    public ProductDetails() {
    }

    public ProductDetails(Product product,
            String description, Double price) {

        this.product = product;
        this.description = description;
        this.price = price;
    }

    public Integer getDetailId() {
        return detailId;
    }

    public void setDetailId(Integer detailId) {
        this.detailId = detailId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    @Override
    public String toString() {
        return "ProductDetails [detailId=" + detailId + ", description=" + description + ", price=" + price
                + ", product=" + product + "]";
    }
}
