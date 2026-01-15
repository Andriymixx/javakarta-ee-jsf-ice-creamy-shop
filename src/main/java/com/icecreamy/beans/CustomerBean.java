package com.icecreamy.beans;

import com.icecreamy.obj.ProType;
import com.icecreamy.obj.dto.CartDTO;
import com.icecreamy.obj.dto.CartItemDTO;
import com.icecreamy.obj.dto.OrderDTO;
import com.icecreamy.obj.dto.ProductDTO;
import com.icecreamy.service.CustomerService;
import com.icecreamy.util.IceCreamyException;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Named("customerBean")
@ViewScoped
public class CustomerBean implements Serializable {

    @EJB
    private CustomerService customerService;
    @Inject
    private SessionBean sessionBean;
    private List<ProductDTO> allProducts;
    private List<ProductDTO> products;
    private List<OrderDTO> userOrders;
    private OrderDTO selectedOrder;
    private CartDTO cart;
    private String query;
    private ProductDTO selectedProduct;
    private int sortState = 0;
    private double minPrice = 0;
    private double maxPrice = 200;
    private Map<String, Boolean> typeSelections = new HashMap<>();
    private List<ProductDTO> featuredProducts;
    private int featuredIndex = 0;

    private Map<String, Boolean> tempTypeSelections = new HashMap<>();
    private double tempMinPrice;
    private double tempMaxPrice;

    private Integer productId;
    private int productQty = 1;

    @PostConstruct
    public void init() {
        for (ProType type : ProType.values()) {
            typeSelections.put(type.name(), false);
        }
        resetTempFilters();
        loadAllProducts();
        loadUserOrders();
        List<Integer> targetIds = Arrays.asList(1, 5, 3, 4);

        this.featuredProducts = new ArrayList<>();
        if (allProducts != null) {
            for (Integer id : targetIds) {
                allProducts.stream()
                        .filter(p -> p.getProductId() == id)
                        .findFirst()
                        .ifPresent(p -> featuredProducts.add(p));
            }
        }

        if (this.featuredProducts.isEmpty() && allProducts.size() > 0) {
            int limit = Math.min(allProducts.size(), 4);
            this.featuredProducts.addAll(allProducts.subList(0, limit));
        }
        try {
            loadCart();
        } catch (IceCreamyException e) {
            System.err.println("Помилка завантаження кошика в init: " + e.getMessage());
        }
    }

    public void loadCart() throws IceCreamyException {
        if (sessionBean.isLoggedIn()) {
            this.cart = customerService.getCartDTO(sessionBean.getCurrentUser().getCustomerId());
        }
    }

