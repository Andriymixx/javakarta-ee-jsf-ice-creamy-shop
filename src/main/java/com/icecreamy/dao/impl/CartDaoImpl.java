package com.icecreamy.dao.impl;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.icecreamy.entity.Cart;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import com.icecreamy.dao.DaoBase;

@Stateless
public class CartDaoImpl extends DaoBase<Cart> {

    @PersistenceContext(unitName = "icecreamy")
    private EntityManager em;

    @Override
    public Cart create(Cart cart) {
        em.persist(cart);
        return cart;
    }

    @Override
    public Cart update(Cart cart) {
        em.merge(cart);
        return cart;
    }

    @Override
    public boolean delete(int cartId) {
        Optional<Cart> optionalCart = this.get(cartId);
        if (optionalCart.isPresent()) {
            em.remove(optionalCart.get());
            return true;
        }
        return false;
    }

    @Override
    public Optional<Cart> get(int cartId) {
        Cart cart = em.find(Cart.class, cartId);
        return Optional.ofNullable(cart);
    }

    @Override
    public List<Cart> getAll() {
        return em.createNamedQuery("getAllCarts", Cart.class).getResultList();
    }

    @Override
    public Cart getResultCustomQuery(String namedQuery, Map<String, Object> parameters) {
        TypedQuery<Cart> namedQueryStatement = em.createNamedQuery(namedQuery, Cart.class);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> namedQueryStatement.setParameter(k, v));
        }
        return namedQueryStatement.getSingleResult();
    }

    @Override
    public List<Cart> getResultListCustomQuery(String namedQuery, Map<String, Object> parameters) {
        TypedQuery<Cart> namedQueryStatement = em.createNamedQuery(namedQuery, Cart.class);
        if (parameters != null && !parameters.isEmpty()) {
            parameters.forEach((k, v) -> namedQueryStatement.setParameter(k, v));
        }
        return namedQueryStatement.getResultList();
    }
}
