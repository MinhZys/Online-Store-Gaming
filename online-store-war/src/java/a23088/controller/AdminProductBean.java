package a23088.controller;

import a23088.entity.Products;
import a23088.entity.Categories;
import a23088.entity.Supplier;
import a23088.mbean.ProductsFacadeLocal;
import a23088.mbean.CategoriesFacadeLocal;
import a23088.mbean.SupplierFacadeLocal;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.ejb.EJB;
import jakarta.servlet.http.Part;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

@Named(value = "adminProductBean")
@SessionScoped
public class AdminProductBean implements Serializable {

    @EJB
    private ProductsFacadeLocal productsFacade;

    @EJB
    private CategoriesFacadeLocal categoriesFacade;

    @EJB
    private SupplierFacadeLocal supplierFacade;

    private Products product = new Products();
    private List<Products> productList;
    private Integer selectedCategoryId;
    private Integer selectedSupplierId;
    private Integer selectedProductId;

    // Upload ảnh - không lưu trong session để tránh serialization issues
    private transient Part file;

    public AdminProductBean() {
    }

    // Lấy toàn bộ sản phẩm
    public List<Products> getProductList() {
        try {
            if (productList == null) {
                productList = productsFacade.findAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
            productList = new java.util.ArrayList<>();
        }
        return productList;
    }

    // Lấy sản phẩm chưa đăng bán (status = false)
    public List<Products> getUnpublishedProducts() {
        try {
            return productsFacade.findUnpublished();
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    // Lấy sản phẩm đã đăng bán (status = true)
    public List<Products> getPublishedProducts() {
        try {
            return productsFacade.findPublished();
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    // Lấy danh mục
    public List<Categories> getCategories() {
        try {
            return categoriesFacade.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    // Lấy nhà cung cấp
    public List<Supplier> getSuppliers() {
        try {
            return supplierFacade.findAll();
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    // Thêm hoặc cập nhật sản phẩm
    public String saveProduct() {
        try {
            // Validate dữ liệu đầu vào
            String validationError = validateProductData();
            if (validationError != null) {
                // Hiển thị lỗi validation qua JSF
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erorr Validation", validationError));
                System.out.println("Validation Error: " + validationError);
                return "admin_products";
            }

            // Set category and supplier from selected IDs
            if (selectedCategoryId != null) {
                Categories category = categoriesFacade.find(selectedCategoryId);
                if (category != null) {
                    product.setCategoryID(category);
                }
            }

            if (selectedSupplierId != null) {
                Supplier supplier = supplierFacade.find(selectedSupplierId);
                if (supplier != null) {
                    product.setSupplierID(supplier);
                }
            }

            // Validate file upload
            if (file != null && file.getSize() > 0) {
                String fileValidationError = validateUploadedFile();
                if (fileValidationError != null) {
                    // Hiển thị lỗi file validation qua JSF
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error File Upload", fileValidationError));
                    System.out.println("File Validation Error: " + fileValidationError);
                    return "admin_products";
                }

                // Lưu ảnh vào thư mục asset/images
                String fileName = getFilename(file);
                String uploadDir = "C:\\Users\\ADMIN\\Desktop\\cuembeta\\online-store\\online-store\\online-store-war\\web\\asset\\images\\";

                // Tạo thư mục nếu chưa tồn tại
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }

                File targetFile = new File(uploadDir + fileName);

                try (InputStream input = file.getInputStream(); FileOutputStream output = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = input.read(buffer)) > 0) {
                        output.write(buffer, 0, length);
                    }
                }
                product.setImage(fileName);
            }

            // Kiểm tra xem là thêm mới hay cập nhật
            boolean isNewProduct = (product.getProductID() == null || product.getProductID() <= 0);

            if (isNewProduct) {
                // Set createdAt and status for new product
                product.setCreatedAt(new java.util.Date());
                product.setStatus(false); // Default is not published yet
                productsFacade.create(product);
                // Display success message
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                                "New product added: " + product.getProductName()));
                System.out.println("New product added: " + product.getProductName());
            } else {
                // Update existing product
                productsFacade.edit(product);
                // Display success message
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                                "Product updated: " + product.getProductName()));
                System.out.println("Product updated: " + product.getProductName());
            }

            return "admin_products?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            // Hiển thị lỗi hệ thống qua JSF
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error",
                            "An error occurred while saving the product: " + e.getMessage()));
            System.out.println("Error while saving product: " + e.getMessage());

            return "admin_products";
        } finally {
            // Reset form và file
            productList = null; // reload danh sách
            product = new Products(); // reset form
            selectedCategoryId = null;
            selectedSupplierId = null;
            file = null; // Reset file to prevent serialization issues
        }
    }

