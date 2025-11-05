/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package a23088.controller;

import a23088.entity.Supplier;
import a23088.mbean.SupplierFacadeLocal;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import java.io.Serializable;
import java.util.List;

/**
 *
 * @author ADMIN
 */
@Named(value = "suppliersBean")
@SessionScoped
public class SuppliersBean implements Serializable {

    @jakarta.ejb.EJB
    private SupplierFacadeLocal supplierFacade;
    private Supplier supplier = new Supplier();
  
    
    public SuppliersBean() {
    }
    
    
    public List<Supplier> showAllSupplier(){
        return supplierFacade.findAll();
    }
    
    
    public String editSupplier(int id) {
        supplier = supplierFacade.find(id);
        return "supplier";

    }

    public String deleteSupplier(int id) {
        supplierFacade.remove(supplierFacade.find(id));
        return "supplier";
    }

    public String saveSupplier() {
        if (supplier.getSupplierID()!= 0) {
            supplierFacade.edit(supplier);
        } else {
            supplierFacade.create(supplier);
        }
        return "supplier";
    }

    
    
    
    
    

    public SupplierFacadeLocal getSupplierFacade() {
        return supplierFacade;
    }

    public void setSupplierFacade(SupplierFacadeLocal supplierFacade) {
        this.supplierFacade = supplierFacade;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }
    
}
