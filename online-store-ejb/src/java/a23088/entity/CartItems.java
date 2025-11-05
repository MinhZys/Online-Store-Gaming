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
@Table(name = "CartItems")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CartItems.findAll", query = "SELECT c FROM CartItems c"),
    @NamedQuery(name = "CartItems.findByCartItemID", query = "SELECT c FROM CartItems c WHERE c.cartItemID = :cartItemID"),
    @NamedQuery(name = "CartItems.findByQuantity", query = "SELECT c FROM CartItems c WHERE c.quantity = :quantity")})
public class CartItems implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "CartItemID")
    private Integer cartItemID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Quantity")
    private int quantity;
    @JoinColumn(name = "CartID", referencedColumnName = "CartID")
    @ManyToOne(optional = false)
    private Cart cartID;
    @JoinColumn(name = "ProductID", referencedColumnName = "ProductID")
    @ManyToOne(optional = false)
    private Products productID;

    public CartItems() {
    }

    public CartItems(Integer cartItemID) {
        this.cartItemID = cartItemID;
    }

    public CartItems(Integer cartItemID, int quantity) {
        this.cartItemID = cartItemID;
        this.quantity = quantity;
    }

    public Integer getCartItemID() {
        return cartItemID;
    }

    public void setCartItemID(Integer cartItemID) {
        this.cartItemID = cartItemID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Cart getCartID() {
        return cartID;
    }

    public void setCartID(Cart cartID) {
        this.cartID = cartID;
    }

    public Products getProductID() {
        return productID;
    }

    public void setProductID(Products productID) {
        this.productID = productID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (cartItemID != null ? cartItemID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CartItems)) {
            return false;
        }
        CartItems other = (CartItems) object;
        if ((this.cartItemID == null && other.cartItemID != null) || (this.cartItemID != null && !this.cartItemID.equals(other.cartItemID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "a23088.entity.CartItems[ cartItemID=" + cartItemID + " ]";
    }
    
}