    // Sửa
    public String editProduct() {
        try {
            if (selectedProductId != null) {
                product = productsFacade.find(selectedProductId);
                if (product != null) {
                    if (product.getCategoryID() != null) {
                        selectedCategoryId = product.getCategoryID().getCategoryID();
                    }
                    if (product.getSupplierID() != null) {
                        selectedSupplierId = product.getSupplierID().getSupplierID();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin_products";
    }

    public String deleteProduct() {
        try {
            if (selectedProductId != null) {
                Products p = productsFacade.find(selectedProductId);
                if (p != null) {
                    // Do not allow deletion when the product is currently published
                    if (Boolean.TRUE.equals(p.getStatus())) {
                        FacesContext.getCurrentInstance().addMessage(null,
                                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Cannot Delete",
                                        "The product is currently published. Please unpublish it before deleting."));
                        return "admin_products"; // Stop delete operation
                    }
                    String productName = p.getProductName();
                    productsFacade.remove(p);
                    // Display success message
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                                    "Product deleted: " + productName));
                    System.out.println("Deleted product ID = " + selectedProductId);
                } else {
                    // Display error message
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                    "Product not found with ID = " + selectedProductId));
                    System.out.println("Product not found with ID = " + selectedProductId);
                }
            } else {
                // Display error message
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "No product selected for deletion"));
                System.out.println("selectedProductId = null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Display system error via JSF
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error",
                            "An error occurred while deleting the product: " + e.getMessage()));
            System.out.println("Error while deleting product: " + e.getMessage());
        } finally {
            // Reset list to reload
            productList = null;
            selectedProductId = null;
        }
        return "admin_products?faces-redirect=true";
    }

    // Đăng bán sản phẩm (set status = true)
    public String publishProduct() {
        try {
            if (selectedProductId != null) {
                Products p = productsFacade.find(selectedProductId);
                if (p != null) {
                    p.setStatus(true); // Publish product
                    productsFacade.edit(p);
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                                    "Product published: " + p.getProductName()));
                    System.out.println("Published product ID = " + selectedProductId);
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                    "Product not found with ID = " + selectedProductId));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "No product selected to publish"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error",
                            "An error occurred while publishing the product: " + e.getMessage()));
        } finally {
            productList = null; // Reset to reload
        }
        return "admin_products";
    }

    // Hủy đăng bán sản phẩm (set status = false)
    public String unpublishProduct() {
        try {
            if (selectedProductId != null) {
                Products p = productsFacade.find(selectedProductId);
                if (p != null) {
                    p.setStatus(false); // Unpublish product
                    productsFacade.edit(p);
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                                    "Product unpublished: " + p.getProductName()));
                    System.out.println("Unpublished product ID = " + selectedProductId);
                } else {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                    "Product not found with ID = " + selectedProductId));
                }
            } else {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                                "No product selected to unpublish"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error",
                            "An error occurred while unpublishing the product: " + e.getMessage()));
        } finally {
            productList = null; // Reset to reload
        }
        return "admin_products";
    }

    // Reset form
    // Reset form
    public String resetForm() {
        try {
            product = new Products();
            selectedCategoryId = null;
            selectedSupplierId = null;
            selectedProductId = null;
            file = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "admin_products";
    }

    // Lấy tên file upload
    private String getFilename(Part part) {
        try {
            String contentDisposition = part.getHeader("content-disposition");
            if (contentDisposition != null) {
                for (String cd : contentDisposition.split(";")) {
                    if (cd.trim().startsWith("filename")) {
                        return cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "uploaded_file_" + System.currentTimeMillis();
    }

    // Validate dữ liệu sản phẩm
    private String validateProductData() {
        // Validate product name
        if (product.getProductName() == null || product.getProductName().trim().isEmpty()) {
            return "Product name cannot be empty";
        }
        if (product.getProductName().trim().length() < 2) {
            return "Product name must be at least 2 characters long";
        }
        if (product.getProductName().trim().length() > 100) {
            return "Product name must not exceed 100 characters";
        }

        // Validate category
        if (selectedCategoryId == null) {
            return "Please select a product category";
        }

        // Validate supplier
        if (selectedSupplierId == null) {
            return "Please select a supplier";
        }

        // Validate description
        if (product.getDescription() == null || product.getDescription().trim().isEmpty()) {
            return "Product description cannot be empty";
        }
        if (product.getDescription().trim().length() < 10) {
            return "Product description must be at least 10 characters long";
        }
        if (product.getDescription().trim().length() > 500) {
            return "Product description must not exceed 500 characters";
        }

        // Validate price
        if (product.getPrice() <= 0) {
            return "Product price must be greater than 0";
        }
        if (product.getPrice() > 1000000000) { // 1 billion VND
            return "Product price must not exceed 1,000,000,000 VND";
        }

        // Validate stock
        if (product.getStock() < 0) {
            return "Stock quantity cannot be negative";
        }
        if (product.getStock() > 10000) {
            return "Stock quantity must not exceed 10,000";
        }

        // Validate duplicate product name (only when adding new product)
        if (product.getProductID() == null || product.getProductID() <= 0) {
            if (isProductNameExists(product.getProductName().trim())) {
                return "Product name already exists in the system";
            }
        }

        return null; // No errors
    }

// Validate file upload
    private String validateUploadedFile() {
        if (file == null || file.getSize() == 0) {
            return null; // No file uploaded, no validation needed
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !isValidImageType(contentType)) {
            return "Only image files are allowed (JPG, PNG, GIF)";
        }

        // Check file name
        String fileName = getFilename(file);
        if (fileName == null || fileName.trim().isEmpty()) {
            return "Invalid file name";
        }

        // Check file extension
        if (!hasValidImageExtension(fileName)) {
            return "File must have .jpg, .jpeg, .png or .gif extension";
        }

        return null; // No errors
    }

    // Kiểm tra loại file ảnh hợp lệ
    private boolean isValidImageType(String contentType) {
        return contentType.equals("image/jpeg")
                || contentType.equals("image/jpg")
                || contentType.equals("image/png")
                || contentType.equals("image/gif");
    }

    // Kiểm tra extension file ảnh hợp lệ
    private boolean hasValidImageExtension(String fileName) {
        String lowerFileName = fileName.toLowerCase();
        return lowerFileName.endsWith(".jpg")
                || lowerFileName.endsWith(".jpeg")
                || lowerFileName.endsWith(".png")
                || lowerFileName.endsWith(".gif");
    }

    // Kiểm tra tên sản phẩm đã tồn tại chưa
    private boolean isProductNameExists(String productName) {
        try {
            List<Products> existingProducts = productsFacade.findAll();
            for (Products existingProduct : existingProducts) {
                if (existingProduct.getProductName() != null
                        && existingProduct.getProductName().trim().equalsIgnoreCase(productName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Save product for selling (customer)
    public String saveSellProduct() {
        try {
            // Validate input data
            String validationError = validateProductData();
            if (validationError != null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error Validation", validationError));
                return "sell-product";
            }

            // Set category and supplier from selected IDs
            if (selectedCategoryId != null) {
                Categories category = categoriesFacade.find(selectedCategoryId);
                if (category != null) {
                    product.setCategoryID(category);
                }
            }

            if (selectedSupplierId != null) {
                Supplier supplier = supplierFacade.find(selectedSupplierId);
                if (supplier != null) {
                    product.setSupplierID(supplier);
                }
            }

            // Validate file upload
            if (file != null && file.getSize() > 0) {
                String fileValidationError = validateUploadedFile();
                if (fileValidationError != null) {
                    FacesContext.getCurrentInstance().addMessage(null,
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error File Upload", fileValidationError));
                    return "sell-product";
                }

                // Save image to asset/images directory
                String fileName = getFilename(file);
                String uploadDir = "C:\\Users\\ADMIN\\Desktop\\cuembeta\\online-store\\online-store\\online-store-war\\web\\asset\\images\\";

                // Create directory if not exists
                File uploadDirFile = new File(uploadDir);
                if (!uploadDirFile.exists()) {
                    uploadDirFile.mkdirs();
                }

                File targetFile = new File(uploadDir + fileName);

                try (InputStream input = file.getInputStream(); FileOutputStream output = new FileOutputStream(targetFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = input.read(buffer)) > 0) {
                        output.write(buffer, 0, length);
                    }
                }
                product.setImage(fileName);
            }

            // Set as new product for selling
            product.setCreatedAt(new java.util.Date());
            product.setStatus(true); // Active
            productsFacade.create(product);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success",
                            "Your product has been successfully published: " + product.getProductName()));

            return "index?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error",
                            "An error occurred while publishing the product: " + e.getMessage()));
            return "sell-product";
        } finally {
            productList = null;
            product = new Products();
            selectedCategoryId = null;
            selectedSupplierId = null;
            file = null;
        }
    }

    // Preview product before selling
    public String previewSellProduct() {
        try {
            // Validate data first
            String validationError = validateProductData();
            if (validationError != null) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi Validation", validationError));
                return "sell-product";
            }

            // Set category and supplier if selected
            if (selectedCategoryId != null) {
                Categories category = categoriesFacade.find(selectedCategoryId);
                if (category != null) {
                    product.setCategoryID(category);
                }
            }

            if (selectedSupplierId != null) {
                Supplier supplier = supplierFacade.find(selectedSupplierId);
                if (supplier != null) {
                    product.setSupplierID(supplier);
                }
            }

            // Show preview message
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Preview",
                            "The product is ready to be published. Click 'Publish now' to proceed."));

            return "sell-product";
        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error",
                            "An error occurred while generating preview: " + e.getMessage()));
            return "sell-product";
        }

    }

    // Getters & Setters
    public Products getProduct() {
        if (product == null) {
            product = new Products();
        }
        return product;
    }

    public void setProduct(Products product) {
        this.product = product;
    }

    public Part getFile() {
        return file;
    }

    public void setFile(Part file) {
        this.file = file;
    }

    public Integer getSelectedCategoryId() {
        return selectedCategoryId;
    }

    public void setSelectedCategoryId(Integer selectedCategoryId) {
        this.selectedCategoryId = selectedCategoryId;
    }

    public Integer getSelectedSupplierId() {
        return selectedSupplierId;
    }

    public void setSelectedSupplierId(Integer selectedSupplierId) {
        this.selectedSupplierId = selectedSupplierId;
    }

    public Integer getSelectedProductId() {
        return selectedProductId;
    }

    public void setSelectedProductId(Integer selectedProductId) {
        this.selectedProductId = selectedProductId;
    }

}
