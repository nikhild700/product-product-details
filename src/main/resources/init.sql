CREATE DATABASE product_catalog;
USE product_catalog;

CREATE TABLE product (
    product_id INT AUTO_INCREMENT PRIMARY KEY,
    sku VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL
);

CREATE TABLE product_details (
    details_id INT AUTO_INCREMENT PRIMARY KEY,
    product_id INT NOT NULL,
    description TEXT,
    price DECIMAL(10,2),
    FOREIGN KEY (product_id) REFERENCES product(product_id)
    ON DELETE CASCADE
);
