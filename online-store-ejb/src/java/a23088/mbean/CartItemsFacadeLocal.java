/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package a23088.mbean;

import a23088.entity.CartItems;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Local
public interface CartItemsFacadeLocal {

    void create(CartItems cartItems);

    void edit(CartItems cartItems);

    void remove(CartItems cartItems);

    CartItems find(Object id);

    List<CartItems> findAll();

    List<CartItems> findRange(int[] range);

    int count();
    
    List<CartItems> findByCartID(Integer cartId);
    
}
