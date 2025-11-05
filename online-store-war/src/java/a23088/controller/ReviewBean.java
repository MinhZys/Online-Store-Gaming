package a23088.controller;

import a23088.entity.Reviews;
import a23088.entity.Products;
import a23088.entity.Users;
import a23088.mbean.ReviewsFacadeLocal;
import a23088.mbean.ProductsFacadeLocal;
import a23088.mbean.UsersFacadeLocal;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import java.io.Serializable;
import java.util.Date;
import java.util.List;

@Named(value = "reviewBean")
@SessionScoped
public class ReviewBean implements Serializable {

    @EJB
    private ReviewsFacadeLocal reviewsFacade;
    
    @EJB
    private ProductsFacadeLocal productsFacade;
    
    @EJB
    private UsersFacadeLocal usersFacade;

    private int rating = 5;
    private String comment = "";
    private List<Reviews> productReviews;

    public ReviewBean() {
    }

    // Submit a new review
    public String submitReview(Integer productId) {
        try {
            // Validate input
            if (rating < 1 || rating > 5) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", 
                        "Đánh giá phải từ 1 đến 5 sao."));
                return null;
            }
            
            if (comment == null || comment.trim().isEmpty()) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", 
                        "Vui lòng nhập nhận xét."));
                return null;
            }

            // Get current user (you'll need to implement this based on your login system)
            Users currentUser = getCurrentUser();
            if (currentUser == null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", 
                        "Vui lòng đăng nhập để đánh giá sản phẩm."));
                return null;
            }

            // Get product
            Products product = productsFacade.find(productId);
            if (product == null) {
                FacesContext.getCurrentInstance().addMessage(null, 
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", 
                        "Sản phẩm không tồn tại."));
                return null;
            }

           

            // Create new review
            Reviews review = new Reviews();
            review.setRating(rating);
            review.setComment(comment.trim());
            review.setCreatedAt(new Date());
            review.setProductID(product);
            review.setUserID(currentUser);

            // Save review
            reviewsFacade.create(review);

            // Reset form
            rating = 5;
            comment = "";

            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Thành công", 
                    "Cảm ơn bạn đã đánh giá sản phẩm!"));

            return "product-detail?id=" + productId + "&faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Lỗi", 
                    "Có lỗi khi gửi đánh giá: " + e.getMessage()));
            return null;
        }
    }

    // Get current logged-in user (implement based on your login system)
    private Users getCurrentUser() {
        // This is a placeholder - you need to implement this based on your login system
        // For now, return null to indicate no user is logged in
        // You might get this from session or security context
        return null;
    }

    // Get reviews for a specific product
    public List<Reviews> getReviewsByProduct(Integer productId) {
        try {
            return reviewsFacade.findByProductID(productId);
        } catch (Exception e) {
            e.printStackTrace();
            return new java.util.ArrayList<>();
        }
    }

    // Get stars display for rating
    public String getStars(int rating) {
        StringBuilder stars = new StringBuilder();
        for (int i = 0; i < rating; i++) {
            stars.append("★");
        }
        for (int i = rating; i < 5; i++) {
            stars.append("☆");
        }
        return stars.toString();
    }

    // Format date
    public String formatDate(Date date) {
        if (date == null) return "";
        return new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm").format(date);
    }

    // Getters and Setters
    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public List<Reviews> getProductReviews() {
        return productReviews;
    }

    public void setProductReviews(List<Reviews> productReviews) {
        this.productReviews = productReviews;
    }
}
