package com.icecreamy.entity;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.icecreamy.obj.ProType;

@Entity
@Table(name = "product")
@NamedQueries({@NamedQuery(name = "getAllProducts", query = "SELECT p FROM Product  AS p ORDER BY p.productId ASC"),
        @NamedQuery(name = "getProductByName", query = "SELECT p FROM Product AS p WHERE p.name = :name"),
        @NamedQuery(name = "getProductById", query = "SELECT p FROM Product AS p WHERE p.productId = :productId"),
        @NamedQuery(name = "getAllProductByPriceEx", query = "SELECT p FROM Product AS p ORDER BY p.price desc"),
        @NamedQuery(name = "getAllProductByPriceChe", query = "SELECT p FROM Product AS p ORDER BY p.price asc"),
        @NamedQuery(name = "searchProductByName", query = "SELECT p FROM Product p WHERE UPPER(p.name) LIKE :name"),
        @NamedQuery(name = "getAllProductsByType", query = "SELECT p FROM Product p WHERE p.type = :type"),
        @NamedQuery(name = "searchProductByPriceRange", query = "SELECT p FROM Product p WHERE p.price  BETWEEN :minPrice AND :maxPrice"),
        @NamedQuery(name = "deleteProductById", query = "DELETE from Product p where p.productId=:productId"),

})
@JsonIgnoreProperties(ignoreUnknown = true)
public class Product implements Serializable {
    private static final long serialVersionUID = 1906936250766173293L;

    private int quantity;
    @Id
    @Column(name = "product_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productId;
    private String name;
    private double price;

    @Enumerated(EnumType.STRING)
    private ProType type;

    @Column(columnDefinition = "text")
    private String description;

    private String imagePath;

    private int stock;
    private String italianName;
    private String ukrainianName;
    private double oldPrice;
    private String accentColor;
    private String gradientStyle;

    public Product() {

    }

    public Product(String name, String italianName, String ukrainianName, double price, double oldPrice,
                   ProType type, String description, String imagePath, int stock,
                   String accentColor, String gradientStyle, int quantity) {
        this.name = name;
        this.italianName = italianName;
        this.ukrainianName = ukrainianName;
        this.price = price;
        this.oldPrice = oldPrice;
        this.type = type;
        this.description = description;
        this.imagePath = imagePath;
        this.stock = stock;
        this.accentColor = accentColor;
        this.gradientStyle = gradientStyle;
        this.quantity = quantity;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public ProType getType() {
        return type;
    }

    public void setType(ProType type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public String getItalianName() {
        return italianName;
    }

    public void setItalianName(String italianName) {
        this.italianName = italianName;
    }

    public String getUkrainianName() {
        return ukrainianName;
    }

    public void setUkrainianName(String ukrainianName) {
        this.ukrainianName = ukrainianName;
    }

    public double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(double oldPrice) {
        this.oldPrice = oldPrice;
    }

    public String getAccentColor() {
        return accentColor;
    }

    public void setAccentColor(String accentColor) {
        this.accentColor = accentColor;
    }

    public String getGradientStyle() {
        return gradientStyle;
    }

    public void setGradientStyle(String gradientStyle) {
        this.gradientStyle = gradientStyle;
    }

    @Override
    public String toString() {
        return "Product [quantity=" + quantity + ", productId=" + productId + ", name=" + name + ", price=" + price
                + ", type=" + type + ", description=" + description + ", imagePath=" + imagePath + ", stock=" + stock
                + "]";
    }

}
