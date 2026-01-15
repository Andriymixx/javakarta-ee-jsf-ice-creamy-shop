package com.icecreamy.obj.dto;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.icecreamy.obj.ProType;

@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ProductDTO extends ModelDtoObject implements Serializable {
    private int quantity;
    private int productId;
    private String name;
    private double price;
    private ProType type;
    private String description;
    private String imagePath;
    private int stock;
    private String italianName;
    private String ukrainianName;
    private double oldPrice;
    private String accentColor;
    private String gradientStyle;
    private int editingProduct;

    public ProductDTO() {
    }

    public int getEditingProduct() {
        return editingProduct;
    }

    public void setEditingProduct(int editingProduct) {
        this.editingProduct = editingProduct;
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

    public Double getOldPrice() {
        return oldPrice;
    }

    public void setOldPrice(Double oldPrice) {
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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
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

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

}
