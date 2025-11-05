/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.service;

import a23088.entity.Users;
import a23088.entity.Roles;
import jakarta.ejb.Stateless;
import jakarta.enterprise.inject.Default;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * UserService EJB for handling user authentication and management
 * @author ADMIN
 */
@Stateless
@Default
public class UserService {

    @PersistenceContext(unitName = "online-store-ejbPU")
    private EntityManager em;

    /**
     * Authenticate user with email and password
     * @param email user email
     * @param password user password
     * @return Users entity if authentication successful, null otherwise
     */
    public Users authenticate(String email, String password) {
        try {
            // Hash the password using SHA-256
            String hashedPassword = hashPassword(password);
            
            // Query to find user by email and password
            Query query = em.createNamedQuery("Users.findByEmail");
            query.setParameter("email", email);
            
            List<Users> users = query.getResultList();
            
            if (!users.isEmpty()) {
                Users user = users.get(0);
                // Check if password matches and user is active
                if (user.getPassword().equals(hashedPassword) && user.getStatus()) {
                    return user;
                }
            }
            
            return null;
        } catch (Exception e) {
            System.err.println("Authentication error: " + e.getMessage());
            return null;
        }
    }

    /**
     * Find user by email
     * @param email user email
     * @return Users entity if found, null otherwise
     */
    public Users findByEmail(String email) {
        try {
            Query query = em.createNamedQuery("Users.findByEmail");
            query.setParameter("email", email);
            
            List<Users> users = query.getResultList();
            return users.isEmpty() ? null : users.get(0);
        } catch (Exception e) {
            System.err.println("Error finding user by email: " + e.getMessage());
            return null;
        }
    }

    /**
     * Find user by ID
     * @param userId user ID
     * @return Users entity if found, null otherwise
     */
    public Users findById(Integer userId) {
        try {
            return em.find(Users.class, userId);
        } catch (Exception e) {
            System.err.println("Error finding user by ID: " + e.getMessage());
            return null;
        }
    }

