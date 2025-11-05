/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.mbean;

import a23088.entity.Orders;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Stateless
public class OrdersFacade extends AbstractFacade<Orders> implements OrdersFacadeLocal {

    @PersistenceContext(unitName = "online-store-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public OrdersFacade() {
        super(Orders.class);
    }
    
    
    @Override
    public List<Orders> findByUserID(Integer userID) {
        try {
            System.out.println("=== ORDERS FACADE findByUserID START ===");
            System.out.println("UserID parameter: " + userID);
            
            List<Orders> result = em.createQuery(
                "SELECT o FROM Orders o WHERE o.userID.userID = :userID ORDER BY o.orderDate DESC", 
                Orders.class)
                .setParameter("userID", userID)
                .getResultList();
                
            System.out.println("Query result size: " + (result != null ? result.size() : "NULL"));
            if (result != null && !result.isEmpty()) {
                for (Orders order : result) {
                    System.out.println("Found order: ID=" + order.getOrderID() + 
                                     ", UserID=" + (order.getUserID() != null ? order.getUserID().getUserID() : "NULL") +
                                     ", Date=" + order.getOrderDate());
                }
            } else {
                System.out.println("No orders found for userID: " + userID);
            }
            
            System.out.println("=== ORDERS FACADE findByUserID END ===");
            return result;
            
        } catch (Exception e) {
            System.err.println("ERROR in OrdersFacade.findByUserID(): " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
    
    @Override
    public List<Orders> findByStatus(String status) {
        try {
            return em.createNamedQuery("Orders.findByStatus", Orders.class)
                     .setParameter("status", status)
                     .getResultList();
        } catch (Exception e) {
            System.err.println("ERROR in OrdersFacade.findByStatus(): " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
    @Override
    public List<Orders> findByPhone(String phone) {
        try {
            return em.createNamedQuery("Orders.findByPhone", Orders.class)
                     .setParameter("phone", phone)
                     .getResultList();
        } catch (Exception e) {
            System.err.println("ERROR in OrdersFacade.findByPhone(): " + e.getMessage());
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }
    
}
