/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.mbean;

import a23088.entity.OrderItems;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Stateless
public class OrderItemsFacade extends AbstractFacade<OrderItems> implements OrderItemsFacadeLocal {

    @PersistenceContext(unitName = "online-store-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public OrderItemsFacade() {
        super(OrderItems.class);
    }
    
    @Override
    public List<OrderItems> findByOrderID(Integer orderId) {
        try {
            return em.createQuery(
                "SELECT oi FROM OrderItems oi WHERE oi.orderID.orderID = :orderId", OrderItems.class)
                .setParameter("orderId", orderId)
                .getResultList();
        } catch (Exception e) {
            return null;
        }
    }
    
}
