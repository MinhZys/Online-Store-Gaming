/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.mbean;

import a23088.entity.CartItems;
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
public class CartItemsFacade extends AbstractFacade<CartItems> implements CartItemsFacadeLocal {

    @PersistenceContext(unitName = "online-store-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CartItemsFacade() {
        super(CartItems.class);
    }
    
    @Override
    public List<CartItems> findByCartID(Integer cartId) {
        try {
            TypedQuery<CartItems> query = em.createQuery(
                "SELECT ci FROM CartItems ci WHERE ci.cartID.cartID = :cartId", CartItems.class);
            query.setParameter("cartId", cartId);
            return query.getResultList();
        } catch (Exception e) {
            return null;
        }
    }
    
}
