package com.icecreamy.beans;

import com.icecreamy.obj.ProType;
import com.icecreamy.obj.dto.ProductDTO;
import com.icecreamy.service.AdminService;
import com.icecreamy.obj.WebResponse;
import com.icecreamy.service.CustomerService;
import com.icecreamy.util.AppConstants;
import com.icecreamy.util.IceCreamyException;
import com.icecreamy.util.Utilities;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.security.RolesAllowed;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;
import org.apache.commons.collections.bag.SynchronizedSortedBag;

import java.io.*;
import java.util.List;

@Named("adminBean")
@ViewScoped
public class AdminBean implements Serializable {

    @EJB
    private AdminService adminService;
    @EJB
    private CustomerService customerService;
    @Inject
    private SessionBean sessionBean;
    private ProductDTO newProduct = new ProductDTO();
    private Part uploadedFile;
    private Part editUploadedFile;
    private ProductDTO selectedProduct;
    private List<ProductDTO> allProducts;
    private Integer selectedProductId;
    private Integer selectedEditProductId;
    private ProductDTO editingProduct = new ProductDTO();

    private boolean updateName;
    private boolean updateItalianName;
    private boolean updateUkrainianName;
    private boolean updateDescription;
    private boolean updatePrice;
    private boolean updateOldPrice;
    private boolean updateStock;
    private boolean updateType;
    private boolean updateImage;
    private boolean updateAccentColor;
    private boolean updateGradientStyle;
    private boolean updateImagePath;


