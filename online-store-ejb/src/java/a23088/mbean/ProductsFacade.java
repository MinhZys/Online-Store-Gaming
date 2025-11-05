/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.mbean;

import a23088.entity.Products;
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
public class ProductsFacade extends AbstractFacade<Products> implements ProductsFacadeLocal {

    @PersistenceContext(unitName = "online-store-ejbPU")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProductsFacade() {
        super(Products.class);
    }
    
    @Override
    public List<Products> findPublished() {
        TypedQuery<Products> query = em.createNamedQuery("Products.findPublished", Products.class);
        return query.getResultList();
    }
    
    @Override
    public List<Products> findUnpublished() {
        TypedQuery<Products> query = em.createNamedQuery("Products.findUnpublished", Products.class);
        return query.getResultList();
    }
    
}
