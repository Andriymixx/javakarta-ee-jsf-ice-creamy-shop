package com.icecreamy.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "cart")
@NamedQueries({
        @NamedQuery(name = "getAllCarts", query = "SELECT w FROM Cart  AS w ORDER BY w.cartId ASC"),
})

public class Cart implements Serializable {
    private static final long serialVersionUID = 250885203288254412L;

    @Id
    private int cartId;
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @OrderBy("id ASC")
    private List<CartItem> items = new ArrayList<>();
    @OneToOne
    @MapsId
    @JoinColumn(name = "customer_id")
    @JsonIgnore
    private Customer customer;

    @Transient
    private int size;

    @Transient
    private int sum;

    public Cart() {
    }

    public Cart(Customer customer) {
        this.customer = customer;
    }

    public double calculateTotal() {
        if (items == null) return 0.0;
        return items.stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();
    }

    public int getTotalQuantity() {
        if (items == null) return 0;
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    public int getCartId() {
        return cartId;
    }

    public void setCartId(int cartId) {
        this.cartId = cartId;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getSize() {
        return getTotalQuantity();
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSum() {
        return (int) calculateTotal();
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }

    @Override
    public String toString() {
        return "Cart [cartId=" + cartId + "]";
    }
}
