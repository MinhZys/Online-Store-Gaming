package a23088.controller;

import a23088.entity.Orders;
import a23088.entity.OrderItems;
import a23088.mbean.OrdersFacadeLocal;
import a23088.mbean.OrderItemsFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named("adminOrdersBean")
@SessionScoped
public class AdminOrdersBean implements Serializable {

    @EJB
    private OrdersFacadeLocal ordersFacade;

    @EJB
    private OrderItemsFacadeLocal orderItemsFacade;

    private List<Orders> allOrders;
    private Orders selectedOrder;
    private String statusFilter = "all";
    private String searchKeyword = "";

    public AdminOrdersBean() {
    }

    /** 🔹 Load all orders list */
    public void loadOrders() {
        try {
            allOrders = ordersFacade.findAll();
            if (allOrders == null) {
                allOrders = new ArrayList<>();
            }
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error loading orders list: " + e.getMessage());
            allOrders = new ArrayList<>();
        }
    }

    /** 🔹 Get all orders list (lazy load) */
    public List<Orders> getAllOrders() {
        if (allOrders == null) {
            loadOrders();
        }
        return allOrders;
    }

    /** 🔹 Filter and search orders */
    public List<Orders> getFilteredOrders() {
        List<Orders> orders = getAllOrders();

        // Filter by status
        if (statusFilter != null && !statusFilter.equals("all")) {
            orders = orders.stream()
                    .filter(o -> o.getStatus() != null && o.getStatus().equalsIgnoreCase(statusFilter))
                    .collect(Collectors.toList());
        }

        // Search by keyword
        if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
            String keyword = searchKeyword.toLowerCase();
            orders = orders.stream()
                    .filter(order ->
                            (order.getOrderID() != null && order.getOrderID().toString().contains(keyword))
                            || (order.getUserID() != null && order.getUserID().getFullName() != null
                                && order.getUserID().getFullName().toLowerCase().contains(keyword))
                            || (order.getPhone() != null && order.getPhone().toLowerCase().contains(keyword))
                            || (order.getAddress() != null && order.getAddress().toLowerCase().contains(keyword))
                    )
                    .collect(Collectors.toList());
        }

        return orders;
    }

    /** 🔹 Update order status */
    public void updateOrderStatus(Orders order, String newStatus) {
        try {
            order.setStatus(newStatus);
            ordersFacade.edit(order);
            addSuccessMessage("Order status updated successfully!");
            loadOrders();
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error updating status: " + e.getMessage());
        }
    }

    public void confirmOrder(Orders order) {
        updateOrderStatus(order, "confirmed");
    }

    public void cancelOrder(Orders order) {
        updateOrderStatus(order, "cancelled");
    }

    /** 🔹 Delete order */
    public void deleteOrder(Orders order) {
        try {
            if (order.getOrderItemsCollection() != null) {
                for (OrderItems item : order.getOrderItemsCollection()) {
                    orderItemsFacade.remove(item);
                }
            }
            ordersFacade.remove(order);
            addSuccessMessage("Order deleted successfully!");
            loadOrders();
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error deleting order: " + e.getMessage());
        }
    }

    /** 🔹 Get order items list */
    public List<OrderItems> getOrderItems(Orders order) {
        if (order != null && order.getOrderItemsCollection() != null) {
            return new ArrayList<>(order.getOrderItemsCollection());
        }
        return new ArrayList<>();
    }

    /** 🔹 Show order detail modal */
    public String viewOrderDetail(Orders order) {
        this.selectedOrder = order;
        return "order-detail?faces-redirect=true&orderId=" + order.getOrderID();
    }

    /** 🔹 Format price */
    public String formatPrice(double price) {
        return String.format("%,.0f $", price);
    }

    /** 🔹 Display status in Vietnamese */
    public String statusDisplay(String status) {
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

    /** 🔹 CSS badge for status */
    public String statusBadgeClass(String status) {
        if (status == null) return "badge-secondary";
        switch (status.toLowerCase()) {
            case "pending":
                return "badge bg-warning text-dark";
            case "confirmed":
                return "badge bg-info text-dark";
            case "shipped":
                return "badge bg-primary";
            case "delivered":
                return "badge bg-success";
            case "cancelled":
                return "badge bg-danger";
            default:
                return "badge bg-secondary";
        }
    }

    /** 🔹 Can cancel order */
    public boolean canCancelOrder(Orders order) {
        if (order == null || order.getStatus() == null) return false;
        String s = order.getStatus().toLowerCase();
        return s.equals("pending") || s.equals("confirmed");
    }

    /** 🔹 Can delete order */
    public boolean canDeleteOrder(Orders order) {
        if (order == null || order.getStatus() == null) return false;
        String s = order.getStatus().toLowerCase();
        return s.equals("cancelled") || s.equals("delivered");
    }

    /** 🔹 Status dropdown */
    public List<String> getStatusOptions() {
        return List.of("all", "pending", "confirmed", "shipped", "delivered", "cancelled");
    }

    public String statusOptionLabel(String status) {
        if ("all".equals(status)) return "All";
        return statusDisplay(status);
    }

    /** 🔹 Statistics */
    public long getTotalOrders() {
        return getAllOrders().size();
    }

    public long getPendingOrders() {
        return getAllOrders().stream()
                .filter(o -> o.getStatus() != null && o.getStatus().equalsIgnoreCase("pending"))
                .count();
    }

    public long getConfirmedOrders() {
        return getAllOrders().stream()
                .filter(o -> o.getStatus() != null && o.getStatus().equalsIgnoreCase("confirmed"))
                .count();
    }

    public double getTotalRevenue() {
        return getAllOrders().stream()
                .filter(o -> o.getStatus() != null && !o.getStatus().equalsIgnoreCase("cancelled"))
                .mapToDouble(Orders::getTotalAmount)
                .sum();
    }

    /** 🔹 Messages */
    private void addSuccessMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, msg, null));
    }

    private void addErrorMessage(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, msg, null));
    }

    /** 🔹 Reset filter */
    public void resetFilter() {
        statusFilter = "all";
        searchKeyword = "";
        loadOrders();
    }

    /** 🔹 Load order by ID for detail page */
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
            addErrorMessage("Error loading order information: " + e.getMessage());
        }
    }

    /** 🔹 Get total items in order */
    public int getTotalItemsInOrder(Orders order) {
        if (order != null && order.getOrderItemsCollection() != null) {
            return order.getOrderItemsCollection().stream()
                    .mapToInt(OrderItems::getQuantity)
                    .sum();
        }
        return 0;
    }

    /** 🔹 Update current order status */
    public void updateCurrentOrderStatus() {
        if (selectedOrder != null) {
            updateOrderStatus(selectedOrder, selectedOrder.getStatus());
        }
    }

    /** 🔹 Getters & Setters */
    public Orders getSelectedOrder() {
        return selectedOrder;
    }

    public void setSelectedOrder(Orders selectedOrder) {
        this.selectedOrder = selectedOrder;
    }

    public String getStatusFilter() {
        return statusFilter;
    }

    public void setStatusFilter(String statusFilter) {
        this.statusFilter = statusFilter;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }
}
