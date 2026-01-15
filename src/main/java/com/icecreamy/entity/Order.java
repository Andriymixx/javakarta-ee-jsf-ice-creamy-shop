package com.icecreamy.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.persistence.*;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@Entity
@Table(name = "orders")
@NamedQueries({@NamedQuery(name = "getAllOrders", query = "SELECT o FROM Order  AS o ORDER BY o.orderId ASC"),
        @NamedQuery(name = "getOrderById", query = "SELECT o FROM Order  AS o WHERE o.orderId = :orderId"),
        @NamedQuery(name = "getOrdersByCustomerId", query = "SELECT o FROM Order  AS o WHERE o.customer.customerId = :customerId"),

})
public class Order implements Serializable {
    private static final long serialVersionUID = -6300981392612885499L;

    @Id
    @Column(name = "order_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int orderId;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "create_date")
    private Date timestamp;

    private double amount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "customer_order",
            joinColumns = {@JoinColumn(name = "order_id")},
            inverseJoinColumns = {@JoinColumn(name = "customer_id")},
            uniqueConstraints = @UniqueConstraint(columnNames = {"order_id", "customer_id"}))

    @JsonIdentityInfo(generator = ObjectIdGenerators.PropertyGenerator.class, property = "customerId")
    @JsonIdentityReference(alwaysAsId = true)
    @JsonProperty("customerId")
    private Customer customer;

    @Transient
    private int size;

    @Transient
    private int sum;

    public Order() {

    }

    public Order(Date timestamp, double amount, Customer customer) {
        super();
        this.timestamp = timestamp;
        this.amount = amount;
        this.customer = customer;
    }

    public int getOrderId() {
        return orderId;
    }

    public void setOrderId(int orderId) {
        this.orderId = orderId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getSize() {
        if (items == null) return 0;
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getSum() {
        if (items == null) return 0;
        return (int) items.stream()
                .mapToDouble(i -> i.getPriceAtPurchase() * i.getQuantity())
                .sum();
    }

    public void setSum(int sum) {
        this.sum = sum;
    }

    @Override
    public String toString() {
        return "Order [orderId=" + orderId + ", timestamp=" + timestamp + ", amount=" + amount + "]";
    }

}
