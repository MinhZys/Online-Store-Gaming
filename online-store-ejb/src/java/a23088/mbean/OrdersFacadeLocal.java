/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package a23088.mbean;

import a23088.entity.Orders;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Local
public interface OrdersFacadeLocal {

    void create(Orders orders);

    void edit(Orders orders);

    void remove(Orders orders);

    Orders find(Object id);

    List<Orders> findAll();

    List<Orders> findRange(int[] range);

    int count();
    
    List<Orders> findByUserID(Integer userID);
    
    List<Orders> findByStatus(String status);
    
    List<Orders> findByPhone(String phone);
    
}
