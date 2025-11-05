/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "OrderItems")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "OrderItems.findAll", query = "SELECT o FROM OrderItems o"),
    @NamedQuery(name = "OrderItems.findByOrderItemID", query = "SELECT o FROM OrderItems o WHERE o.orderItemID = :orderItemID"),
    @NamedQuery(name = "OrderItems.findByQuantity", query = "SELECT o FROM OrderItems o WHERE o.quantity = :quantity"),
    @NamedQuery(name = "OrderItems.findByUnitPrice", query = "SELECT o FROM OrderItems o WHERE o.unitPrice = :unitPrice")})
public class OrderItems implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "OrderItemID")
    private Integer orderItemID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Quantity")
    private int quantity;
    @Basic(optional = false)
    @NotNull
    @Column(name = "UnitPrice")
    private int unitPrice;
    @JoinColumn(name = "OrderID", referencedColumnName = "OrderID")
    @ManyToOne(optional = false)
    private Orders orderID;
    @JoinColumn(name = "ProductID", referencedColumnName = "ProductID")
    @ManyToOne(optional = false)
    private Products productID;
    @JoinColumn(name = "VoucherID", referencedColumnName = "VoucherID")
    @ManyToOne
    private Vouchers voucherID;

    public OrderItems() {
    }

    public OrderItems(Integer orderItemID) {
        this.orderItemID = orderItemID;
    }

    public OrderItems(Integer orderItemID, int quantity, int unitPrice) {
        this.orderItemID = orderItemID;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
    }

    public Integer getOrderItemID() {
        return orderItemID;
    }

    public void setOrderItemID(Integer orderItemID) {
        this.orderItemID = orderItemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public Orders getOrderID() {
        return orderID;
    }

    public void setOrderID(Orders orderID) {
        this.orderID = orderID;
    }

    public Products getProductID() {
        return productID;
    }

    public void setProductID(Products productID) {
        this.productID = productID;
    }

    public Vouchers getVoucherID() {
        return voucherID;
    }

    public void setVoucherID(Vouchers voucherID) {
        this.voucherID = voucherID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (orderItemID != null ? orderItemID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof OrderItems)) {
            return false;
        }
        OrderItems other = (OrderItems) object;
        if ((this.orderItemID == null && other.orderItemID != null) || (this.orderItemID != null && !this.orderItemID.equals(other.orderItemID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "a23088.entity.OrderItems[ orderItemID=" + orderItemID + " ]";
    }
    
}
