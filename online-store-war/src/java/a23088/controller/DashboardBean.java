package a23088.controller;

import a23088.entity.Orders;
import a23088.entity.Products;
import a23088.entity.Users;
import a23088.mbean.OrdersFacadeLocal;
import a23088.mbean.ProductsFacadeLocal;
import a23088.mbean.UsersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.inject.Named;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Named("dashboardBean")
@SessionScoped
public class DashboardBean implements Serializable {

    @EJB
    private OrdersFacadeLocal ordersFacade;
    
    @EJB
    private ProductsFacadeLocal productsFacade;
    
    @EJB
    private UsersFacadeLocal usersFacade;

    private List<Orders> allOrders;
    private List<Products> allProducts;
    private List<Users> allUsers;

    public DashboardBean() {
    }

    /** 🔹 Load all data */
    public void loadData() {
        try {
            allOrders = ordersFacade.findAll();
            allProducts = productsFacade.findAll();
            allUsers = usersFacade.findAll();
            
            if (allOrders == null) allOrders = new ArrayList<>();
            if (allProducts == null) allProducts = new ArrayList<>();
            if (allUsers == null) allUsers = new ArrayList<>();
        } catch (Exception e) {
            e.printStackTrace();
            allOrders = new ArrayList<>();
            allProducts = new ArrayList<>();
            allUsers = new ArrayList<>();
        }
    }

    /** 🔹 Basic Statistics */
    public long getTotalUsers() {
        if (allUsers == null) loadData();
        return allUsers.size();
    }

    public long getTotalOrders() {
        if (allOrders == null) loadData();
        return allOrders.size();
    }

    public long getTotalProducts() {
        if (allProducts == null) loadData();
        return allProducts.size();
    }

    public double getTotalRevenue() {
        if (allOrders == null) loadData();
        return allOrders.stream()
                .filter(o -> o.getStatus() != null && !o.getStatus().equalsIgnoreCase("cancelled"))
                .mapToDouble(Orders::getTotalAmount)
                .sum();
    }

