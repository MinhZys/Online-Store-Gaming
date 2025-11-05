/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.service;

import a23088.entity.Users;
import a23088.entity.Roles;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.Date;
import java.util.logging.Logger;

/**
 * DataInitializationService - EJB Singleton for initializing sample data
 * This service runs once when the application starts
 * @author ADMIN
 */
@Singleton
@Startup
public class DataInitializationService {

    private static final Logger logger = Logger.getLogger(DataInitializationService.class.getName());

    @PersistenceContext(unitName = "online-store-ejbPU")
    private EntityManager em;

    @PostConstruct
    public void initializeData() {
        try {
            logger.info("Starting data initialization...");
            
            // Initialize roles
            initializeRoles();
            
            // Initialize admin user
            initializeAdminUser();
            
            logger.info("Data initialization completed successfully");
            
        } catch (Exception e) {
            logger.severe("Error during data initialization: " + e.getMessage());
        }
    }

    /**
     * Initialize default roles
     */
    private void initializeRoles() {
        try {
            // Check if roles already exist
            Long adminRoleCount = em.createQuery("SELECT COUNT(r) FROM Roles r WHERE r.roleName = 'Admin'", Long.class)
                    .getSingleResult();
            
            if (adminRoleCount == 0) {
                Roles adminRole = new Roles();
                adminRole.setRoleName("Admin");
                em.persist(adminRole);
                logger.info("Admin role created");
            }

            Long customerRoleCount = em.createQuery("SELECT COUNT(r) FROM Roles r WHERE r.roleName = 'Customer'", Long.class)
                    .getSingleResult();
            
            if (customerRoleCount == 0) {
                Roles customerRole = new Roles();
                customerRole.setRoleName("Customer");
                em.persist(customerRole);
                logger.info("Customer role created");
            }

            em.flush();
            
        } catch (Exception e) {
            logger.severe("Error initializing roles: " + e.getMessage());
        }
    }

    /**
     * Initialize admin user
     */
    private void initializeAdminUser() {
        try {
            // Check if admin user already exists
            Long adminUserCount = em.createQuery("SELECT COUNT(u) FROM Users u WHERE u.email = 'admin@store.com'", Long.class)
                    .getSingleResult();
            
            if (adminUserCount == 0) {
                // Get admin role
                Roles adminRole = em.createQuery("SELECT r FROM Roles r WHERE r.roleName = 'Admin'", Roles.class)
                        .getSingleResult();
                
                // Create admin user
                Users adminUser = new Users();
                adminUser.setFullName("Administrator");
                adminUser.setEmail("admin@store.com");
                adminUser.setPassword(hashPassword("admin123")); // Default password
                adminUser.setStatus(true);
                adminUser.setCreatedAt(new Date());
                adminUser.setRoleID(adminRole);
                
                em.persist(adminUser);
                em.flush();
                
                logger.info("Admin user created - Email: admin@store.com, Password: admin123");
            } else {
                logger.info("Admin user already exists");
            }
            
        } catch (Exception e) {
            logger.severe("Error initializing admin user: " + e.getMessage());
        }
    }

    /**
     * Hash password using SHA-256
     * @param password plain text password
     * @return hashed password
     */
    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes());
            StringBuilder hexString = new StringBuilder();
            
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            
            return hexString.toString();
        } catch (Exception e) {
            logger.severe("Error hashing password: " + e.getMessage());
            return password; // Return original password if hashing fails
        }
    }
}