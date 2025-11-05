/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.mbean;

import a23088.entity.Users;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Stateless
public class UsersFacade extends AbstractFacade<Users> implements UsersFacadeLocal {

    @PersistenceContext(unitName = "online-store-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public UsersFacade() {
        super(Users.class);
    }
    
    @Override
    public Users findByEmail(String email) {
        try {
            TypedQuery<Users> query = em.createNamedQuery("Users.findByEmail", Users.class);
            query.setParameter("email", email);
            List<Users> results = query.getResultList();
            return results.isEmpty() ? null : results.get(0);
        } catch (Exception e) {
            return null;
        }
    }
    
    @Override
    public List<Users> findByRole(Integer roleId) {
        try {
            TypedQuery<Users> query = em.createQuery(
                "SELECT u FROM Users u WHERE u.roleID.roleID = :roleId", Users.class);
            query.setParameter("roleId", roleId);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }
    
}
