package com.icecreamy.dao.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import com.icecreamy.dao.DaoBase;
import com.icecreamy.entity.Customer;

@Stateless
public class CustomerDaoImpl extends DaoBase<Customer> {

    @PersistenceContext(unitName = "icecreamy")
    private EntityManager em;

    @Override
    public Customer create(Customer customer) {
        em.persist(customer);
        return customer;
    }

    @Override
    public Customer update(Customer customer) {
        return em.merge(customer);
    }

    @Override
    public Optional<Customer> get(int customerId) {
        Customer customer = em.find(Customer.class, customerId);
        return Optional.of(customer);
    }

    @Override
    public List<Customer> getAll() {
        return em.createNamedQuery("getAllCustomers", Customer.class).getResultList();
    }

    @Override
    public boolean delete(int customerId) {
        Optional<Customer> optionalCustomer = this.get(customerId);
        if (optionalCustomer.isPresent()) {
            em.remove(optionalCustomer.get());
            return true;
        }
        return false;
    }

    @Override
    public Customer getResultCustomQuery(String namedQuery, Map<String, Object> parameters) {
        TypedQuery<Customer> namedQueryStatement = em.createNamedQuery(namedQuery, Customer.class);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> namedQueryStatement.setParameter(k, v));
        }
        return namedQueryStatement.getResultList().stream().findFirst().orElse(null);
    }

    @Override
    public List<Customer> getResultListCustomQuery(String namedQuery, Map<String, Object> parameters) {
        TypedQuery<Customer> namedQueryStatement = em.createNamedQuery(namedQuery, Customer.class);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> namedQueryStatement.setParameter(k, v));
        }
        return namedQueryStatement.getResultList();
    }
}
