/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.mbean;

import a23088.entity.Cart;
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
public class CartFacade extends AbstractFacade<Cart> implements CartFacadeLocal {

    @PersistenceContext(unitName = "online-store-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CartFacade() {
        super(Cart.class);
    }
    
    @Override
    public List<Cart> findByUserID(Integer userId) {
        try {
            TypedQuery<Cart> query = em.createQuery(
                "SELECT c FROM Cart c WHERE c.userID.userID = :userId", Cart.class);
            query.setParameter("userId", userId);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }
    
}
