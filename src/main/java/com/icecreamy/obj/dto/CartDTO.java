package com.icecreamy.obj.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.icecreamy.util.Utilities;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CartDTO extends ModelDtoObject {

    private int cartId;
    private List<CartItemDTO> items;
    private CustomerDTO customer;
    private int size;
    private int sum;

    public CartDTO() {
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public List<CartItemDTO> getCartProducts() {
        return items;
    }

    public void setCartProducts(List<CartItemDTO> cartProducts) {
        this.items = cartProducts;
    }

    public CustomerDTO getCustomer() {
        return customer;
    }

    public void setCustomer(CustomerDTO customer) {
        this.customer = customer;
    }

    public int getSize() {
        if (items == null || items.isEmpty()) return 0;
        return items.stream().mapToInt(CartItemDTO::getQuantity).sum();
    }

    public void setSize(int size) {
        this.size = size;
    }

    public double getSum() {
        if (items == null || items.isEmpty()) return 0.0;
        return items.stream()
                .mapToDouble(i -> i.getProduct().getPrice() * i.getQuantity())
                .sum();
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }
}
