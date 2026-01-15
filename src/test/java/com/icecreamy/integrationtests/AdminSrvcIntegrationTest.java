package com.icecreamy.integrationtests;

import static org.junit.jupiter.api.Assertions.*;

import com.icecreamy.dao.impl.CustomerDaoImpl;
import com.icecreamy.dao.impl.ProductDaoImpl;
import com.icecreamy.entity.Customer;
import com.icecreamy.entity.Product;
import com.icecreamy.obj.ProType;
import com.icecreamy.obj.UserRole;
import com.icecreamy.obj.dto.CustomerDTO;
import com.icecreamy.obj.dto.ProductDTO;
import com.icecreamy.service.AdminService;
import com.icecreamy.util.IceCreamyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

public class AdminSrvcIntegrationTest extends BasePersistenceTest {

    private AdminService adminService;
    private CustomerDaoImpl customerDao;
    private ProductDaoImpl productDao;

    @BeforeEach
    public void init() throws Exception {
        adminService = new AdminService();
        customerDao = new CustomerDaoImpl();
        productDao = new ProductDaoImpl();

        injectEntityManager(customerDao, "em");
        injectEntityManager(productDao, "em");

        Field customerField = AdminService.class.getDeclaredField("customerDaoStub");
        customerField.setAccessible(true);
        customerField.set(adminService, customerDao);

        Field productField = AdminService.class.getDeclaredField("productDaoStub");
        productField.setAccessible(true);
        productField.set(adminService, productDao);
    }

    @Test
    public void testCreateProductWithSetters() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Caramel Fusion");
        dto.setPrice(12.50);
        dto.setStock(100);
        dto.setType(ProType.WAFFLE_CONE);
        dto.setUkrainianName("Карамельний Мікс");

        ProductDTO saved = adminService.createProduct(dto);

        assertNotNull(saved);
        assertTrue(saved.getProductId() > 0);

        Product dbProduct = em.find(Product.class, saved.getProductId());
        assertEquals("Caramel Fusion", dbProduct.getName());
        assertEquals(12.50, dbProduct.getPrice());
    }

    @Test
    public void testUpdateProductLogic() {
        ProductDTO dto = new ProductDTO();
        dto.setName("Initial Name");
        dto.setPrice(5.0);
        ProductDTO saved = adminService.createProduct(dto);

        saved.setName("Updated Name");
        saved.setPrice(15.0);
        adminService.updateProduct(saved);

        em.flush();
        em.clear();

        Product updated = em.find(Product.class, saved.getProductId());
        assertEquals("Updated Name", updated.getName());
        assertEquals(15.0, updated.getPrice());
    }

    @Test
    public void testRemoveProductSuccess() throws IceCreamyException {
        Product product = new Product();
        product.setName("Delete Me");
        product.setPrice(1.0);
        product.setStock(1);
        em.persist(product);
        int id = product.getProductId();

        adminService.removeProduct(id);

        em.flush();
        em.clear();

        Product result = em.find(Product.class, id);
        assertNull(result, "Продукт має бути видалений");
    }

}