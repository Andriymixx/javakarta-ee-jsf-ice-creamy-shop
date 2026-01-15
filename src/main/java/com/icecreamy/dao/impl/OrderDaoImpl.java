package com.icecreamy.dao.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import com.icecreamy.dao.DaoBase;
import com.icecreamy.entity.Order;

@Stateless
public class OrderDaoImpl extends DaoBase<Order> {

    @PersistenceContext(unitName = "icecreamy")
    private EntityManager em;

    @Override
    public Order create(Order order) {
        em.persist(order);
        return order;
    }

    @Override
    public Optional<Order> get(int orderId) {
        Order order = em.find(Order.class, orderId);
        return Optional.of(order);
    }

    @Override
    public List<Order> getAll() {
        return em.createNamedQuery("getAllOrders", Order.class).getResultList();
    }

    @Override
    public Order update(Order t) {
        return null;
    }

    @Override
    public boolean delete(int id) {
        return false;
    }

    @Override
    public Order getResultCustomQuery(String namedQuery, Map<String, Object> parameters) {
        TypedQuery<Order> namedQueryStatement = em.createNamedQuery(namedQuery, Order.class);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> namedQueryStatement.setParameter(k, v));
        }
        return namedQueryStatement.getSingleResult();
    }

    @Override
    public List<Order> getResultListCustomQuery(String namedQuery, Map<String, Object> parameters) {
        TypedQuery<Order> namedQueryStatement = em.createNamedQuery(namedQuery, Order.class);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> namedQueryStatement.setParameter(k, v));
        }
        return namedQueryStatement.getResultList();
    }
}
