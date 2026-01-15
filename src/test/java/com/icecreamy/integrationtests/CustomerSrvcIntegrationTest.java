package com.icecreamy.integrationtests;

import static org.junit.jupiter.api.Assertions.*;

import com.icecreamy.dao.impl.*;
import com.icecreamy.entity.*;
import com.icecreamy.obj.ProType;
import com.icecreamy.obj.UserRole;
import com.icecreamy.obj.dto.*;
import com.icecreamy.service.CustomerService;
import com.icecreamy.util.IceCreamyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Date;
import java.util.List;

public class CustomerSrvcIntegrationTest extends BasePersistenceTest {

    private CustomerService customerService;
    private ProductDaoImpl productDao;
    private OrderDaoImpl orderDao;
    private CustomerDaoImpl customerDao;
    private CartDaoImpl cartDao;


    @BeforeEach
    public void setup() throws Exception {
        customerService = new CustomerService();
        productDao = new ProductDaoImpl();
        orderDao = new OrderDaoImpl();
        customerDao = new CustomerDaoImpl();
        cartDao = new CartDaoImpl();

        injectEntityManager(productDao, "em");
        injectEntityManager(orderDao, "em");
        injectEntityManager(customerDao, "em");
        injectEntityManager(cartDao, "em");

        injectField(customerService, "productDaoStub", productDao);
        injectField(customerService, "orderDaoStub", orderDao);
        injectField(customerService, "customerDaoStub", customerDao);
        injectField(customerService, "cartDaoStub", cartDao);
    }