    /** 🔹 Order Statistics */
    public long getPendingOrders() {
        if (allOrders == null) loadData();
        return allOrders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus().equalsIgnoreCase("pending"))
                .count();
    }

    public long getConfirmedOrders() {
        if (allOrders == null) loadData();
        return allOrders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus().equalsIgnoreCase("confirmed"))
                .count();
    }

    public long getDeliveredOrders() {
        if (allOrders == null) loadData();
        return allOrders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus().equalsIgnoreCase("delivered"))
                .count();
    }

    public long getCancelledOrders() {
        if (allOrders == null) loadData();
        return allOrders.stream()
                .filter(o -> o.getStatus() != null && o.getStatus().equalsIgnoreCase("cancelled"))
                .count();
    }

    /** 🔹 Revenue Statistics */
    public double getTodayRevenue() {
        if (allOrders == null) loadData();
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        
        return allOrders.stream()
                .filter(o -> o.getOrderDate() != null && 
                           o.getOrderDate().after(today.getTime()) &&
                           o.getStatus() != null && !o.getStatus().equalsIgnoreCase("cancelled"))
                .mapToDouble(Orders::getTotalAmount)
                .sum();
    }

    public double getThisMonthRevenue() {
        if (allOrders == null) loadData();
        Calendar monthStart = Calendar.getInstance();
        monthStart.set(Calendar.DAY_OF_MONTH, 1);
        monthStart.set(Calendar.HOUR_OF_DAY, 0);
        monthStart.set(Calendar.MINUTE, 0);
        monthStart.set(Calendar.SECOND, 0);
        monthStart.set(Calendar.MILLISECOND, 0);
        
        return allOrders.stream()
                .filter(o -> o.getOrderDate() != null && 
                           o.getOrderDate().after(monthStart.getTime()) &&
                           o.getStatus() != null && !o.getStatus().equalsIgnoreCase("cancelled"))
                .mapToDouble(Orders::getTotalAmount)
                .sum();
    }

    /** 🔹 Chart Data - Revenue by Month (Last 6 months) */
    public String getRevenueChartData() {
        if (allOrders == null) loadData();
        
        Map<String, Double> monthlyRevenue = new LinkedHashMap<>();
        Calendar cal = Calendar.getInstance();
        
        for (int i = 5; i >= 0; i--) {
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -i);
            String monthKey = new SimpleDateFormat("MM/yyyy").format(cal.getTime());
            monthlyRevenue.put(monthKey, 0.0);
        }
        
        allOrders.stream()
                .filter(o -> o.getOrderDate() != null && 
                           o.getStatus() != null && !o.getStatus().equalsIgnoreCase("cancelled"))
                .forEach(order -> {
                    Calendar orderCal = Calendar.getInstance();
                    orderCal.setTime(order.getOrderDate());
                    String monthKey = new SimpleDateFormat("MM/yyyy").format(orderCal.getTime());
                    monthlyRevenue.merge(monthKey, (double) order.getTotalAmount(), Double::sum);
                });
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"labels\": [");
        json.append(monthlyRevenue.keySet().stream()
                .map(label -> "\"" + label + "\"")
                .collect(Collectors.joining(",")));
        json.append("],");
        json.append("\"datasets\": [{");
        json.append("\"label\": \"Doanh thu (VNĐ)\",");
        json.append("\"data\": [");
        json.append(monthlyRevenue.values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        json.append("],");
        json.append("\"backgroundColor\": \"rgba(54, 162, 235, 0.2)\",");
        json.append("\"borderColor\": \"rgba(54, 162, 235, 1)\",");
        json.append("\"borderWidth\": 2");
        json.append("}]");
        json.append("}");
        
        return json.toString();
    }

    /** 🔹 Chart Data - Orders by Status */
    public String getOrdersStatusChartData() {
        if (allOrders == null) loadData();
        
        Map<String, Long> statusCount = allOrders.stream()
                .filter(o -> o.getStatus() != null)
                .collect(Collectors.groupingBy(
                    o -> o.getStatus().toLowerCase(),
                    Collectors.counting()
                ));
        
        // Map status to Vietnamese
        Map<String, String> statusMap = new HashMap<>();
        statusMap.put("pending", "Chờ xác nhận");
        statusMap.put("confirmed", "Đã xác nhận");
        statusMap.put("shipped", "Đang giao hàng");
        statusMap.put("delivered", "Đã giao hàng");
        statusMap.put("cancelled", "Đã hủy");
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"labels\": [");
        json.append(statusCount.keySet().stream()
                .map(status -> "\"" + statusMap.getOrDefault(status, status) + "\"")
                .collect(Collectors.joining(",")));
        json.append("],");
        json.append("\"datasets\": [{");
        json.append("\"label\": \"Số đơn hàng\",");
        json.append("\"data\": [");
        json.append(statusCount.values().stream()
                .map(String::valueOf)
                .collect(Collectors.joining(",")));
        json.append("],");
        json.append("\"backgroundColor\": [");
        json.append("\"#ffc107\", \"#17a2b8\", \"#007bff\", \"#28a745\", \"#dc3545\"");
        json.append("]");
        json.append("}]");
        json.append("}");
        
        return json.toString();
    }

    /** 🔹 Chart Data - Top Selling Products */
    public String getTopProductsChartData() {
        if (allOrders == null) loadData();
        
        Map<String, Integer> productSales = new HashMap<>();
        
        allOrders.stream()
                .filter(o -> o.getOrderItemsCollection() != null && 
                           o.getStatus() != null && !o.getStatus().equalsIgnoreCase("cancelled"))
                .flatMap(order -> order.getOrderItemsCollection().stream())
                .forEach(item -> {
                    String productName = item.getProductID().getProductName();
                    productSales.merge(productName, item.getQuantity(), Integer::sum);
                });
        
        // Get top 5 products
        List<Map.Entry<String, Integer>> topProducts = productSales.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());
        
        StringBuilder json = new StringBuilder();
        json.append("{");
        json.append("\"labels\": [");
        json.append(topProducts.stream()
                .map(entry -> "\"" + entry.getKey() + "\"")
                .collect(Collectors.joining(",")));
        json.append("],");
        json.append("\"datasets\": [{");
        json.append("\"label\": \"Số lượng bán\",");
        json.append("\"data\": [");
        json.append(topProducts.stream()
                .map(entry -> String.valueOf(entry.getValue()))
                .collect(Collectors.joining(",")));
        json.append("],");
        json.append("\"backgroundColor\": [");
        json.append("\"#ff6384\", \"#36a2eb\", \"#ffce56\", \"#4bc0c0\", \"#9966ff\"");
        json.append("]");
        json.append("}]");
        json.append("}");
        
        return json.toString();
    }

    /** 🔹 Recent Orders */
    public List<Orders> getRecentOrders() {
        if (allOrders == null) loadData();
        return allOrders.stream()
                .sorted((o1, o2) -> o2.getOrderDate().compareTo(o1.getOrderDate()))
                .limit(5)
                .collect(Collectors.toList());
    }

    /** 🔹 Format Price */
    public String formatPrice(double price) {
        return String.format("%,.0f $", price);
    }

    /** 🔹 Format Date */
    public String formatDate(Date date) {
        if (date == null) return "";
        return new SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
    }

    /** 🔹 Status Display */
    public String statusDisplay(String status) {
        if (status == null) return "Không xác định";
        switch (status.toLowerCase()) {
            case "pending": return "pending";
            case "confirmed": return "confirmed";
            case "cancelled": return "cancelled";
            default: return status;
        }
    }

    /** 🔹 Status Badge Class */
    public String statusBadgeClass(String status) {
        if (status == null) return "badge-secondary";
        switch (status.toLowerCase()) {
            case "pending": return "badge bg-warning text-dark";
            case "confirmed": return "badge bg-info text-dark";
            case "shipped": return "badge bg-primary";
            case "delivered": return "badge bg-success";
            case "cancelled": return "badge bg-danger";
            default: return "badge bg-secondary";
        }
    }
}
