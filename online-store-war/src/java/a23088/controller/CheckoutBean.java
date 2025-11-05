package a23088.controller;

import a23088.entity.Orders;
import a23088.entity.OrderItems;
import a23088.mbean.OrdersFacadeLocal;
import a23088.mbean.OrderItemsFacadeLocal;
import a23088.service.OrderUpdateService;
import jakarta.ejb.EJB;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("checkoutBean")
@SessionScoped
public class CheckoutBean implements Serializable {

    @EJB
    private OrdersFacadeLocal ordersFacade;
    
    @EJB
    private OrderItemsFacadeLocal orderItemsFacade;
    
    @EJB
    private OrderUpdateService orderUpdateService;

    private Orders currentOrder;
    private String address;
    private String phone;

    public CheckoutBean() {
    }

    // Initialize checkout with order from session
    public void init() {
        try {
            // Get order from session
            currentOrder = (Orders) FacesContext.getCurrentInstance()
                    .getExternalContext().getSessionMap().get("currentOrder");
            
            if (currentOrder != null) {
                // Load order items from database
                List<OrderItems> orderItems = orderItemsFacade.findByOrderID(currentOrder.getOrderID());
                currentOrder.setOrderItemsCollection(orderItems);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Format price
    public String formatPrice(double price) {
        return String.format("%,.0f", price);
    }

    // Calculate total price for an order item
    public double getItemTotalPrice(OrderItems item) {
        return item.getUnitPrice() * item.getQuantity();
    }

    // Convert English status to Vietnamese for display
    public String getStatusDisplay(String status) {
        if (status == null) return "Không xác định";
        
        switch (status.toLowerCase()) {
            case "pending":
                return "Đang chờ xác nhận";
            case "confirmed":
                return "Đã xác nhận";
            case "shipped":
                return "Đã giao hàng";
            case "delivered":
                return "Đã nhận hàng";
            case "cancelled":
                return "Đã hủy";
            default:
                return status;
        }
    }

    // Xác nhận đơn hàng và cập nhật thông tin
    public String confirmOrder() {
        try {
            if (currentOrder == null) {
                addErrorMessage("Không tìm thấy đơn hàng!");
                return null;
            }

            if (address == null || address.trim().isEmpty()) {
                addErrorMessage("Vui lòng nhập địa chỉ giao hàng!");
                return null;
            }

            if (phone == null || phone.trim().isEmpty()) {
                addErrorMessage("Vui lòng nhập số điện thoại!");
                return null;
            }

            // Thử cập nhật trực tiếp trước
            try {
                currentOrder.setAddress(address.trim());
                currentOrder.setPhone(phone.trim());
                currentOrder.setStatus("Pending"); // Sử dụng Pending thay vì Confirmed
                ordersFacade.edit(currentOrder);
                
                // Log thông báo thành công ra console
                System.out.println("==========================================");
                System.out.println("🎉 ĐẶT HÀNG THÀNH CÔNG! 🎉");
                System.out.println("==========================================");
                System.out.println("Mã đơn hàng: #" + currentOrder.getOrderID());
                System.out.println("Khách hàng: " + currentOrder.getUserID().getFullName());
                System.out.println("Email: " + currentOrder.getUserID().getEmail());
                System.out.println("Địa chỉ: " + currentOrder.getAddress());
                System.out.println("Số điện thoại: " + currentOrder.getPhone());
                System.out.println("Tổng tiền: " + String.format("%,.0f", (double)currentOrder.getTotalAmount()) + "₫");
                System.out.println("Trạng thái: " + currentOrder.getStatus());
                System.out.println("Thời gian: " + new java.util.Date());
                System.out.println("==========================================");
                
                // Hiển thị popup thông báo thành công bằng JavaScript
                String successMessage = "🎉 ĐẶT HÀNG THÀNH CÔNG! 🎉\n\n" +
                    "Mã đơn hàng: #" + currentOrder.getOrderID() + "\n" +
                    "Tổng tiền: " + String.format("%,.0f", (double)currentOrder.getTotalAmount()) + "₫\n" +
                    "Cảm ơn bạn đã mua sắm!";
                
                // Thêm script để hiển thị alert
                FacesContext.getCurrentInstance().getExternalContext().getRequestMap().put("successMessage", successMessage);
                
                addSuccessMessage("Đơn hàng đã được xác nhận thành công!");
                return "index?faces-redirect=true&success=true"; // Chuyển về trang chủ với parameter success
                
            } catch (Exception e1) {
                System.err.println("Direct update failed: " + e1.getMessage());
                e1.printStackTrace();
                
                // Thử với OrderUpdateService
                try {
                    boolean success = orderUpdateService.updateOrderInfo(
                        currentOrder.getOrderID(), 
                        address.trim(), 
                        phone.trim()
                    );
                    
                    if (success) {
                        addSuccessMessage("Đơn hàng đã được xác nhận thành công!");
                        return "index?faces-redirect=true&success=true"; // Chuyển về trang chủ với parameter success
                    } else {
                        addErrorMessage("Không thể cập nhật đơn hàng!");
                        return null;
                    }
                } catch (Exception e2) {
                    System.err.println("Service update also failed: " + e2.getMessage());
                    e2.printStackTrace();
                    addErrorMessage("Lỗi khi cập nhật đơn hàng: " + e2.getMessage());
                    return null;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Lỗi khi xác nhận đơn hàng: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " - Nguyên nhân: " + e.getCause().getMessage();
            }
            addErrorMessage(errorMessage);
            return null;
        }
    }

    // Message handling methods
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_INFO,
                        message, null));
    }

    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null,
                new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR,
                        message, null));
    }

    // Getters and Setters
    public Orders getCurrentOrder() {
        return currentOrder;
    }

    public void setCurrentOrder(Orders currentOrder) {
        this.currentOrder = currentOrder;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}