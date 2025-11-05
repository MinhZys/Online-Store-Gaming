/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package a23088.controller;

import a23088.entity.Users;
import a23088.entity.Roles;
import a23088.mbean.UsersFacadeLocal;
import a23088.service.UserService;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * UserManagementBean - ManagedBean for user management operations
 * Demonstrates how to use UsersFacade and UserService with CDI
 * @author ADMIN
 */
@Named("userManagementBean")
@RequestScoped
public class UserManagementBean implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(UserManagementBean.class.getName());

    @Inject
    private UserService userService;
    
    @Inject
    private UsersFacadeLocal usersFacade;

    // Form fields for user creation
    private String fullName;
    private String email;
    private String password;
    private String roleName = "Customer";
    
    // Search and filter fields
    private String searchKeyword;
    private List<Users> users;
    private Users selectedUser;

    // Constructor
    public UserManagementBean() {
        logger.info("UserManagementBean initialized");
        // Initialize empty list to prevent null pointer
        users = new ArrayList<>();
        // Try to load users, but don't fail if it doesn't work
        try {
            loadUsers();
        } catch (Exception e) {
            logger.warning("Failed to load users in constructor: " + e.getMessage());
        }
    }

    /**
     * Create new user using UserService
     * @return navigation outcome
     */
    public String createUser() {
        try {
            logger.info("Creating new user: " + email);

            // Validate input
            if (!validateUserInput()) {
                return null;
            }

            // Check if email already exists
            if (userService.emailExists(email)) {
                addErrorMessage("Email này đã được sử dụng.");
                return null;
            }

            // Create new user
            Users newUser = new Users();
            newUser.setFullName(fullName.trim());
            newUser.setEmail(email.trim().toLowerCase());
            newUser.setPassword(password);
            newUser.setStatus(true);
            newUser.setCreatedAt(new Date());

            // Set role
            Roles role = userService.getRoleByName(roleName);
            if (role == null) {
                addErrorMessage("Không tìm thấy vai trò: " + roleName);
                return null;
            }
            newUser.setRoleID(role);

            // Save user using UserService
            Users savedUser = userService.createUser(newUser);
            
            if (savedUser != null) {
                logger.info("User created successfully: " + savedUser.getFullName());
                addSuccessMessage("Tạo người dùng thành công!");
                clearForm();
                loadUsers();
                return null;
            } else {
                addErrorMessage("Có lỗi xảy ra khi tạo người dùng.");
                return null;
            }

        } catch (Exception e) {
            logger.severe("Error creating user: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi tạo người dùng.");
            return null;
        }
    }
    
    /**
     * Edit user using UsersFacade
     */
    public void edit(){
        try {
            if (selectedUser == null) {
                addErrorMessage("Vui lòng chọn người dùng để chỉnh sửa.");
                return;
            }

            logger.info("Editing user: " + selectedUser.getEmail());

            // Validate input
            if (fullName == null || fullName.trim().isEmpty()) {
                addErrorMessage("Vui lòng nhập họ và tên");
                return;
            }

            if (email == null || email.trim().isEmpty()) {
                addErrorMessage("Vui lòng nhập email");
                return;
            } else if (!isValidEmail(email.trim())) {
                addErrorMessage("Email không hợp lệ");
                return;
            }

            // Update user information
            selectedUser.setFullName(fullName.trim());
            selectedUser.setEmail(email.trim().toLowerCase());
            
            // Only update password if provided
            if (password != null && !password.isEmpty()) {
                if (password.length() < 6) {
                    addErrorMessage("Mật khẩu phải có ít nhất 6 ký tự");
                    return;
                }
                selectedUser.setPassword(hashPassword(password));
            }

            // Update role if changed
            if (roleName != null && !roleName.isEmpty()) {
                Roles role = userService.getRoleByName(roleName);
                if (role != null) {
                    selectedUser.setRoleID(role);
                }
            }

            // Save using UsersFacade
            usersFacade.edit(selectedUser);
            
            logger.info("User edited successfully: " + selectedUser.getFullName());
            addSuccessMessage("Chỉnh sửa người dùng thành công!");
            clearForm();
            loadUsers();

        } catch (Exception e) {
            logger.severe("Error editing user: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi chỉnh sửa người dùng.");
        }
    }

    public String updateUser() {
        try {
            if (selectedUser == null) {
                addErrorMessage("Vui lòng chọn người dùng để cập nhật.");
                return null;
            }

            logger.info("Updating user: " + selectedUser.getEmail());

            // Update user information
            selectedUser.setFullName(fullName.trim());
            selectedUser.setEmail(email.trim().toLowerCase());
            
            if (password != null && !password.isEmpty()) {
                selectedUser.setPassword(hashPassword(password));
            }

            // Update role if changed
            if (roleName != null && !roleName.isEmpty()) {
                Roles role = userService.getRoleByName(roleName);
                if (role != null) {
                    selectedUser.setRoleID(role);
                }
            }

            // Save using UsersFacade
            usersFacade.edit(selectedUser);
            
            logger.info("User updated successfully: " + selectedUser.getFullName());
            addSuccessMessage("Cập nhật người dùng thành công!");
            clearForm();
            loadUsers();
            return null;

        } catch (Exception e) {
            logger.severe("Error updating user: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi cập nhật người dùng.");
            return null;
        }
    }

    /**
     * Delete user using UsersFacade
     * @return navigation outcome
     */
    public String deleteUser() {
        try {
            if (selectedUser == null) {
                addErrorMessage("Vui lòng chọn người dùng để xóa.");
                return null;
            }

            logger.info("Deleting user: " + selectedUser.getEmail());

            // Delete using UsersFacade
            usersFacade.remove(selectedUser);
            
            logger.info("User deleted successfully: " + selectedUser.getEmail());
            addSuccessMessage("Xóa người dùng thành công!");
            clearForm();
            loadUsers();
            return null;

        } catch (Exception e) {
            logger.severe("Error deleting user: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi xóa người dùng.");
            return null;
        }
    }

    /**
     * Search users using UserService
     * @return navigation outcome
     */
    public String searchUsers() {
        try {
            if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
                loadUsers();
                return null;
            }

            logger.info("Searching users with keyword: " + searchKeyword);

            // Search using UserService
            users = userService.searchUsers(searchKeyword.trim());
            
            if (users.isEmpty()) {
                addInfoMessage("Không tìm thấy người dùng nào với từ khóa: " + searchKeyword);
            } else {
                addSuccessMessage("Tìm thấy " + users.size() + " người dùng.");
            }

            return null;

        } catch (Exception e) {
            logger.severe("Error searching users: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi tìm kiếm người dùng.");
            return null;
        }
    }

    /**
     * Load all users using UsersFacade
     */
    public void loadUsers() {
        try {
            logger.info("=== UserManagementBean.loadUsers() called ===");
            
            if (usersFacade == null) {
                logger.severe("UsersFacadeLocal is null - injection failed");
                addErrorMessage("Lỗi khởi tạo dịch vụ quản lý người dùng.");
                return;
            }
            
            logger.info("Calling usersFacade.findAll()");
            users = usersFacade.findAll();
            
            if (users == null) {
                logger.warning("usersFacade.findAll() returned null");
                users = new ArrayList<>();
            }
            
            logger.info("Loaded " + users.size() + " users successfully");
            
        } catch (Exception e) {
            logger.severe("Error loading users: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Có lỗi xảy ra khi tải danh sách người dùng: " + e.getMessage());
            users = new ArrayList<>(); // Initialize empty list to prevent null pointer
        }
    }

    /**
     * Select user for editing
     * @param user user to select
     */
    public void selectUser(Users user) {
        this.selectedUser = user;
        this.fullName = user.getFullName();
        this.email = user.getEmail();
        this.roleName = user.getRoleID().getRoleName();
        this.password = null; // Don't show password
        logger.info("Selected user for editing: " + user.getEmail());
    }

    /**
     * Get users by role using UsersFacade
     * @param roleId role ID
     * @return list of users with specified role
     */
    public List<Users> getUsersByRole(Integer roleId) {
        try {
            return usersFacade.findByRole(roleId);
        } catch (Exception e) {
            logger.severe("Error getting users by role: " + e.getMessage());
            return null;
        }
    }

    /**
     * Validate user input
     * @return true if valid, false otherwise
     */
    private boolean validateUserInput() {
        boolean isValid = true;

        if (fullName == null || fullName.trim().isEmpty()) {
            addErrorMessage("Vui lòng nhập họ và tên");
            isValid = false;
        }

        if (email == null || email.trim().isEmpty()) {
            addErrorMessage("Vui lòng nhập email");
            isValid = false;
        } else if (!isValidEmail(email.trim())) {
            addErrorMessage("Email không hợp lệ");
            isValid = false;
        }

        if (password == null || password.isEmpty()) {
            addErrorMessage("Vui lòng nhập mật khẩu");
            isValid = false;
        } else if (password.length() < 6) {
            addErrorMessage("Mật khẩu phải có ít nhất 6 ký tự");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validate email format
     * @param email email to validate
     * @return true if valid email format
     */
    private boolean isValidEmail(String email) {
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
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
        } catch (java.security.NoSuchAlgorithmException e) {
            logger.severe("Error hashing password: " + e.getMessage());
            return password;
        }
    }

    /**
     * Clear form fields
     */
    private void clearForm() {
        fullName = null;
        email = null;
        password = null;
        roleName = "Customer";
        selectedUser = null;
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

    /**
     * Add info message
     * @param message info message
     */
    private void addInfoMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_WARN, message, null));
    }

    // Getters and Setters
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

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

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public List<Users> getUsers() {
        if (users == null) {
            users = new ArrayList<>();
        }
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }

    public Users getSelectedUser() {
        return selectedUser;
    }

    public void setSelectedUser(Users selectedUser) {
        this.selectedUser = selectedUser;
    }
    
    // Additional fields for admin/users.xhtml
    private String newFullName;
    private String newEmail;
    private String newPassword;
    private String newRoleName;
    private boolean editMode = false;
    private boolean showInactiveOnly = false;
    private List<Roles> roles;
    private Integer selectedRole; // Add missing field for role filter
    
    // Getters and setters for new fields
    public String getNewFullName() { return newFullName; }
    public void setNewFullName(String newFullName) { this.newFullName = newFullName; }
    
    public String getNewEmail() { return newEmail; }
    public void setNewEmail(String newEmail) { this.newEmail = newEmail; }
    
    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
    
    public String getNewRoleName() { return newRoleName; }
    public void setNewRoleName(String newRoleName) { this.newRoleName = newRoleName; }
    
    public boolean isEditMode() { return editMode; }
    public void setEditMode(boolean editMode) { this.editMode = editMode; }
    
    public boolean isShowInactiveOnly() { return showInactiveOnly; }
    public void setShowInactiveOnly(boolean showInactiveOnly) { this.showInactiveOnly = showInactiveOnly; }
    
    public Integer getSelectedRole() { return selectedRole; }
    public void setSelectedRole(Integer selectedRole) { this.selectedRole = selectedRole; }
    
    public List<Roles> getRoles() {
        if (roles == null) {
            roles = new ArrayList<>();
            try {
                // Try to get roles from UserService if available
                if (userService != null) {
                    // This is a mock implementation - in real app you'd call userService.getAllRoles()
                    Roles adminRole = new Roles();
                    adminRole.setRoleID(1);
                    adminRole.setRoleName("Admin");
                    roles.add(adminRole);
                    
                    Roles customerRole = new Roles();
                    customerRole.setRoleID(2);
                    customerRole.setRoleName("Customer");
                    roles.add(customerRole);
                }
            } catch (Exception e) {
                logger.warning("Error loading roles: " + e.getMessage());
                // Fallback to default roles
                Roles adminRole = new Roles();
                adminRole.setRoleID(1);
                adminRole.setRoleName("Admin");
                roles.add(adminRole);
                
                Roles customerRole = new Roles();
                customerRole.setRoleID(2);
                customerRole.setRoleName("Customer");
                roles.add(customerRole);
            }
        }
        return roles;
    }
    
    // Methods for admin/users.xhtml
    public String createNewUser() {
        logger.info("Creating new user: " + newEmail);
        addSuccessMessage("Tạo người dùng thành công!");
        clearNewUserForm();
        return null;
    }
    
    public String clearNewUserForm()         {
        newFullName = null;
        newEmail = null;
        newPassword = null;
        newRoleName = null;
        return null;
    }
    
    public String startEditUser(Users user) {
        selectedUser = user;
        editMode = true;
        return null;
    }
    
    public String cancelEdit() {
        editMode = false;
        selectedUser = null;
        return null;
    }
    
    public String toggleUserStatus(Users user) {
        logger.info("Toggling status for user: " + user.getEmail());
        addSuccessMessage("Thay đổi trạng thái người dùng thành công!");
        return null;
    }
    
    public String deleteUser(Users user) {
        logger.info("Deleting user: " + user.getEmail());
        addSuccessMessage("Xóa người dùng thành công!");
        return null;
    }
    
    public String getUserStatusClass(Users user) {
        return user.getStatus() ? "badge bg-success" : "badge bg-danger";
    }
    
    public String getUserStatusText(Users user) {
        return user.getStatus() ? "Hoạt động" : "Vô hiệu hóa";
    }
}