    private void injectField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        java.lang.reflect.Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private Product createTestProduct(String name, double price, ProType type) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(price);
        p.setType(type);
        p.setStock(100);
        p.setQuantity(1);
        p.setDescription("Test Description");
        p.setImagePath("test/path.jpg");
        return p;
    }

    private Customer createTestCustomer(String email) {
        Customer c = new Customer();
        c.setFirstName("Test");
        c.setSurName("User");
        c.setEmail(email);
        c.setPassword("password");
        c.setRole(UserRole.CUSTOMER);
        return c;
    }

    @Test
    public void testGetAllProducts() {
        em.persist(createTestProduct("P1", 10.0, ProType.ICE_CREAM_SANDWICH));
        em.persist(createTestProduct("P2", 20.0, ProType.ICE_CREAM_SANDWICH));
        em.flush();

        List<ProductDTO> result = customerService.getAllProducts();

        assertTrue(result.size() >= 2);
    }

    @Test
    public void testGetAllProductsByType() {
        em.persist(createTestProduct("Sandwich", 10.0, ProType.ICE_CREAM_SANDWICH));
        em.persist(createTestProduct("Other", 20.0, ProType.WAFFLE_CONE));
        em.flush();

        List<ProductDTO> results = customerService.getAllProductsByType(ProType.ICE_CREAM_SANDWICH);

        assertFalse(results.isEmpty());
        for (ProductDTO p : results) {
            assertEquals(ProType.ICE_CREAM_SANDWICH, p.getType());
        }
    }

    @Test
    public void testSearchProductByPriceRange() {
        em.persist(createTestProduct("Cheap Ice", 5.0, ProType.ICE_CREAM_SANDWICH));
        em.persist(createTestProduct("Mid Ice", 25.0, ProType.ICE_CREAM_SANDWICH));
        em.persist(createTestProduct("Expensive Ice", 100.0, ProType.ICE_CREAM_SANDWICH));
        em.flush();

        List<ProductDTO> result = customerService.searchProductByPriceRange(10.0, 50.0);

        assertEquals(1, result.size());
        assertEquals("Mid Ice", result.get(0).getName());
    }

    @Test
    public void testAddToCartIntegration() throws IceCreamyException {
        Product product = createTestProduct("Ice Cream", 10.0, ProType.ICE_CREAM_SANDWICH);
        Customer customer = createTestCustomer("ivan@test.com");
        em.persist(product);
        em.persist(customer);
        em.flush();

        customerService.addToCart(product.getProductId(), customer.getCustomerId());
        em.flush();
        em.clear();

        CartDTO cart = customerService.getCartDTO(customer.getCustomerId());
        assertNotNull(cart.getItems());
        assertEquals(1, cart.getItems().size());
        assertEquals("Ice Cream", cart.getItems().get(0).getProduct().getName());
    }

    @Test
    public void testRemoveFromCartIntegration() throws IceCreamyException {
        Product product = createTestProduct("To Remove", 10.0, ProType.ICE_CREAM_SANDWICH);
        Customer customer = createTestCustomer("remove@test.com");
        em.persist(product);
        em.persist(customer);
        em.flush();

        customerService.addToCart(product.getProductId(), customer.getCustomerId());
        em.flush();

        customerService.removeFromCart(product.getProductId(), customer.getCustomerId());
        em.flush();
        em.clear();

        CartDTO cart = customerService.getCartDTO(customer.getCustomerId());
        assertTrue(cart.getItems() == null || cart.getItems().isEmpty());
    }

    @Test
    public void testCheckoutProcessIntegration() throws IceCreamyException {
        Product p1 = createTestProduct("Product 1", 50.0, ProType.ICE_CREAM_SANDWICH);
        Product p2 = createTestProduct("Product 2", 30.0, ProType.ICE_CREAM_SANDWICH);
        Customer c = createTestCustomer("buyer@test.com");
        em.persist(p1);
        em.persist(p2);
        em.persist(c);
        em.flush();

        customerService.addToCart(p1.getProductId(), c.getCustomerId());
        customerService.addToCart(p2.getProductId(), c.getCustomerId());
        em.flush();

        OrderDTO order = customerService.chackOut(c.getCustomerId());
        em.flush();
        em.clear();

        assertNotNull(order);
        assertEquals(80.0, order.getAmount());

        CartDTO cartAfter = customerService.getCartDTO(c.getCustomerId());
        assertTrue(cartAfter.getItems() == null || cartAfter.getItems().isEmpty());

        long orderCount = em.createQuery("SELECT COUNT(o) FROM Order o WHERE o.customer.customerId = :cid", Long.class)
                .setParameter("cid", c.getCustomerId())
                .getSingleResult();
        assertEquals(1, orderCount);
    }


    @Test
    public void testGetProductByIdSuccess() throws IceCreamyException {
        Product p = createTestProduct("Unique Product", 99.0, ProType.ICE_CREAM_SANDWICH);
        em.persist(p);
        em.flush();
        int savedId = p.getProductId();
        em.clear();

        ProductDTO result = customerService.getProductById(savedId);

        assertNotNull(result);
        assertEquals("Unique Product", result.getName());
    }

    @Test
    public void testGetCustomerOrdersById() {
        Customer c = createTestCustomer("single_order@test.com");
        em.persist(c);
        em.flush();

        Order o = new Order();
        o.setTimestamp(new java.util.Date());
        o.setAmount(150.0);
        o.setCustomer(c);
        em.persist(o);
        em.flush();
        int orderId = o.getOrderId();
        em.clear();

        OrderDTO result = customerService.getCustomerOrdersById(orderId);

        assertNotNull(result);
        assertEquals(150.0, result.getAmount());
    }

    @Test
    public void testGetAllCustomerOrdersById() throws IceCreamyException {
        Customer c = createTestCustomer("orders@test.com");
        em.persist(c);
        em.flush();

        Order o1 = new Order();
        o1.setTimestamp(new Date());
        o1.setAmount(100.0);
        o1.setCustomer(c);

        Order o2 = new Order();
        o2.setTimestamp(new Date());
        o2.setAmount(200.0);
        o2.setCustomer(c);

        em.persist(o1);
        em.persist(o2);
        em.flush();
        em.clear();

        List<OrderDTO> result = customerService.getAllCustomerOrdersById(c.getCustomerId());
        assertEquals(2, result.size());
    }

    @Test
    public void testUpdateQuantityIncrements() throws IceCreamyException {
        Product p = createTestProduct("Update Qty", 10.0, ProType.ICE_CREAM_SANDWICH);
        Customer c = createTestCustomer("qty_inc@test.com");
        em.persist(p);
        em.persist(c);
        em.flush();
        customerService.addToCart(p.getProductId(), c.getCustomerId());
        em.flush();

        CartDTO result = customerService.updateQuantity(p.getProductId(), c.getCustomerId(), 2);

        assertEquals(1, result.getItems().size());
        assertEquals(3, result.getItems().get(0).getQuantity());
    }

    @Test
    public void testUpdateQuantityRemovesIfZeroOrLess() throws IceCreamyException {
        Product p = createTestProduct("Remove Via Qty", 10.0, ProType.ICE_CREAM_SANDWICH);
        Customer c = createTestCustomer("qty_rem@test.com");
        em.persist(p);
        em.persist(c);
        em.flush();
        customerService.addToCart(p.getProductId(), c.getCustomerId());
        em.flush();

        CartDTO result = customerService.updateQuantity(p.getProductId(), c.getCustomerId(), -1);

        assertTrue(result.getItems() == null || result.getItems().isEmpty());
    }

}
