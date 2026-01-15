package com.icecreamy.service;

import java.util.List;

import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;

import com.icecreamy.dao.impl.CustomerDaoImpl;
import com.icecreamy.dao.impl.ProductDaoImpl;
import com.icecreamy.entity.Customer;
import com.icecreamy.entity.Product;
import com.icecreamy.obj.dto.CustomerDTO;
import com.icecreamy.obj.dto.ProductDTO;
import com.icecreamy.util.IceCreamyException;
import com.icecreamy.util.Utilities;

@Stateless
public class AdminService {

    @EJB
    private ProductDaoImpl productDaoStub;

    @EJB
    private CustomerDaoImpl customerDaoStub;

    public ProductDTO createProduct(ProductDTO productDto) {
        Product newProduct = Utilities.convertToOrFromDto(productDto, Product.class);
        newProduct = productDaoStub.create(newProduct);
        return Utilities.convertToOrFromDto(newProduct, ProductDTO.class);
    }

    public void removeProduct(int productId) throws IceCreamyException {
        try {
            productDaoStub.delete(productId);
        } catch (Exception e) {
            throw new IceCreamyException("Не вдалося видалити продукт", productId);
        }
    }

    public List<CustomerDTO> getAllCustomers() {
        try {
            List<Customer> allCustomers = customerDaoStub.getAll();
            return Utilities.convertListToOrFromDto(allCustomers, CustomerDTO.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    public void updateProduct(ProductDTO productDto) {
        Product entity = Utilities.convertToOrFromDto(productDto, Product.class);
        productDaoStub.update(entity);
    }
}