    public void addToCart(int productId) {
        try {
            if (!sessionBean.isLoggedIn()) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                        FacesMessage.SEVERITY_WARN,
                        "Увага!",
                        "Будь ласка, увійдіть в акаунт, щоб додавати товари у кошик."
                ));
                return;
            }
            int customerId = sessionBean.getCurrentUser().getCustomerId();
            customerService.addToCart(productId, customerId);
            loadCart();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_INFO,
                    "Успіх",
                    "Товар додано до кошика!"
            ));
        } catch (IceCreamyException e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Помилка",
                    e.getMessage()
            ));
        }
    }

    public int getQuantityInCart(int productId) {
        if (cart == null || cart.getItems() == null) return 0;
        return cart.getItems().stream()
                .filter(item -> item.getProduct().getProductId() == productId)
                .findFirst()
                .map(CartItemDTO::getQuantity)
                .orElse(0);
    }

    public void updateQuantity(int productId, int delta) {
        try {
            int customerId = sessionBean.getCurrentUser().getCustomerId();
            this.cart = customerService.updateQuantity(productId, customerId, delta);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void removeFromCart(int productId) {
        try {
            int customerId = sessionBean.getCurrentUser().getCustomerId();
            this.cart = customerService.removeFromCart(productId, customerId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String checkout() {
        try {
            if (!sessionBean.isLoggedIn()) return "login?faces-redirect=true";

            customerService.chackOut(sessionBean.getCurrentUser().getCustomerId());
            this.cart = null;
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    public void loadUserOrders() {
        if (sessionBean.isLoggedIn()) {
            int customerId = sessionBean.getCurrentUser().getCustomerId();
            this.userOrders = customerService.getAllCustomerOrdersById(customerId);
        }
    }

    public void viewOrderDetails(Integer orderId) {
        this.selectedOrder = customerService.getCustomerOrdersById(orderId);
    }

    public void updateProfile() {
    }

    private void resetTempFilters() {
        for (ProType type : ProType.values()) {
            tempTypeSelections.put(type.name(), typeSelections.getOrDefault(type.name(), false));
        }
        this.tempMinPrice = this.minPrice;
        this.tempMaxPrice = this.maxPrice;
    }

    public List<OrderDTO> getUserOrders() {
        return userOrders;
    }

    public OrderDTO getSelectedOrder() {
        return selectedOrder;
    }

    public double getTempMaxPrice() {
        return tempMaxPrice;
    }

    public void setTempMaxPrice(double tempMaxPrice) {
        this.tempMaxPrice = tempMaxPrice;
    }

    public double getTempMinPrice() {
        return tempMinPrice;
    }

    public void setTempMinPrice(double tempMinPrice) {
        this.tempMinPrice = tempMinPrice;
    }

    public Map<String, Boolean> getTempTypeSelections() {
        return tempTypeSelections;
    }

    public void setTempTypeSelections(Map<String, Boolean> tempTypeSelections) {
        this.tempTypeSelections = tempTypeSelections;
    }

    public void commitFilters() {
        this.typeSelections = new HashMap<>(tempTypeSelections);
        this.minPrice = tempMinPrice;
        this.maxPrice = tempMaxPrice;
        applyAllFilters();
    }

    public long getTempFiltersCount() {
        long count = tempTypeSelections.values().stream().filter(b -> b).count();
        if (tempMinPrice > 0 || tempMaxPrice < 200) {
            count++;
        }
        return count;
    }

    public void switchFeaturedProduct(int index) {
        if (index >= 0 && index < featuredProducts.size()) {
            this.featuredIndex = index;
        }
    }

    public ProductDTO getCurrentFeaturedProduct() {
        if (featuredProducts != null && !featuredProducts.isEmpty()) {
            return featuredProducts.get(featuredIndex);
        }
        return null;
    }

    public void loadAllProducts() {
        this.allProducts = customerService.getAllProducts();
        applyAllFilters();
    }

    public String getProductTypeLabel(String typeName) {
        switch (typeName) {
            case "WAFFLE_CONE":
                return "Вафельні ріжки";
            case "ICE_CREAM_SANDWICH":
                return "Сендвічі";
            case "POPSICLE":
                return "На паличці";
            case "PAPER_CUP":
                return "В паперовому стаканчику";
            case "PACKAGED_ICE_CREAM":
                return "Фасоване морозиво";
            default:
                return "Інше";
        }
    }

    public void applyAllFilters() {
        List<String> activeTypes = typeSelections.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        this.products = allProducts.stream()
                .filter(p -> activeTypes.isEmpty() || activeTypes.contains(p.getType().name()))
                .filter(p -> p.getPrice() >= minPrice && p.getPrice() <= maxPrice)
                .filter(p -> query == null || query.isEmpty() || p.getName().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());

        sortProducts();
    }

    private void sortProducts() {
        if (sortState == 1) {
            products.sort(Comparator.comparingDouble(ProductDTO::getPrice));
        } else if (sortState == 2) {
            products.sort((p1, p2) -> Double.compare(p2.getPrice(), p1.getPrice()));
        }
    }

    public void toggleSortByPrice() {
        sortState = (sortState + 1) % 3;
        if (sortState == 0) {
            applyAllFilters();
        } else {
            sortProducts();
        }
    }

    public void search() {
        applyAllFilters();
    }

    public void applyFilters() {
        applyAllFilters();
    }

    public void searchByPriceRange() {
        applyAllFilters();
    }

    public void resetFilters() {
        this.minPrice = 0;
        this.maxPrice = 200;
        this.sortState = 0;
        this.query = "";
        for (ProType type : ProType.values()) {
            typeSelections.put(type.name(), false);
        }

        this.tempMinPrice = 0;
        this.tempMaxPrice = 200;
        this.tempTypeSelections = new HashMap<>();
        for (ProType type : ProType.values()) {
            tempTypeSelections.put(type.name(), false);
        }
        applyAllFilters();
    }

    public long getActiveFiltersCount() {
        if (typeSelections == null) {
            return 0;
        }
        return typeSelections.values().stream()
                .filter(Boolean::booleanValue)
                .count();
    }

    public void loadSelectedProduct() {
        if (productId != null) {
            try {
                this.selectedProduct = customerService.getProductById(productId);
                this.productQty = 1;
            } catch (Exception e) {
            }
        }
    }

    public void incrementQty() {
        productQty++;
    }

    public void decrementQty() {
        if (productQty > 1) productQty--;
    }

    public Map<String, Boolean> getTypeSelections() {
        return typeSelections;
    }

    public List<ProductDTO> getProducts() {
        return products;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public ProductDTO getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(ProductDTO selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public int getSortState() {
        return sortState;
    }

    public double getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(double minPrice) {
        this.minPrice = minPrice;
    }

    public double getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(double maxPrice) {
        this.maxPrice = maxPrice;
    }

    public List<ProductDTO> getFeaturedProducts() {
        return featuredProducts;
    }

    public int getFeaturedIndex() {
        return featuredIndex;
    }

    public void selectProduct(int id) {
        try {
            this.selectedProduct = customerService.getProductById(id);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setSortState(int newState) {
        this.sortState = newState;
        applyAllFilters();
    }

    public CartDTO getCart() {
        return cart;
    }

    public void setCart(CartDTO cart) {
        this.cart = cart;
    }

    public void setFeaturedIndex(int featuredIndex) {
        this.featuredIndex = featuredIndex;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public int getProductQty() {
        return productQty;
    }

    public void setProductQty(int productQty) {
        this.productQty = productQty;
    }
}