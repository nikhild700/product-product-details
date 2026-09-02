package com.example.model;

public class Product {

    private Integer productId;
    private String name;
    private String sku;

    public Product() {
    }

    public Product(String name, String sku) {
        this.name = name;
        this.sku = sku;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", sku='" + sku + '\'' +
                '}';
    }

}
