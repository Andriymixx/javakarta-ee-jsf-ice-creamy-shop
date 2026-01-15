package com.icecreamy.dao.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import com.icecreamy.dao.DaoBase;
import com.icecreamy.entity.Product;

@Stateless
public class ProductDaoImpl extends DaoBase<Product> {

    @PersistenceContext(unitName = "icecreamy")
    private EntityManager em;

    @Override
    public Product create(Product product) {
        em.persist(product);
        return product;
    }

    @Override
    public Product update(Product product) {
        em.merge(product);
        return product;
    }

    @Override
    public boolean delete(int productId) {
        Optional<Product> optionalProduct = this.get(productId);
        if (optionalProduct.isPresent()) {
            em.remove(optionalProduct.get());
            return true;
        }
        return false;
    }

    @Override
    public Optional<Product> get(int productId) {
        Product product = em.find(Product.class, productId);
        return Optional.of(product);
    }

    @Override
    public List<Product> getAll() {
        return em.createNamedQuery("getAllProducts", Product.class).getResultList();
    }

    @Override
    public Product getResultCustomQuery(String namedQuery, Map<String, Object> parameters) {
        TypedQuery<Product> namedQueryStatement = em.createNamedQuery(namedQuery, Product.class);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> namedQueryStatement.setParameter(k, v));
        }
        return namedQueryStatement.getSingleResult();
    }

    @Override
    public List<Product> getResultListCustomQuery(String namedQuery, Map<String, Object> parameters) {
        TypedQuery<Product> namedQueryStatement = em.createNamedQuery(namedQuery, Product.class);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> namedQueryStatement.setParameter(k, v));
        }
        return namedQueryStatement.getResultList();
    }
}
