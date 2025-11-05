package a23088.controller;

import a23088.entity.Orders;
import a23088.entity.Users;
import a23088.mbean.OrdersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.List;

@Named("ordersBean")
@SessionScoped
public class OrdersBean implements Serializable {

    @EJB
    private OrdersFacadeLocal ordersFacade;
    
    @Inject
    private LoginBean loginBean;

    private List<Orders> userOrders;
    private Orders selectedOrder;

    public OrdersBean() {
    }

    // Initialize orders for current user
    public void init() {
        try {
            System.out.println("=== ORDERS BEAN INIT START ===");
            System.out.println("LoginBean is null: " + (loginBean == null));
            
            if (loginBean != null) {
                System.out.println("isLoggedIn: " + loginBean.isLoggedIn());
                
                if (loginBean.isLoggedIn()) {
                    Users currentUser = loginBean.getCurrentUser();
                    System.out.println("Current user: " + (currentUser != null ? currentUser.getFullName() : "NULL"));
                    
                    if (currentUser != null) {
                        System.out.println("User ID: " + currentUser.getUserID());
                        System.out.println("Calling ordersFacade.findByUserID...");
                        
                        userOrders = ordersFacade.findByUserID(currentUser.getUserID());
                        
                        System.out.println("Orders result: " + (userOrders != null ? userOrders.size() : "NULL"));
                        if (userOrders != null) {
                            for (Orders order : userOrders) {
                                System.out.println("Order ID: " + order.getOrderID() + ", Date: " + order.getOrderDate());
                            }
                        }
                    } else {
                        System.out.println("Current user is NULL!");
                    }
                } else {
                    System.out.println("User is not logged in!");
                }
            } else {
                System.out.println("LoginBean is NULL!");
            }
            
            System.out.println("=== ORDERS BEAN INIT END ===");
            
        } catch (Exception e) {
            System.err.println("ERROR in OrdersBean.init(): " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Lỗi khi tải danh sách đơn hàng: " + e.getMessage());
        }
    }
    
    // Method to be called when page loads
    public void onPageLoad() {
        init();
    }

    // Get orders for current user
    public List<Orders> getUserOrders() {
        if (userOrders == null) {
            init();
        }
        return userOrders;
    }

    // Check if user has orders
    public boolean hasOrders() {
        return userOrders != null && !userOrders.isEmpty();
    }

    // Get order count
    public int getOrderCount() {
        return userOrders != null ? userOrders.size() : 0;
    }

    // Format price
    public String formatPrice(double price) {
        return String.format("%,.0f", price);
    }

    // Convert English status to Vietnamese for display
    public String getStatusDisplay(String status) {
        if (status == null) return "Unknown";
        
        switch (status.toLowerCase()) {
            case "pending":
                return "pending";
            case "confirmed":
                return "confirmed";
            case "cancelled":
                return "cancelled";
            default:
                return status;
        }
    }

    // Get status badge class
    public String getStatusBadgeClass(String status) {
        if (status == null) return "badge-secondary";
        
        switch (status.toLowerCase()) {
            case "pending":
                return "badge-warning";
            case "confirmed":
                return "badge-info";
            case "shipped":
                return "badge-primary";
            case "delivered":
                return "badge-success";
            case "cancelled":
                return "badge-danger";
            default:
                return "badge-secondary";
        }
    }

    // Get selected order
    public Orders getSelectedOrder() {
        return selectedOrder;
    }

    public void setSelectedOrder(Orders selectedOrder) {
        this.selectedOrder = selectedOrder;
    }
    
    // Get order items for selected order
    public java.util.Collection<a23088.entity.OrderItems> getSelectedOrderItems() {
        if (selectedOrder != null && selectedOrder.getOrderItemsCollection() != null) {
            return selectedOrder.getOrderItemsCollection();
        }
        return new java.util.ArrayList<>();
    }
    
    // Calculate total items in order
    public int getTotalItemsInOrder(Orders order) {
        if (order != null && order.getOrderItemsCollection() != null) {
            return order.getOrderItemsCollection().stream()
                    .mapToInt(item -> item.getQuantity())
                    .sum();
        }
        return 0;
    }

    // View order detail - navigate to viewDetail page
    public String viewOrderDetail(Orders order) {
        this.selectedOrder = order;
        return "viewDetail?faces-redirect=true&orderId=" + order.getOrderID();
    }
    
    // Load order by ID for detail page
    public void loadOrderById() {
        try {
            String orderIdParam = FacesContext.getCurrentInstance()
                    .getExternalContext().getRequestParameterMap().get("orderId");
            if (orderIdParam != null) {
                Integer orderId = Integer.parseInt(orderIdParam);
                selectedOrder = ordersFacade.find(orderId);
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Lỗi khi tải thông tin đơn hàng: " + e.getMessage());
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
}
