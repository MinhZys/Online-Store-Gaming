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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author ADMIN
 */
@Entity
@Table(name = "Vouchers")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Vouchers.findAll", query = "SELECT v FROM Vouchers v"),
    @NamedQuery(name = "Vouchers.findByVoucherID", query = "SELECT v FROM Vouchers v WHERE v.voucherID = :voucherID"),
    @NamedQuery(name = "Vouchers.findByCode", query = "SELECT v FROM Vouchers v WHERE v.code = :code"),
    @NamedQuery(name = "Vouchers.findByDiscountPercent", query = "SELECT v FROM Vouchers v WHERE v.discountPercent = :discountPercent"),
    @NamedQuery(name = "Vouchers.findByStartDate", query = "SELECT v FROM Vouchers v WHERE v.startDate = :startDate"),
    @NamedQuery(name = "Vouchers.findByEndDate", query = "SELECT v FROM Vouchers v WHERE v.endDate = :endDate"),
    @NamedQuery(name = "Vouchers.findByStatus", query = "SELECT v FROM Vouchers v WHERE v.status = :status")})
public class Vouchers implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "VoucherID")
    private Integer voucherID;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "Code")
    private String code;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DiscountPercent")
    private int discountPercent;
    @Basic(optional = false)
    @NotNull
    @Column(name = "StartDate")
    @Temporal(TemporalType.DATE)
    private Date startDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "EndDate")
    @Temporal(TemporalType.DATE)
    private Date endDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Status")
    private boolean status;
    @OneToMany(mappedBy = "voucherID")
    private Collection<OrderItems> orderItemsCollection;

    public Vouchers() {
    }

    public Vouchers(Integer voucherID) {
        this.voucherID = voucherID;
    }

    public Vouchers(Integer voucherID, String code, int discountPercent, Date startDate, Date endDate, boolean status) {
        this.voucherID = voucherID;
        this.code = code;
        this.discountPercent = discountPercent;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
    }

    public Integer getVoucherID() {
        return voucherID;
    }

    public void setVoucherID(Integer voucherID) {
        this.voucherID = voucherID;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public void setDiscountPercent(int discountPercent) {
        this.discountPercent = discountPercent;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public boolean getStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    @XmlTransient
    public Collection<OrderItems> getOrderItemsCollection() {
        return orderItemsCollection;
    }

    public void setOrderItemsCollection(Collection<OrderItems> orderItemsCollection) {
        this.orderItemsCollection = orderItemsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (voucherID != null ? voucherID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Vouchers)) {
            return false;
        }
        Vouchers other = (Vouchers) object;
        if ((this.voucherID == null && other.voucherID != null) || (this.voucherID != null && !this.voucherID.equals(other.voucherID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "a23088.entity.Vouchers[ voucherID=" + voucherID + " ]";
    }
    
}
