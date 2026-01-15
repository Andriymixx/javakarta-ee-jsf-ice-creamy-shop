package com.icecreamy.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;

import com.icecreamy.obj.UserRole;

@Entity
@Table(name = "customer")
@NamedQueries({@NamedQuery(name = "getAllCustomers", query = "SELECT c FROM Customer AS c ORDER BY c.customerId asc"),
        @NamedQuery(name = "getCustomerByName", query = "SELECT c FROM Customer AS c WHERE UPPER(c.firstName) LIKE :firstName"),
        @NamedQuery(name = "getCustomerByEmail", query = "SELECT c FROM Customer AS c WHERE c.email = :email"),
        @NamedQuery(name = "getCustomerByEmailAndPassword", query = "SELECT c FROM Customer AS c WHERE c.password = :password AND c.email = :email"),})

public class Customer implements Serializable {
    private static final long serialVersionUID = -3240716900978864647L;

    @Id
    @Column(name = "customer_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int customerId;

    private String firstName;
    private String surName;

    @Column(columnDefinition = "VARCHAR(100)")
    private String password;

    @Column(name = "email", unique = true, updatable = false)
    private String email;

    @OneToOne(fetch = FetchType.LAZY, mappedBy = "customer", cascade = CascadeType.ALL)
    @PrimaryKeyJoinColumn
    private Cart cart;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private UserRole role;

    public Customer() {
    }

    public Customer(String firstName, String surName, String password, String email, UserRole role) {
        super();
        this.firstName = firstName;
        this.surName = surName;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public int getCustomerId() {
        return customerId;
    }

    public void setCustomerId(int customerId) {
        this.customerId = customerId;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSurName() {
        return surName;
    }

    public void setSurName(String surName) {
        this.surName = surName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Cart getCart() {
        return cart;
    }

    public void setCart(Cart cart) {
        this.cart = cart;
    }

    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    public UserRole getRole() {
        return role;
    }

    public void setRole(UserRole role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "Customer [customerId=" + customerId +
                ", firstName=" + firstName + ", surName=" + surName + ", password="
                + password + ", email=" + email + "]";
    }
}
