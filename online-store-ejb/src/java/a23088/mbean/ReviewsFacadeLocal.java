/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package a23088.mbean;

import a23088.entity.Reviews;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Local
public interface ReviewsFacadeLocal {

    void create(Reviews reviews);

    void edit(Reviews reviews);

    void remove(Reviews reviews);

    Reviews find(Object id);

    List<Reviews> findAll();

    List<Reviews> findRange(int[] range);

    int count();
    
    List<Reviews> findByProductID(Integer productID);
    
}
