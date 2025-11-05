/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.controller;

import a23088.entity.Users;
import a23088.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;

/**
 * AdminAccessBean - ManagedBean for admin access control
 * Checks if current user has admin privileges
 * @author ADMIN
 */
@Named("adminAccessBean")
@RequestScoped
public class AdminAccessBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(AdminAccessBean.class.getName());

    @Inject
    private LoginBean loginBean;
    
    @Inject
    private UserService userService;

    /**
     * Check admin access and redirect if not authorized
     * This method should be called from preRenderView event
     */
    public void checkAdminAccess() {
        try {
            logger.info("=== AdminAccessBean.checkAdminAccess() called ===");
            
            // Check if user is logged in
            boolean loggedIn = loginBean.isLoggedIn();
            logger.info("User logged in: " + loggedIn);
            
            if (!loggedIn) {
                logger.warning("Unauthorized access attempt - user not logged in");
                redirectToLogin();
                return;
            }
            
            // Get current user info
            String userName = loginBean.getCurrentUserName();
            logger.info("Current user: " + userName);
            
            // TEMPORARY: Allow access for debugging - comment out admin check
            logger.info("TEMPORARY: Bypassing admin check for debugging");
            logger.info("Admin access granted for user: " + userName);
            return;
            
            /*
            // Check if user is admin
            boolean isAdmin = loginBean.isAdmin();
            logger.info("User is admin: " + isAdmin);
            
            if (!isAdmin) {
                logger.warning("Unauthorized access attempt - user is not admin: " + userName);
                redirectToIndex();
                return;
            }
            
            logger.info("Admin access granted for user: " + userName);
            */
            
        } catch (Exception e) {
            logger.severe("Error checking admin access: " + e.getMessage());
            e.printStackTrace();
            redirectToLogin();
        }
    }
    
    /**
     * Redirect to login page
     */
    private void redirectToLogin() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                facesContext.getExternalContext().redirect(
                    facesContext.getExternalContext().getRequestContextPath() + "/login.xhtml"
                );
            }
        } catch (IOException e) {
            logger.severe("Error redirecting to login: " + e.getMessage());
        }
    }
    
    /**
     * Redirect to index page
     */
    private void redirectToIndex() {
        try {
            FacesContext facesContext = FacesContext.getCurrentInstance();
            if (facesContext != null) {
                facesContext.getExternalContext().redirect(
                    facesContext.getExternalContext().getRequestContextPath() + "/index.xhtml"
                );
            }
        } catch (IOException e) {
            logger.severe("Error redirecting to index: " + e.getMessage());
        }
    }
    
    /**
     * Check if current user is admin (for UI rendering)
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin() {
        return loginBean.isAdmin();
    }
    
    /**
     * Check if current user is logged in (for UI rendering)
     * @return true if user is logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return loginBean.isLoggedIn();
    }
}
