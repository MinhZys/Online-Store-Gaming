package a23088.controller;

import a23088.entity.Supplier;
import a23088.mbean.SupplierFacadeLocal;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.List;

@Named(value = "adminSupplierBean")
@SessionScoped
public class AdminSupplierBean implements Serializable {

    @EJB
    private SupplierFacadeLocal supplierFacade;

    private Supplier supplier = new Supplier();
    private List<Supplier> supplierList;
    private Integer selectedSupplierId;

    public AdminSupplierBean() {
    }

    // Lấy toàn bộ nhà cung cấp
    public List<Supplier> getSupplierList() {
        try {
            if (supplierList == null) {
                supplierList = supplierFacade.findAll();
            }
        } catch (Exception e) {
            e.printStackTrace();
            supplierList = new java.util.ArrayList<>();
        }
        return supplierList;
    }

    // Thêm hoặc cập nhật nhà cung cấp
    public String saveSupplier() {
        try {
            // Validate dữ liệu đầu vào
            String validationError = validateSupplierData();
            if (validationError != null) {
                // Hiển thị lỗi validation qua JSF
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi Validation", validationError));
                System.out.println("Validation Error: " + validationError);
                return "suppliers";
            }

            // Kiểm tra xem là thêm mới hay cập nhật
            boolean isNewSupplier = (supplier.getSupplierID() == null || supplier.getSupplierID() <= 0);
            
            if (isNewSupplier) {
                supplierFacade.create(supplier);
                // Hiển thị thông báo thành công
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", 
                        "Đã thêm nhà cung cấp mới: " + supplier.getSupplierName()));
                System.out.println("Đã thêm nhà cung cấp mới: " + supplier.getSupplierName());
            } else {
                supplierFacade.edit(supplier);
                // Hiển thị thông báo thành công
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", 
                        "Đã cập nhật nhà cung cấp: " + supplier.getSupplierName()));
                System.out.println("Đã cập nhật nhà cung cấp: " + supplier.getSupplierName());
            }
            
            return "suppliers?faces-redirect=true";
        } catch (Exception e) {
            e.printStackTrace();
            // Hiển thị lỗi hệ thống qua JSF
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi Hệ thống", 
                    "Có lỗi xảy ra khi lưu nhà cung cấp: " + e.getMessage()));
            System.out.println("Lỗi khi lưu nhà cung cấp: " + e.getMessage());
            return "suppliers";
        } finally {
            // Reset form
            supplierList = null; // reload danh sách
            supplier = new Supplier(); // reset form
            selectedSupplierId = null;
        }
    }

    // Sửa nhà cung cấp
    public String editSupplier() {
        try {
            if (selectedSupplierId != null) {
                supplier = supplierFacade.find(selectedSupplierId);
                if (supplier != null) {
                    System.out.println("Đã load nhà cung cấp để sửa: " + supplier.getSupplierName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "suppliers";
    }

    // Xóa nhà cung cấp
    public String deleteSupplier() {
        try {
            if (selectedSupplierId != null) {
                Supplier s = supplierFacade.find(selectedSupplierId);
                if (s != null) {
                    String supplierName = s.getSupplierName();
                    
                    // Kiểm tra xem nhà cung cấp có sản phẩm nào không
                    if (s.getProductsCollection() != null && !s.getProductsCollection().isEmpty()) {
                        FacesContext.getCurrentInstance().addMessage(null, 
                            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", 
                                "Không thể xóa nhà cung cấp '" + supplierName + "' vì còn có sản phẩm liên quan"));
                        return "suppliers";
                    }
                    
                    supplierFacade.remove(s);
                    // Hiển thị thông báo thành công
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", 
                            "Đã xóa nhà cung cấp: " + supplierName));
                    System.out.println("Đã xóa nhà cung cấp ID = " + selectedSupplierId);
                } else {
                    // Hiển thị thông báo lỗi
                    FacesContext.getCurrentInstance().addMessage(null, 
                        new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", 
                            "Không tìm thấy nhà cung cấp ID = " + selectedSupplierId));
                    System.out.println("Không tìm thấy nhà cung cấp ID = " + selectedSupplierId);
                }
            } else {
                // Hiển thị thông báo lỗi
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", 
                        "Không có nhà cung cấp nào được chọn để xóa"));
                System.out.println("selectedSupplierId = null");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Hiển thị lỗi hệ thống qua JSF
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi Hệ thống", 
                    "Có lỗi xảy ra khi xóa nhà cung cấp: " + e.getMessage()));
            System.out.println("Lỗi khi xóa nhà cung cấp: " + e.getMessage());
        } finally {
            // Reset danh sách để reload
            supplierList = null;
            selectedSupplierId = null;
        }
        return "suppliers?faces-redirect=true";
    }

    // Reset form
    public String resetForm() {
        try {
            supplier = new Supplier();
            selectedSupplierId = null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "suppliers";
    }

    // Validate dữ liệu nhà cung cấp
    private String validateSupplierData() {
        // Validate tên nhà cung cấp
        if (supplier.getSupplierName() == null || supplier.getSupplierName().trim().isEmpty()) {
            return "Tên nhà cung cấp không được để trống";
        }
        if (supplier.getSupplierName().trim().length() < 2) {
            return "Tên nhà cung cấp phải có ít nhất 2 ký tự";
        }
        if (supplier.getSupplierName().trim().length() > 100) {
            return "Tên nhà cung cấp không được vượt quá 100 ký tự";
        }
        
        // Validate số lượng
        if (supplier.getQuantity() < 0) {
            return "Số lượng không được âm";
        }
        if (supplier.getQuantity() > 10000) {
            return "Số lượng không được vượt quá 10,000";
        }
        
        // Validate tên nhà cung cấp trùng lặp (chỉ khi thêm mới)
        if (supplier.getSupplierID() == null || supplier.getSupplierID() <= 0) {
            if (isSupplierNameExists(supplier.getSupplierName().trim())) {
                return "Tên nhà cung cấp đã tồn tại trong hệ thống";
            }
        }
        
        return null; // Không có lỗi
    }
    
    // Kiểm tra tên nhà cung cấp đã tồn tại chưa
    private boolean isSupplierNameExists(String supplierName) {
        try {
            List<Supplier> existingSuppliers = supplierFacade.findAll();
            for (Supplier existingSupplier : existingSuppliers) {
                if (existingSupplier.getSupplierName() != null && 
                    existingSupplier.getSupplierName().trim().equalsIgnoreCase(supplierName)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // Getters & Setters
    public Supplier getSupplier() {
        if (supplier == null) {
            supplier = new Supplier();
        }
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public Integer getSelectedSupplierId() {
        return selectedSupplierId;
    }

    public void setSelectedSupplierId(Integer selectedSupplierId) {
        this.selectedSupplierId = selectedSupplierId;
    }
}
