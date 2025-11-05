package a23088.service;

import a23088.entity.Orders;
import a23088.mbean.OrdersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.Stateless;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class OrderUpdateService {

    @EJB
    private OrdersFacadeLocal ordersFacade;
    
    @PersistenceContext
    private EntityManager em;

    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean updateOrderInfo(Integer orderId, String address, String phone) {
        try {
            // Tìm đơn hàng theo ID
            Orders order = ordersFacade.find(orderId);
            if (order == null) {
                System.out.println("Order not found with ID: " + orderId);
                return false;
            }

            // Kiểm tra dữ liệu đầu vào
            if (address == null || address.trim().isEmpty()) {
                System.out.println("Address is null or empty");
                return false;
            }
            
            if (phone == null || phone.trim().isEmpty()) {
                System.out.println("Phone is null or empty");
                return false;
            }

            // Cập nhật thông tin
            order.setAddress(address.trim());
            order.setPhone(phone.trim());
            order.setStatus("Pending"); // Sử dụng Pending thay vì Confirmed
            
            System.out.println("Updating order: " + orderId + " with address: " + address + ", phone: " + phone);
            
            // Lưu cập nhật sử dụng EntityManager
            em.merge(order);
            
            // Log thông báo thành công ra console
            System.out.println("==========================================");
            System.out.println("🎉 ĐẶT HÀNG THÀNH CÔNG! 🎉");
            System.out.println("==========================================");
            System.out.println("Mã đơn hàng: #" + order.getOrderID());
            System.out.println("Khách hàng: " + order.getUserID().getFullName());
            System.out.println("Email: " + order.getUserID().getEmail());
            System.out.println("Địa chỉ: " + order.getAddress());
            System.out.println("Số điện thoại: " + order.getPhone());
            System.out.println("Tổng tiền: " + String.format("%,.0f", (double)order.getTotalAmount()) + "₫");
            System.out.println("Trạng thái: " + order.getStatus());
            System.out.println("Thời gian: " + new java.util.Date());
            System.out.println("==========================================");
            
            return true;

        } catch (Exception e) {
            System.err.println("Error updating order: " + e.getMessage());
            e.printStackTrace();
            
            // Thử cách khác với native query
            try {
                return updateOrderWithNativeQuery(orderId, address, phone);
            } catch (Exception e2) {
                System.err.println("Native query also failed: " + e2.getMessage());
                e2.printStackTrace();
                return false;
            }
        }
    }
    
    // Method backup sử dụng native query
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public boolean updateOrderWithNativeQuery(Integer orderId, String address, String phone) {
        try {
            String sql = "UPDATE Orders SET Address = ?, Phone = ?, Status = ? WHERE OrderID = ?";
            int result = em.createNativeQuery(sql)
                .setParameter(1, address.trim())
                .setParameter(2, phone.trim())
                .setParameter(3, "Pending") // Sử dụng Pending thay vì Confirmed
                .setParameter(4, orderId)
                .executeUpdate();
                
            System.out.println("Native query updated " + result + " rows");
            return result > 0;
            
        } catch (Exception e) {
            System.err.println("Native query failed: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
}
