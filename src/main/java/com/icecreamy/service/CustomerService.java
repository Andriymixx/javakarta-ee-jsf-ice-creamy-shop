package com.icecreamy.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.icecreamy.entity.*;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import com.icecreamy.dao.impl.CustomerDaoImpl;
import com.icecreamy.dao.impl.OrderDaoImpl;
import com.icecreamy.dao.impl.ProductDaoImpl;
import com.icecreamy.dao.impl.CartDaoImpl;
import com.icecreamy.obj.ProType;
import com.icecreamy.obj.dto.OrderDTO;
import com.icecreamy.obj.dto.ProductDTO;
import com.icecreamy.obj.dto.CartDTO;
import com.icecreamy.util.AppConstants;
import com.icecreamy.util.IceCreamyException;
import com.icecreamy.util.Utilities;

@Stateless
public class CustomerService {

    @EJB
    private ProductDaoImpl productDaoStub;

    @EJB
    private OrderDaoImpl orderDaoStub;

    @EJB
    private CustomerDaoImpl customerDaoStub;

    @EJB
    private CartDaoImpl cartDaoStub;

    public List<ProductDTO> getAllProducts() {
        List<Product> products = productDaoStub.getAll();
        return Utilities.convertListToOrFromDto(products, ProductDTO.class);
    }

    public List<ProductDTO> searchProductByPriceRange(double minPrice, double maxPrice) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("minPrice", minPrice);
        parameters.put("maxPrice", maxPrice);
        List<Product> products = productDaoStub.getResultListCustomQuery("searchProductByPriceRange", parameters);
        return Utilities.convertListToOrFromDto(products, ProductDTO.class);
    }

    public List<ProductDTO> getAllProductByPriceEx() {
        List<Product> products = productDaoStub.getResultListCustomQuery("getAllProductByPriceEx", null);
        return Utilities.convertListToOrFromDto(products, ProductDTO.class);
    }

    public List<ProductDTO> getAllProductByPriceChe() {
        List<Product> products = productDaoStub.getResultListCustomQuery("getAllProductByPriceChe", null);
        return Utilities.convertListToOrFromDto(products, ProductDTO.class);
    }

    public ProductDTO getProductById(int productId) throws IceCreamyException {
        Optional<Product> optionalProduct = productDaoStub.get(productId);
        Product product = optionalProduct.orElseThrow(() -> new IceCreamyException("Record not found"));
        return Utilities.convertToOrFromDto(product, ProductDTO.class);
    }

    public List<ProductDTO> getAllProductsByType(ProType type) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("type", type);
        List<Product> products = productDaoStub.getResultListCustomQuery("getAllProductsByType", parameters);
        return Utilities.convertListToOrFromDto(products, ProductDTO.class);
    }

    public List<OrderDTO> getAllCustomerOrdersById(int customerId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("customerId", customerId);
        List<Order> orders = orderDaoStub.getResultListCustomQuery("getOrdersByCustomerId", parameters);
        return Utilities.convertListToOrFromDto(orders, OrderDTO.class);
    }

    public OrderDTO getCustomerOrdersById(int orderId) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("orderId", orderId);
        Order order = orderDaoStub.getResultCustomQuery("getOrderById", parameters);
        return Utilities.convertToOrFromDto(order, OrderDTO.class);
    }

    public CartDTO addToCart(int productId, int customerId) throws IceCreamyException {
        Optional<Product> optionalProduct = productDaoStub.get(productId);
        Product product = optionalProduct.orElseThrow(() -> new IceCreamyException("Product not found"));

        Cart customerCart = this.getCustomerCart(customerId);

        Optional<CartItem> existingItem = customerCart.getItems().stream()
                .filter(item -> item.getProduct().getProductId() == productId)
                .findFirst();

        if (existingItem.isPresent()) {
            existingItem.get().setQuantity(existingItem.get().getQuantity() + 1);
        } else {
            CartItem newItem = new CartItem(customerCart, product, 1);
            customerCart.getItems().add(newItem);
        }

        cartDaoStub.update(customerCart);
        return Utilities.convertToOrFromDto(customerCart, CartDTO.class);
    }

    public CartDTO updateQuantity(int productId, int customerId, int delta) throws IceCreamyException {
        Cart customerCart = this.getCustomerCart(customerId);

        java.util.Iterator<CartItem> iterator = customerCart.getItems().iterator();
        while (iterator.hasNext()) {
            CartItem item = iterator.next();
            if (item.getProduct().getProductId() == productId) {
                int newQty = item.getQuantity() + delta;

                if (newQty > 0) {
                    item.setQuantity(newQty);
                } else {
                    iterator.remove();
                }
            }
        }

        cartDaoStub.update(customerCart);
        return Utilities.convertToOrFromDto(customerCart, CartDTO.class);
    }

    public CartDTO removeFromCart(int productId, int customerId) throws IceCreamyException {
        Cart customerCart = this.getCustomerCart(customerId);

        customerCart.getItems().removeIf(item -> item.getProduct().getProductId() == productId);

        cartDaoStub.update(customerCart);
        return Utilities.convertToOrFromDto(customerCart, CartDTO.class);
    }

    public CartDTO getCartDTO(int customerId) throws IceCreamyException {
        Cart customerCart = this.getCustomerCart(customerId);
        return Utilities.convertToOrFromDto(customerCart, CartDTO.class);
    }

    private Cart getCustomerCart(int customerId) throws IceCreamyException {
        Optional<Cart> cartOptional = cartDaoStub.get(customerId);
        if (!cartOptional.isPresent()) {
            Optional<Customer> customerOptional = customerDaoStub.get(customerId);
            Customer customer = customerOptional.orElseThrow(() -> new IceCreamyException("Customer not found", customerId));

            Cart customerCart = new Cart(customer);
            customerCart = cartDaoStub.create(customerCart);

            customer.setCart(customerCart);
            customerDaoStub.update(customer);
            return customerCart;
        }
        return cartOptional.get();
    }

    public OrderDTO chackOut(int customerId) throws IceCreamyException {
        Optional<Customer> customerOptional = customerDaoStub.get(customerId);
        Customer customer = customerOptional.orElseThrow(() -> new IceCreamyException("Record not found", customerId));

        Cart customerCart = this.getCustomerCart(customerId);
        List<CartItem> cartItems = customerCart.getItems();

        if (cartItems != null && !cartItems.isEmpty()) {
            Order order = new Order();
            order.setTimestamp(new Date());
            order.setCustomer(customer);

            List<OrderItem> orderItems = new ArrayList<>();
            double totalAmount = 0;

            for (CartItem cartItem : cartItems) {
                OrderItem orderItem = new OrderItem(
                        order,
                        cartItem.getProduct(),
                        cartItem.getQuantity()
                );
                orderItems.add(orderItem);
                totalAmount += orderItem.getPriceAtPurchase() * orderItem.getQuantity();
            }
            order.setItems(orderItems);
            order.setAmount(totalAmount);

            orderDaoStub.create(order);

            customerCart.getItems().clear();
            cartDaoStub.update(customerCart);

            return Utilities.convertToOrFromDto(order, OrderDTO.class);
        }

        throw new IceCreamyException(AppConstants.EMPTY_CART);
    }
}
