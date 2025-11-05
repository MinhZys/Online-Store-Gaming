/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package a23088.mbean;

import a23088.entity.Vouchers;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Local
public interface VouchersFacadeLocal {

    void create(Vouchers vouchers);

    void edit(Vouchers vouchers);

    void remove(Vouchers vouchers);

    Vouchers find(Object id);

    List<Vouchers> findAll();

    List<Vouchers> findRange(int[] range);

    int count();
    
}
