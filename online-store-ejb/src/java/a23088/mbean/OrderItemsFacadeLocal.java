/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package a23088.mbean;

import a23088.entity.OrderItems;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Local
public interface OrderItemsFacadeLocal {

    void create(OrderItems orderItems);

    void edit(OrderItems orderItems);

    void remove(OrderItems orderItems);

    OrderItems find(Object id);

    List<OrderItems> findAll();

    List<OrderItems> findRange(int[] range);

    int count();
    
    List<OrderItems> findByOrderID(Integer orderId);
    
}