    /**
     * Create new user
     * @param user Users entity
     * @return created Users entity
     */
    public Users createUser(Users user) {
        try {
            // Password should already be hashed before calling this method
            em.persist(user);
            em.flush();
            return user;
        } catch (Exception e) {
            System.err.println("Error creating user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Update user
     * @param user Users entity
     * @return updated Users entity
     */
    public Users updateUser(Users user) {
        try {
            return em.merge(user);
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Get all users
     * @return List of all users
     */
    public List<Users> getAllUsers() {
        try {
            Query query = em.createNamedQuery("Users.findAll");
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting all users: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get users by role
     * @param roleId role ID
     * @return List of users with specified role
     */
    public List<Users> getUsersByRole(Integer roleId) {
        try {
            Query query = em.createQuery("SELECT u FROM Users u WHERE u.roleID.roleID = :roleId");
            query.setParameter("roleId", roleId);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting users by role: " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if user is admin
     * @param user Users entity
     * @return true if user is admin, false otherwise
     */
    public boolean isAdmin(Users user) {
        if (user == null || user.getRoleID() == null) {
            return false;
        }
        return "Admin".equalsIgnoreCase(user.getRoleID().getRoleName());
    }

    /**
     * Check if user is customer
     * @param user Users entity
     * @return true if user is customer, false otherwise
     */
    public boolean isCustomer(Users user) {
        if (user == null || user.getRoleID() == null) {
            return false;
        }
        return "Customer".equalsIgnoreCase(user.getRoleID().getRoleName());
    }

    /**
     * Hash password using SHA-256
     * @param password plain text password
     * @return hashed password
     */
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
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
        } catch (NoSuchAlgorithmException e) {
            System.err.println("Error hashing password: " + e.getMessage());
            return password; // Return original password if hashing fails
        }
    }

    /**
     * Verify password
     * @param password plain text password
     * @param hashedPassword hashed password from database
     * @return true if passwords match, false otherwise
     */
    public boolean verifyPassword(String password, String hashedPassword) {
        return hashPassword(password).equals(hashedPassword);
    }

    /**
     * Check if email already exists
     * @param email email to check
     * @return true if email exists, false otherwise
     */
    public boolean emailExists(String email) {
        try {
            Users user = findByEmail(email);
            return user != null;
        } catch (Exception e) {
            System.err.println("Error checking email existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get role by name
     * @param roleName role name
     * @return Roles entity if found, null otherwise
     */
    public Roles getRoleByName(String roleName) {
        try {
            Query query = em.createNamedQuery("Roles.findByRoleName");
            query.setParameter("roleName", roleName);
            
            List<Roles> roles = query.getResultList();
            return roles.isEmpty() ? null : roles.get(0);
        } catch (Exception e) {
            System.err.println("Error getting role by name: " + e.getMessage());
            return null;
        }
    }

    /**
     * Create user with role
     * @param user Users entity
     * @param roleName role name
     * @return created Users entity
     */
    public Users createUserWithRole(Users user, String roleName) {
        try {
            Roles role = getRoleByName(roleName);
            if (role == null) {
                throw new RuntimeException("Role not found: " + roleName);
            }
            
            user.setRoleID(role);
            return createUser(user);
        } catch (Exception e) {
            System.err.println("Error creating user with role: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Delete user by ID
     * @param userId user ID to delete
     * @return true if deleted successfully, false otherwise
     */
    public boolean deleteUser(Integer userId) {
        try {
            Users user = em.find(Users.class, userId);
            if (user != null) {
                em.remove(user);
                em.flush();
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return false;
        }
    }

    /**
     * Search users by keyword (name or email)
     * @param keyword search keyword
     * @return List of matching users
     */
    public List<Users> searchUsers(String keyword) {
        try {
            Query query = em.createQuery(
                "SELECT u FROM Users u WHERE " +
                "LOWER(u.fullName) LIKE LOWER(:keyword) OR " +
                "LOWER(u.email) LIKE LOWER(:keyword)"
            );
            query.setParameter("keyword", "%" + keyword + "%");
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error searching users: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get all roles
     * @return List of all roles
     */
    public List<Roles> getAllRoles() {
        try {
            Query query = em.createNamedQuery("Roles.findAll");
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting all roles: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get users by status
     * @param status user status (true/false)
     * @return List of users with specified status
     */
    public List<Users> getUsersByStatus(boolean status) {
        try {
            Query query = em.createQuery("SELECT u FROM Users u WHERE u.status = :status");
            query.setParameter("status", status);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting users by status: " + e.getMessage());
            return null;
        }
    }

    /**
     * Get user count by role
     * @param roleId role ID
     * @return number of users with specified role
     */
    public long getUserCountByRole(Integer roleId) {
        try {
            Query query = em.createQuery("SELECT COUNT(u) FROM Users u WHERE u.roleID.roleID = :roleId");
            query.setParameter("roleId", roleId);
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            System.err.println("Error getting user count by role: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get total user count
     * @return total number of users
     */
    public long getTotalUserCount() {
        try {
            Query query = em.createQuery("SELECT COUNT(u) FROM Users u");
            return (Long) query.getSingleResult();
        } catch (Exception e) {
            System.err.println("Error getting total user count: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Update user password
     * @param userId user ID
     * @param newPassword new password (will be hashed)
     * @return true if updated successfully, false otherwise
     */
    public boolean updateUserPassword(Integer userId, String newPassword) {
        try {
            Users user = em.find(Users.class, userId);
            if (user != null) {
                user.setPassword(hashPassword(newPassword));
                em.merge(user);
                em.flush();
                return true;
            }
            return false;
        } catch (Exception e) {
            System.err.println("Error updating user password: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if user exists by email
     * @param email email to check
     * @return true if user exists, false otherwise
     */
    public boolean userExists(String email) {
        try {
            Users user = findByEmail(email);
            return user != null;
        } catch (Exception e) {
            System.err.println("Error checking user existence: " + e.getMessage());
            return false;
        }
    }

    /**
     * Get recent users (created in last N days)
     * @param days number of days
     * @return List of recent users
     */
    public List<Users> getRecentUsers(int days) {
        try {
            Query query = em.createQuery(
                "SELECT u FROM Users u WHERE u.createdAt >= :date ORDER BY u.createdAt DESC"
            );
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.add(java.util.Calendar.DAY_OF_MONTH, -days);
            query.setParameter("date", cal.getTime());
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Error getting recent users: " + e.getMessage());
            return null;
        }
    }
}