    @PostConstruct
    public void init() {
        if (!isAdmin()) {
            FacesContext context = FacesContext.getCurrentInstance();
            context.addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_ERROR,
                    "Доступ обмежено!",
                    "Ця сторінка доступна тільки адміністратору. Будь ласка, авторизуйтесь."
            ));
            context.getExternalContext().getFlash().setKeepMessages(true);
        } else {
            loadProducts();
        }
    }

    private boolean checkAdminAccess() {
        if (!isAdmin()) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(
                    FacesMessage.SEVERITY_WARN,
                    "Доступ обмежено!",
                    "Ця дія доступна тільки адміністратору системи."
            ));
            return false;
        }
        return true;
    }

    private boolean isAdmin() {
        return sessionBean != null && sessionBean.isLoggedIn() &&
                sessionBean.getCurrentUser().getRole() == com.icecreamy.obj.UserRole.ADMIN;
    }

    public void loadProducts() {
        this.allProducts = customerService.getAllProducts();
    }

    public void prepareEdit(ProductDTO prod) {
        this.editingProduct = Utilities.convertToOrFromDto(prod, ProductDTO.class);
    }

    public void updateProduct() {
        if (!checkAdminAccess()) return;
        try {
            ProductDTO currentProduct = customerService.getProductById(editingProduct.getProductId());
            if (!updateName) editingProduct.setName(currentProduct.getName());
            if (!updateItalianName) editingProduct.setItalianName(currentProduct.getItalianName());
            if (!updateUkrainianName) editingProduct.setUkrainianName(currentProduct.getUkrainianName());
            if (!updateDescription) editingProduct.setDescription(currentProduct.getDescription());
            if (!updatePrice) editingProduct.setPrice(currentProduct.getPrice());
            if (!updateOldPrice) editingProduct.setOldPrice(currentProduct.getOldPrice());
            if (!updateType) editingProduct.setType(currentProduct.getType());
            if (!updateAccentColor) editingProduct.setAccentColor(currentProduct.getAccentColor());
            if (!updateGradientStyle) editingProduct.setGradientStyle(currentProduct.getGradientStyle());
            if (!updateStock) editingProduct.setStock(currentProduct.getStock());
            if (updateImagePath) {
                if (editUploadedFile != null && editUploadedFile.getSize() > 0) {
                    String fileName = getFileName(editUploadedFile);
                    editingProduct.setImagePath("rest/admin/images/" + fileName);
                    saveFileToDisk(editUploadedFile, fileName);
                    System.out.println("DEBUG: Завантажено нову картинку: " + fileName);
                } else {
                    editingProduct.setImagePath(currentProduct.getImagePath());
                }
            } else {
                editingProduct.setImagePath(currentProduct.getImagePath());
            }
            adminService.updateProduct(editingProduct);
            loadProducts();
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Успіх", "Товар [" + editingProduct.getItalianName() + "] оновлено!"));
            resetCheckboxes();
            this.editingProduct = new ProductDTO();
            this.selectedEditProductId = null;
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Помилка", e.getMessage()));
        }
    }

    private void resetCheckboxes() {
        updateItalianName = false;
        updateUkrainianName = false;
        updateDescription = false;
        updatePrice = false;
        updateOldPrice = false;
        updateStock = false;
        updateType = false;
        updateImage = false;
        updateAccentColor = false;
        updateGradientStyle = false;
        updateImagePath = false;
    }

    public void deleteSelectedProduct() {
        if (!checkAdminAccess()) return;
        if (selectedProductId != null) {
            try {
                adminService.removeProduct(selectedProductId);
                loadProducts();
                selectedProductId = null;
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Видалено", "Товар успішно видалено"));
            } catch (Exception e) {
                FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Помилка", e.getMessage()));
            }
        }
    }

    public String createProduct() {
        if (!checkAdminAccess()) return null;
        try {
            if (uploadedFile != null && uploadedFile.getSize() > 0) {
                String fileName = getFileName(uploadedFile);
                System.out.println("DEBUG: Saving file: " + fileName);
                newProduct.setImagePath("rest/admin/images/" + fileName);
                saveFileToDisk(uploadedFile, fileName);
            } else {
                System.out.println("DEBUG: No file uploaded or file is empty");
            }
            adminService.createProduct(newProduct);
            loadProducts();
            FacesContext.getCurrentInstance().getExternalContext().getFlash().setKeepMessages(true);
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Успіх", "Товар [" + newProduct.getItalianName() + "] додано!"));
            newProduct = new ProductDTO();
            return "admin?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Помилка", e.getMessage()));
            return null;
        }
    }

    private String getFileName(Part part) {
        String fileName = part.getSubmittedFileName();
        if (fileName != null) {
            return fileName.substring(fileName.lastIndexOf('/') + 1).substring(fileName.lastIndexOf('\\') + 1);
        }
        return "unknown_" + System.currentTimeMillis();
    }

    private void saveFileToDisk(Part part, String fileName) throws Exception {
        File uploadDir = new File(AppConstants.UPLOAD_FILE_PATH);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }
        File file = new File(uploadDir, fileName);
        try (InputStream input = part.getInputStream(); OutputStream output = new FileOutputStream(file)) {
            byte[] buffer = new byte[16384];
            int length;
            while ((length = input.read(buffer)) != -1) {
                output.write(buffer, 0, length);
            }
        }
    }

    public void loadProductToEdit() {
        System.out.println("DEBUG: Отримано ID для редагування: " + selectedEditProductId);
        if (selectedEditProductId != null && allProducts != null) {
            this.editingProduct = allProducts.stream().filter(p -> p.getProductId() == selectedEditProductId.intValue()).findFirst().orElse(new ProductDTO());
            System.out.println("DEBUG: Завантажено товар: " + editingProduct.getItalianName());
        }
    }

    public ProductDTO getNewProduct() {
        return newProduct;
    }

    public void setNewProduct(ProductDTO newProduct) {
        this.newProduct = newProduct;
    }

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public ProType[] getProductTypes() {
        return ProType.values();
    }

    public List<ProductDTO> getAllProducts() {
        return allProducts;
    }

    public Integer getSelectedProductId() {
        return selectedProductId;
    }

    public void setSelectedProductId(Integer selectedProductId) {
        this.selectedProductId = selectedProductId;
    }

    public ProductDTO getEditingProduct() {
        return editingProduct;
    }

    public void setEditingProduct(ProductDTO editingProduct) {
        this.editingProduct = editingProduct;
    }

    public Part getEditUploadedFile() {
        return editUploadedFile;
    }

    public void setEditUploadedFile(Part editUploadedFile) {
        this.editUploadedFile = editUploadedFile;
    }

    public ProductDTO getSelectedProduct() {
        return selectedProduct;
    }

    public void setSelectedProduct(ProductDTO selectedProduct) {
        this.selectedProduct = selectedProduct;
    }

    public Integer getSelectedEditProductId() {
        return selectedEditProductId;
    }

    public void setSelectedEditProductId(Integer selectedEditProductId) {
        this.selectedEditProductId = selectedEditProductId;
    }

    public boolean isUpdateName() {
        return updateName;
    }

    public void setUpdateName(boolean updateName) {
        this.updateName = updateName;
    }

    public boolean isUpdateItalianName() {
        return updateItalianName;
    }

    public void setUpdateItalianName(boolean updateItalianName) {
        this.updateItalianName = updateItalianName;
    }

    public boolean isUpdateUkrainianName() {
        return updateUkrainianName;
    }

    public void setUpdateUkrainianName(boolean updateUkrainianName) {
        this.updateUkrainianName = updateUkrainianName;
    }

    public boolean isUpdateDescription() {
        return updateDescription;
    }

    public void setUpdateDescription(boolean updateDescription) {
        this.updateDescription = updateDescription;
    }

    public boolean isUpdateOldPrice() {
        return updateOldPrice;
    }

    public void setUpdateOldPrice(boolean updateOldPrice) {
        this.updateOldPrice = updateOldPrice;
    }

    public boolean isUpdatePrice() {
        return updatePrice;
    }

    public void setUpdatePrice(boolean updatePrice) {
        this.updatePrice = updatePrice;
    }

    public boolean isUpdateStock() {
        return updateStock;
    }

    public void setUpdateStock(boolean updateStock) {
        this.updateStock = updateStock;
    }

    public boolean isUpdateType() {
        return updateType;
    }

    public void setUpdateType(boolean updateType) {
        this.updateType = updateType;
    }

    public boolean isUpdateImage() {
        return updateImage;
    }

    public void setUpdateImage(boolean updateImage) {
        this.updateImage = updateImage;
    }

    public boolean isUpdateAccentColor() {
        return updateAccentColor;
    }

    public void setUpdateAccentColor(boolean updateAccentColor) {
        this.updateAccentColor = updateAccentColor;
    }

    public boolean isUpdateGradientStyle() {
        return updateGradientStyle;
    }

    public void setUpdateGradientStyle(boolean updateGradientStyle) {
        this.updateGradientStyle = updateGradientStyle;
    }

    public boolean isUpdateImagePath() {
        return updateImagePath;
    }

    public void setUpdateImagePath(boolean updateImagePath) {
        this.updateImagePath = updateImagePath;
    }
}