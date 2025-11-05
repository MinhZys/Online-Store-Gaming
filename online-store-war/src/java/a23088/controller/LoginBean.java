/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.controller;

import a23088.entity.Users;
import a23088.service.UserService;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * LoginBean - ManagedBean for handling user login/logout
 * Uses CDI for dependency injection with EJB UserService
 * @author ADMIN
 */
@Named("loginBean")
@SessionScoped
public class LoginBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(LoginBean.class.getName());

    @Inject
    private UserService userService;

    // Login form fields
    private String email;
    private String password;
    private boolean remember;

    // Current logged in user
    private Users currentUser;

    // Constructor
    public LoginBean() {
        logger.info("LoginBean initialized");
    }

    /**
     * Login method
     * @return navigation outcome
     */
    public String login() {
        try {
            logger.info("Attempting login for email: " + email);

            // Fast input validation
            if (email == null || email.trim().isEmpty()) {
                addErrorMessage("Please enter email");
                return null;
            }

            if (password == null || password.trim().isEmpty()) {
                addErrorMessage("Please enter password");
                return null;
            }

            // Authenticate user
            Users user = userService.authenticate(email.trim(), password);
            
            if (user != null) {
                // Login successful
                currentUser = user;
                logger.info("Login successful for user: " + user.getFullName());
                
                // Clear password for security
                password = null;
                
                // Add success message
                addSuccessMessage("Login successful! Welcome " + user.getFullName());
                
                // Redirect based on user role
                if (userService.isAdmin(user)) {
                    return "admin/index?faces-redirect=true";
                } else {
                    return "index?faces-redirect=true";
                }
            } else {
                // Login failed
                logger.warning("Login failed for email: " + email);
                addErrorMessage("Email or password is incorrect");
                return null;
            }

        } catch (Exception e) {
            logger.severe("Login error: " + e.getMessage());
            addErrorMessage("An error occurred during login. Please try again.");
            return null;
        }
    }

    /**
     * Logout method
     * @return navigation outcome
     */
    public String logout() {
        try {
            if (currentUser != null) {
                logger.info("User " + currentUser.getFullName() + " logged out");
            }
            
            // Clear session data
            currentUser = null;
            email = null;
            password = null;
            remember = false;
            
            // Invalidate session
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                facesContext.getExternalContext().invalidateSession();
            }
            
            logger.info("Logout completed successfully");
            
            // Always return null to stay on current page (logout.xhtml)
            return null;
            
        } catch (Exception e) {
            logger.severe("Logout error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Logout method that redirects to index (for direct logout from navbar)
     * @return logout outcome
     */
    public String logoutAction() {
        try {
            if (currentUser != null) {
                logger.info("User " + currentUser.getFullName() + " logged out via logoutAction");
            }
            
            // Clear session data
            currentUser = null;
            email = null;
            password = null;
            remember = false;
            
            // Invalidate session
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                facesContext.getExternalContext().invalidateSession();
            }
            
            logger.info("Logout action completed successfully");
            return "logout?faces-redirect=true";
            
        } catch (Exception e) {
            logger.severe("Logout action error: " + e.getMessage());
            return "logout?faces-redirect=true";
        }
    }



    /**
     * AJAX logout method for better user experience
     * @return null to stay on same page
     */
    public void logoutAjax() {
        try {
            if (currentUser != null) {
                logger.info("User " + currentUser.getFullName() + " logged out via AJAX");
            }
            
            // Clear session data
            currentUser = null;
            email = null;
            password = null;
            remember = false;
            
            // Invalidate session
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                facesContext.getExternalContext().invalidateSession();
            }
            
            // Add success message
            addSuccessMessage("Logout successful");
            
            logger.info("AJAX logout completed successfully");
            
        } catch (Exception e) {
            logger.severe("AJAX logout error: " + e.getMessage());
            addErrorMessage("An error occurred during logout");
        }
    }

    /**
     * Check if user is logged in
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }

    /**
     * Check if current user is admin
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        logger.info("=== LoginBean.isAdmin() called ===");
        logger.info("currentUser: " + (currentUser != null ? currentUser.getFullName() : "null"));
        
        if (currentUser == null) {
            logger.info("currentUser is null - returning false");
            return false;
        }
        
        if (currentUser.getRoleID() == null) {
            logger.info("currentUser.getRoleID() is null - returning false");
            return false;
        }
        
        String roleName = currentUser.getRoleID().getRoleName();
        logger.info("Role name: " + roleName);
        
        boolean isAdmin = userService.isAdmin(currentUser);
        logger.info("userService.isAdmin() returned: " + isAdmin);
        
        return isAdmin;
    }

    /**
     * Check if current user is customer
     * @return true if user is customer, false otherwise
     */
    public boolean isCustomer() {
        return currentUser != null && userService.isCustomer(currentUser);
    }

    /**
     * Get current user's full name
     * @return user's full name or empty string
     */
    public String getCurrentUserName() {
        return currentUser != null ? currentUser.getFullName() : "";
    }

    /**
     * Get current user's email
     * @return user's email or empty string
     */
    public String getCurrentUserEmail() {
        return currentUser != null ? currentUser.getEmail() : "";
    }

    /**
     * Get current user's role name
     * @return user's role name or empty string
     */
    public String getCurrentUserRole() {
        return currentUser != null && currentUser.getRoleID() != null 
            ? currentUser.getRoleID().getRoleName() : "";
    }

    /**
     * Add success message
     * @param message success message
     */
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, message, null));
    }

    /**
     * Add error message
     * @param message error message
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, message, null));
    }

    // Getters and Setters
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isRemember() {
        return remember;
    }

    public void setRemember(boolean remember) {
        this.remember = remember;
    }

    public Users getCurrentUser() {
        return currentUser;
    }
    public void setCurrentUser(Users currentUser) {
        this.currentUser = currentUser;
    }
}
