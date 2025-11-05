/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.mbean;

import a23088.entity.Reviews;
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
public class ReviewsFacade extends AbstractFacade<Reviews> implements ReviewsFacadeLocal {

    @PersistenceContext(unitName = "online-store-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ReviewsFacade() {
        super(Reviews.class);
    }
    
    @Override
    public List<Reviews> findByProductID(Integer productID) {
        TypedQuery<Reviews> query = em.createQuery(
            "SELECT r FROM Reviews r WHERE r.productID.productID = :productID ORDER BY r.createdAt DESC", 
            Reviews.class);
        query.setParameter("productID", productID);
        return query.getResultList();
    }
    
}
