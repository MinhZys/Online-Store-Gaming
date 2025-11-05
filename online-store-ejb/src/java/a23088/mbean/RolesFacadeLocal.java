/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package a23088.mbean;

import a23088.entity.Roles;
import jakarta.ejb.Local;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Local
public interface RolesFacadeLocal {

    void create(Roles roles);

    void edit(Roles roles);

    void remove(Roles roles);

    Roles find(Object id);

    List<Roles> findAll();

    List<Roles> findRange(int[] range);

    int count();
    
    Roles findByRoleName(String roleName);
    
}
