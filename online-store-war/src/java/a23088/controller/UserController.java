/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSF/JSFManagedBean.java to edit this template
 */
package a23088.controller;

import a23088.entity.Users;
import a23088.entity.Roles;
import a23088.mbean.UsersFacadeLocal;
import a23088.mbean.RolesFacadeLocal;
import a23088.service.UserService;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

/**
 * UserController - CDI Managed Bean for comprehensive user management
 * Demonstrates integration with UsersFacade, RolesFacade, and UserService
 * @author ADMIN
 */
@Named(value = "userController")
@SessionScoped
public class UserController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(UserController.class.getName());

    // Inject EJB services
    @Inject
    private UsersFacadeLocal usersFacade;
    
    @Inject
    private RolesFacadeLocal rolesFacade;
    
    @Inject
    private UserService userService;

    // User management fields
    private List<Users> users;
    private Users selectedUser;
    private Users currentUser;
    
    // Form fields for user operations
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;
    private Integer roleId;
    
    // Search and filter fields
    private String searchKeyword;
    private Integer filterRoleId;
    
    // Modal state
    private boolean showEditModal = false;
    private Users userToEdit;
    private Integer editingUserId; // Store user ID for backup
    private boolean showInactiveOnly = false;
    
    // UI state fields
    private boolean editMode = false;
    private boolean showCreateForm = false;
    
    // Available roles
    private List<Roles> availableRoles;

    /**
     * Creates a new instance of UserController
     */
    public UserController() {
        logger.info("=== UserController Constructor START ===");
        logger.info("UserController initialized");
        
        // Don't initialize data in constructor - wait for injection to complete
        logger.info("=== UserController Constructor END ===");
    }
    
    /**
     * PostConstruct method - called after injection is complete
     */
    @jakarta.annotation.PostConstruct
    public void init() {
        logger.info("=== UserController @PostConstruct START ===");
        logger.info("PostConstruct called - injection should be complete now");
        
        // Check injection status
        logger.info("UsersFacade injected: " + (usersFacade != null));
        logger.info("RolesFacade injected: " + (rolesFacade != null));
        logger.info("UserService injected: " + (userService != null));
        
        initializeData();
        logger.info("=== UserController @PostConstruct END ===");
    }
    
    /**
     * Initialize controller data
     */
    private void initializeData() {
        try {
            users = new ArrayList<>();
            availableRoles = new ArrayList<>();
            loadUsers();
            loadRoles();
        } catch (Exception e) {
            logger.severe("Error initializing UserController: " + e.getMessage());
            addErrorMessage("Lỗi khởi tạo controller: " + e.getMessage());
        }
    }

    /**
     * Load all users using UsersFacade.findAll()
     */
    public void loadUsers() {
        try {
            logger.info("=== UserController.loadUsers() START ===");
            
            if (usersFacade == null) {
                logger.severe("UsersFacadeLocal is null - injection failed");
                addErrorMessage("Lỗi khởi tạo dịch vụ quản lý người dùng.");
                return;
            }
            
            logger.info("UsersFacade injected successfully, calling findAll()...");
            
            // Sử dụng hàm findAll() đã có sẵn trong UsersFacadeLocal
            users = usersFacade.findAll();
            
            if (users == null) {
                logger.warning("usersFacade.findAll() returned null - creating empty list");
                users = new ArrayList<>();
            } else {
                logger.info("Successfully retrieved " + users.size() + " users from database");
                
                // Log first few users for debugging
                if (!users.isEmpty()) {
                    logger.info("First user: " + users.get(0).getFullName() + " (" + users.get(0).getEmail() + ")");
                }
            }
            
            logger.info("=== UserController.loadUsers() END ===");
            
        } catch (Exception e) {
            logger.severe("Error loading users: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Có lỗi xảy ra khi tải danh sách người dùng: " + e.getMessage());
            users = new ArrayList<>();
        }
    }
    
    /**
     * Load all roles using RolesFacade
     */
    public void loadRoles() {
        try {
            logger.info("Loading roles...");
            
            if (rolesFacade == null) {
                logger.severe("RolesFacadeLocal is null - injection failed");
                return;
            }
            
            availableRoles = rolesFacade.findAll();
            
            if (availableRoles == null) {
                logger.warning("rolesFacade.findAll() returned null");
                availableRoles = new ArrayList<>();
            }
            
            logger.info("Loaded " + availableRoles.size() + " roles successfully");
            
        } catch (Exception e) {
            logger.severe("Error loading roles: " + e.getMessage());
            e.printStackTrace();
            availableRoles = new ArrayList<>();
        }
    }

    /**
     * Create new user using UsersFacade.create()
     * @return navigation outcome
     */
    public String createUser() {
        try {
            logger.info("Creating new user: " + email);

            // Validate input
            if (!validateUserInput()) {
                return null;
            }

            // Sử dụng hàm findByEmail() đã có sẵn trong UsersFacadeLocal
            Users existingUser = usersFacade.findByEmail(email);
            if (existingUser != null) {
                addErrorMessage("Email này đã được sử dụng.");
                return null;
            }

             // Create new user
             Users newUser = new Users();
             newUser.setFullName(fullName.trim());
             newUser.setEmail(email.trim().toLowerCase());
             newUser.setPassword(hashPassword(password));
             newUser.setStatus(true); // Always set to active
             newUser.setCreatedAt(new Date());

            // Set role
            Roles role = rolesFacade.find(roleId);
            if (role == null) {
                addErrorMessage("Không tìm thấy vai trò được chọn.");
                return null;
            }
            newUser.setRoleID(role);

            // Sử dụng hàm create() đã có sẵn trong UsersFacadeLocal
            usersFacade.create(newUser);
            
            logger.info("User created successfully: " + newUser.getFullName());
            addSuccessMessage("Tạo người dùng thành công!");
            clearForm();
            loadUsers();
            return null;

        } catch (Exception e) {
            logger.severe("Error creating user: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi tạo người dùng.");
            return null;
        }
    }

    /**
     * Update user using UsersFacade.edit()
     * @return navigation outcome
     */
    public String updateUser() {
        try {
            logger.info("=== UserController.updateUser() START ===");
            
            if (selectedUser == null) {
                addErrorMessage("Vui lòng chọn người dùng để cập nhật.");
                return null;
            }

            logger.info("Updating user: " + selectedUser.getEmail());

            // Validate input
            if (!validateUserInputForUpdate()) {
                return null;
            }

            // Sử dụng hàm findByEmail() đã có sẵn để kiểm tra email trùng lặp
            if (!selectedUser.getEmail().equals(email.trim().toLowerCase())) {
                Users existingUser = usersFacade.findByEmail(email.trim().toLowerCase());
                if (existingUser != null && !existingUser.getUserID().equals(selectedUser.getUserID())) {
                    addErrorMessage("Email này đã được sử dụng bởi người dùng khác.");
                    return null;
                }
            }

            // Update user information
            selectedUser.setFullName(fullName.trim());
            selectedUser.setEmail(email.trim().toLowerCase());
            
            // Only update password if provided
            if (password != null && !password.trim().isEmpty()) {
                if (password.length() < 6) {
                    addErrorMessage("Mật khẩu phải có ít nhất 6 ký tự");
                    return null;
                }
                selectedUser.setPassword(hashPassword(password));
                logger.info("Password updated for user: " + selectedUser.getEmail());
            }

             // Update role if changed
             if (roleId != null) {
                 Roles role = rolesFacade.find(roleId);
                 if (role != null) {
                     selectedUser.setRoleID(role);
                     logger.info("Role updated to: " + role.getRoleName());
                 }
             }

            // Sử dụng hàm edit() đã có sẵn trong UsersFacadeLocal
            usersFacade.edit(selectedUser);
            
            logger.info("User updated successfully: " + selectedUser.getFullName());
            addSuccessMessage("Cập nhật người dùng '" + selectedUser.getFullName() + "' thành công!");
            
            // Clear form and reload data
            clearForm();
            loadUsers();
            
            logger.info("=== UserController.updateUser() END ===");
            return null;

        } catch (Exception e) {
            logger.severe("Error updating user: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Có lỗi xảy ra khi cập nhật người dùng: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete user using UsersFacade.remove()
     * @return navigation outcome
     */
    public String deleteUser() {
        try {
            logger.info("=== UserController.deleteUser() START ===");
            
            if (selectedUser == null) {
                addErrorMessage("Vui lòng chọn người dùng để xóa.");
                return null;
            }

            String userEmail = selectedUser.getEmail();
            String userName = selectedUser.getFullName();
            
            logger.info("Deleting user: " + userEmail);

            // Sử dụng các collection đã có sẵn trong Users entity
            if (selectedUser.getOrdersCollection() != null && !selectedUser.getOrdersCollection().isEmpty()) {
                addErrorMessage("Không thể xóa người dùng '" + userName + "' vì đã có đơn hàng. Hãy vô hiệu hóa thay vì xóa.");
                return null;
            }

            // Sử dụng hàm remove() đã có sẵn trong UsersFacadeLocal
            usersFacade.remove(selectedUser);
            
            logger.info("User deleted successfully: " + userEmail);
            addSuccessMessage("Xóa người dùng '" + userName + "' thành công!");
            
            // Clear form and reload data
            clearForm();
            loadUsers();
            
            logger.info("=== UserController.deleteUser() END ===");
            return null;

        } catch (Exception e) {
            logger.severe("Error deleting user: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Có lỗi xảy ra khi xóa người dùng: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete user by ID (for direct deletion from UI) using UsersFacade.remove()
     * @param user user to delete
     * @return navigation outcome
     */
    public String deleteUserDirect(Users user) {
        try {
            logger.info("=== UserController.deleteUserDirect() START ===");
            
            if (user == null) {
                addErrorMessage("Người dùng không tồn tại.");
                return null;
            }

            String userEmail = user.getEmail();
            String userName = user.getFullName();
            
            logger.info("Direct deleting user: " + userEmail);

            // Sử dụng các collection đã có sẵn trong Users entity
            if (user.getOrdersCollection() != null && !user.getOrdersCollection().isEmpty()) {
                addErrorMessage("Không thể xóa người dùng '" + userName + "' vì đã có đơn hàng. Hãy vô hiệu hóa thay vì xóa.");
                return null;
            }

            // Sử dụng hàm remove() đã có sẵn trong UsersFacadeLocal
            usersFacade.remove(user);
            
            logger.info("User deleted successfully: " + userEmail);
            addSuccessMessage("Xóa người dùng '" + userName + "' thành công!");
            
            // Reload data
            loadUsers();
            
            logger.info("=== UserController.deleteUserDirect() END ===");
            return null;

        } catch (Exception e) {
            logger.severe("Error deleting user directly: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Có lỗi xảy ra khi xóa người dùng: " + e.getMessage());
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
     * Filter users by role using UsersFacade.findByRole()
     * @return navigation outcome
     */
    public String filterUsersByRole() {
        try {
            if (filterRoleId == null) {
                loadUsers();
                return null;
            }

            logger.info("Filtering users by role ID: " + filterRoleId);

            // Sử dụng hàm findByRole() đã có sẵn trong UsersFacadeLocal
            users = usersFacade.findByRole(filterRoleId);
            
            if (users == null || users.isEmpty()) {
                addInfoMessage("Không tìm thấy người dùng nào với vai trò này.");
                users = new ArrayList<>();
            } else {
                addSuccessMessage("Tìm thấy " + users.size() + " người dùng với vai trò này.");
            }

            return null;

        } catch (Exception e) {
            logger.severe("Error filtering users by role: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi lọc người dùng theo vai trò.");
            return null;
        }
    }

    /**
     * Select user for editing by ID
     * @param userId ID of user to select
     */
    public void selectUserById(Integer userId) {
        try {
            logger.info("=== UserController.selectUserById() START ===");
            logger.info("Selecting user by ID: " + userId);
            
            if (userId == null) {
                addErrorMessage("ID người dùng không hợp lệ.");
                return;
            }
            
            // Sử dụng hàm find() đã có sẵn trong UsersFacadeLocal để lấy user theo ID
            Users user = usersFacade.find(userId);
            
            if (user == null) {
                addErrorMessage("Không tìm thấy người dùng với ID: " + userId);
                return;
            }
            
            // Load dữ liệu user vào form
            this.selectedUser = user;
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.roleId = user.getRoleID() != null ? user.getRoleID().getRoleID() : null;
            this.password = null; // Don't show password
            this.confirmPassword = null; // Clear confirm password
            this.editMode = true;
            this.showCreateForm = true; // Hiện form chỉnh sửa
            
            logger.info("Selected user for editing: " + user.getEmail() + " (ID: " + user.getUserID() + ")");
            addSuccessMessage("Đã chọn người dùng '" + user.getFullName() + "' để chỉnh sửa");
            
            logger.info("=== UserController.selectUserById() END ===");
            
        } catch (Exception e) {
            logger.severe("Error selecting user by ID: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi chọn người dùng: " + e.getMessage());
        }
    }
    
  

   
    
    /**
     * Test method to check if button click works
     */
    public void testButtonClick() {
        logger.info("=== TEST BUTTON CLICK ===");
        addSuccessMessage("Nút Sửa đã được bấm!");
    }
    
    /**
     * Force refresh form data for debugging
     */
    public void refreshFormData() {
        logger.info("=== REFRESH FORM DATA ===");
        logger.info("Current fullName: " + fullName);
        logger.info("Current email: " + email);
        logger.info("Current roleId: " + roleId);
   
        logger.info("Current userToEdit: " + (userToEdit != null ? userToEdit.getFullName() : "null"));
    }
    
    /**
     * Test update method
     */
    public void testUpdate() {
        logger.info("=== TEST UPDATE METHOD ===");
        addSuccessMessage("Test update method called!");
    }
    
    /**
     * Test parameter passing
     */
    public void testParam() {
        try {
            logger.info("=== TEST PARAM ===");
            String testUserIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("testUserId");
            logger.info("testUserId from request: " + testUserIdParam);
            
            if (testUserIdParam != null) {
                addSuccessMessage("Parameter received: " + testUserIdParam);
            } else {
                addErrorMessage("No parameter received");
            }
        } catch (Exception e) {
            logger.severe("Error in testParam: " + e.getMessage());
            addErrorMessage("Error: " + e.getMessage());
        }
    }
    
    /**
     * Test update with forced data change
     */
    public void testUpdateWithChange() {
        try {
            logger.info("=== TEST UPDATE WITH CHANGE ===");
            
            if (editingUserId != null) {
                Users user = usersFacade.find(editingUserId);
                if (user != null) {
                    // Force change the name to test if update works
                    String originalName = user.getFullName();
                    String newName = originalName + " [UPDATED]";
                    
                    user.setFullName(newName);
                    usersFacade.edit(user);
                    
                    // Reload to verify
                    Users updatedUser = usersFacade.find(editingUserId);
                    if (updatedUser != null) {
                        logger.info("Test update successful - Original: " + originalName + ", Updated: " + updatedUser.getFullName());
                        addSuccessMessage("Test update successful! Name changed from '" + originalName + "' to '" + updatedUser.getFullName() + "'");
                        loadUsers(); // Refresh the list
                    } else {
                        addErrorMessage("Could not verify test update");
                    }
                } else {
                    addErrorMessage("User not found for test");
                }
            } else {
                addErrorMessage("No editing user ID for test");
            }
        } catch (Exception e) {
            logger.severe("Error in testUpdateWithChange: " + e.getMessage());
            addErrorMessage("Test update error: " + e.getMessage());
        }
    }
    
    /**
     * Populate form data from current editing user
     */
    public void populateFormFromCurrentUser() {
        try {
            logger.info("=== POPULATE FORM FROM CURRENT USER ===");
            logger.info("editingUserId: " + editingUserId);
            
            if (editingUserId != null) {
                Users user = usersFacade.find(editingUserId);
                if (user != null) {
                    this.fullName = user.getFullName();
                    this.email = user.getEmail();
                    this.roleId = user.getRoleID() != null ? user.getRoleID().getRoleID() : null;
                    this.password = "";
                    
                    logger.info("Form populated from current user: " + user.getFullName());
                    addSuccessMessage("Đã load dữ liệu từ người dùng hiện tại: " + user.getFullName());
                } else {
                    addErrorMessage("Không tìm thấy người dùng với ID: " + editingUserId);
                }
            } else {
                addErrorMessage("Không có ID người dùng để load");
            }
        } catch (Exception e) {
            logger.severe("Error populating form: " + e.getMessage());
            addErrorMessage("Lỗi khi load dữ liệu: " + e.getMessage());
        }
    }
    
    /**
     * Force load user from database using editingUserId
     */
    public void forceLoadUser() {
        try {
            logger.info("=== FORCE LOAD USER ===");
            logger.info("editingUserId: " + editingUserId);
            
            // Try to get from request parameters
            String editingUserIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editingUserId");
            logger.info("editingUserId from request: " + editingUserIdParam);
            
            Integer userIdToLoad = editingUserId;
            if (userIdToLoad == null && editingUserIdParam != null) {
                try {
                    userIdToLoad = Integer.parseInt(editingUserIdParam);
                    logger.info("Using editingUserId from request: " + userIdToLoad);
                } catch (NumberFormatException e) {
                    logger.warning("Invalid editingUserId from request: " + editingUserIdParam);
                }
            }
            
            if (userIdToLoad != null) {
                Users user = usersFacade.find(userIdToLoad);
                if (user != null) {
                    this.userToEdit = user;
                    this.selectedUser = user;
                    this.editingUserId = userIdToLoad;
                    this.fullName = user.getFullName();
                    this.email = user.getEmail();
                    this.roleId = user.getRoleID() != null ? user.getRoleID().getRoleID() : null;
                    
                    logger.info("User loaded successfully: " + user.getFullName());
                    addSuccessMessage("Đã load lại dữ liệu người dùng: " + user.getFullName());
                } else {
                    logger.warning("User not found with ID: " + userIdToLoad);
                    addErrorMessage("Không tìm thấy người dùng với ID: " + userIdToLoad);
                }
            } else {
                logger.warning("No user ID available to load");
                addErrorMessage("Không có ID người dùng để load");
            }
        } catch (Exception e) {
            logger.severe("Error force loading user: " + e.getMessage());
            addErrorMessage("Lỗi khi load người dùng: " + e.getMessage());
        }
    }
    
    /**
     * Open edit modal for user
     * @param userId ID of user to edit
     */
    public void openEditModal(Integer userId) {
        try {
            logger.info("=== UserController.openEditModal() START ===");
            logger.info("Opening edit modal for user ID: " + userId);
            logger.info("User ID type: " + (userId != null ? userId.getClass().getSimpleName() : "null"));
            
            // Try to get editingUserId from request parameters as backup
            String editingUserIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editingUserId");
            logger.info("editingUserId from request: " + editingUserIdParam);
            
            if (userId == null) {
                logger.severe("User ID is null!");
                addErrorMessage("ID người dùng không hợp lệ.");
                return;
            }
            
            // Get user from database
            Users user = usersFacade.find(userId);
            if (user == null) {
                addErrorMessage("Không tìm thấy người dùng với ID: " + userId);
                return;
            }
            
            // Set user to edit
            this.userToEdit = user;
            this.selectedUser = user; // Also set selectedUser for backup
            this.editingUserId = user.getUserID(); // Store user ID for backup
            
            // Populate form fields
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.password = ""; // Clear password for security
            this.confirmPassword = "";
            this.roleId = user.getRoleID() != null ? user.getRoleID().getRoleID() : null;
            
            // Show modal
            this.showEditModal = true;
            this.editMode = true;
            
            logger.info("Form populated - FullName: " + this.fullName + ", Email: " + this.email + ", RoleId: " + this.roleId);
            
            // Force FacesContext to update
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("editUserModal");
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("editUserModalForm");
            
            logger.info("Edit modal opened for user: " + user.getFullName());
            logger.info("=== UserController.openEditModal() END ===");
            
        } catch (Exception e) {
            logger.severe("Error opening edit modal: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi mở modal chỉnh sửa: " + e.getMessage());
        }
    }
    
    /**
     * Close edit modal
     */
    public void closeEditModal() {
        this.showEditModal = false;
        this.userToEdit = null;
        this.selectedUser = null; // Also reset selectedUser
        this.editingUserId = null; // Reset editing user ID
        this.editMode = false;
        clearForm();
    }
    
    /**
     * Navigate to edit page for user
     * @param userId user ID to edit
     * @return navigation string
     */
    public String navigateToEditPage(Integer userId) {
        try {
            logger.info("=== UserController.navigateToEditPage() START ===");
            logger.info("Navigating to edit page for user ID: " + userId);
            
            if (userId == null) {
                addErrorMessage("ID người dùng không hợp lệ.");
                return null;
            }
            
            // Lấy user từ database
            Users user = usersFacade.find(userId);
            if (user == null) {
                addErrorMessage("Không tìm thấy người dùng với ID: " + userId);
                return null;
            }
            
            // Set user to edit
            this.userToEdit = user;
            
            // Populate form fields
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.password = ""; // Clear password for security
            this.confirmPassword = "";
            this.roleId = user.getRoleID().getRoleID();
            
            // Set edit mode
            this.editMode = true;
            
            logger.info("Navigating to edit page for user: " + user.getFullName());
            logger.info("=== UserController.navigateToEditPage() END ===");
            
            return "edit-user";
            
        } catch (Exception e) {
            logger.severe("Error navigating to edit page: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi mở trang chỉnh sửa: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Update user from edit page
     */
    public String updateUserFromEditPage() {
        try {
            logger.info("=== UserController.updateUserFromEditPage() START ===");
            
            if (userToEdit == null) {
                addErrorMessage("Không có người dùng để cập nhật.");
                return null;
            }
            
            // Validate input
            if (!validateUserInputForUpdate()) {
                return null;
            }
            
             // Update user data
             userToEdit.setFullName(fullName);
             userToEdit.setEmail(email);
            
            // Update role if changed
            if (roleId != null) {
                Roles newRole = rolesFacade.find(roleId);
                if (newRole != null) {
                    userToEdit.setRoleID(newRole);
                }
            }
            
            // Update password only if provided
            if (password != null && !password.trim().isEmpty()) {
                String hashedPassword = hashPassword(password);
                userToEdit.setPassword(hashedPassword);
            }
            
            // Save to database
            usersFacade.edit(userToEdit);
            
            // Refresh users list
            loadUsers();
            
            addSuccessMessage("Cập nhật người dùng '" + userToEdit.getFullName() + "' thành công!");
            
            logger.info("User updated successfully: " + userToEdit.getFullName());
            logger.info("=== UserController.updateUserFromEditPage() END ===");
            
            return "user-management";
            
        } catch (Exception e) {
            logger.severe("Error updating user from edit page: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi cập nhật người dùng: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Cancel edit operation
     */
    public String cancelEdit() {
        clearForm();
        userToEdit = null;
        editMode = false;
        return "user-management";
    }
    
    /**
     * Back to user list
     */
    public String backToList() {
        return "user-management";
    }

    /**
     * Update user from modal
     */
    public void updateUserFromModal() {
        try {
            logger.info("=== UserController.updateUserFromModal() START ===");
            
            // Get user ID from request parameter
            String userIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editingUserId");
            logger.info("editingUserId from request: " + userIdParam);
            
            if (userIdParam == null) {
                logger.severe("No user ID provided!");
                addErrorMessage("Không có ID người dùng để cập nhật.");
                return;
            }
            
            // Parse user ID
            Integer userId;
            try {
                userId = Integer.parseInt(userIdParam);
                logger.info("Parsed user ID: " + userId);
            } catch (NumberFormatException e) {
                logger.severe("Invalid user ID: " + userIdParam);
                addErrorMessage("ID người dùng không hợp lệ.");
                return;
            }
            
            // Load user from database
            Users userToUpdate = usersFacade.find(userId);
            if (userToUpdate == null) {
                logger.severe("User not found with ID: " + userId);
                addErrorMessage("Không tìm thấy người dùng với ID: " + userId);
                return;
            }
            
            logger.info("Loaded user from database: " + userToUpdate.getFullName() + " (ID: " + userToUpdate.getUserID() + ")");
            
            // Get form data from request parameters (user input)
            String formFullName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editUserModalForm:editFullName");
            String formEmail = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editUserModalForm:editEmail");
            String formRoleId = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editUserModalForm:editRole");
            String formPassword = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editUserModalForm:editPassword");
            
            logger.info("Form data from request - FullName: " + formFullName + ", Email: " + formEmail + ", RoleId: " + formRoleId);
            
            // Use form data if available, otherwise use loaded user data
            if (formFullName != null && !formFullName.trim().isEmpty()) {
                this.fullName = formFullName;
                this.email = formEmail;
                this.roleId = formRoleId != null ? Integer.parseInt(formRoleId) : null;
                this.password = formPassword;
                logger.info("Using form data from request parameters");
            } else {
                logger.info("Form data is null, populating from loaded user...");
                this.fullName = userToUpdate.getFullName();
                this.email = userToUpdate.getEmail();
                this.roleId = userToUpdate.getRoleID() != null ? userToUpdate.getRoleID().getRoleID() : null;
                this.password = ""; // Clear password for security
                logger.info("Form data populated from loaded user - FullName: " + this.fullName + ", Email: " + this.email + ", RoleId: " + this.roleId);
            }
            
            // Validate input using the populated data
            logger.info("Validating input...");
            logger.info("Validation data - fullName: " + this.fullName + ", email: " + this.email + ", roleId: " + this.roleId);
            
            // Simple validation
            if (this.fullName == null || this.fullName.trim().isEmpty()) {
                addErrorMessage("Vui lòng nhập họ và tên");
                return;
            }
            if (this.email == null || this.email.trim().isEmpty()) {
                addErrorMessage("Vui lòng nhập email");
                return;
            }
            if (this.roleId == null) {
                addErrorMessage("Vui lòng chọn vai trò");
                return;
            }
            
            logger.info("Input validation passed!");
            
            // Check for email conflicts (only if email changed)
            if (!userToUpdate.getEmail().equals(email.trim().toLowerCase())) {
                Users existingUser = usersFacade.findByEmail(email.trim().toLowerCase());
                if (existingUser != null && !existingUser.getUserID().equals(userToUpdate.getUserID())) {
                    addErrorMessage("Email này đã được sử dụng bởi người dùng khác.");
                    return;
                }
            }
            
            // Log original data before update
            logger.info("BEFORE UPDATE - Name: " + userToUpdate.getFullName() + ", Email: " + userToUpdate.getEmail() + ", Role: " + (userToUpdate.getRoleID() != null ? userToUpdate.getRoleID().getRoleName() : "null"));
            
             // Update user data
             logger.info("Updating user data...");
             userToUpdate.setFullName(fullName.trim());
             userToUpdate.setEmail(email.trim().toLowerCase());
             logger.info("AFTER UPDATE - Name: " + userToUpdate.getFullName() + ", Email: " + userToUpdate.getEmail());
            
            // Update role if changed
            if (roleId != null) {
                logger.info("Updating role to ID: " + roleId);
                Roles newRole = rolesFacade.find(roleId);
                if (newRole != null) {
                    userToUpdate.setRoleID(newRole);
                    logger.info("Role updated to: " + newRole.getRoleName());
                } else {
                    logger.warning("Role not found for ID: " + roleId);
                }
            }
            
            // Update password only if provided
            if (password != null && !password.trim().isEmpty()) {
                logger.info("Updating password...");
                String hashedPassword = hashPassword(password);
                userToUpdate.setPassword(hashedPassword);
                logger.info("Password updated");
            } else {
                logger.info("Password not provided, keeping existing password");
            }
            
            // Save to database
            logger.info("Saving to database...");
            usersFacade.edit(userToUpdate);
            logger.info("User saved to database successfully!");
            
            // Verify data was saved by reloading from database
            Users savedUser = usersFacade.find(userToUpdate.getUserID());
            if (savedUser != null) {
                logger.info("VERIFICATION - Saved user data - Name: " + savedUser.getFullName() + ", Email: " + savedUser.getEmail() + ", Role: " + (savedUser.getRoleID() != null ? savedUser.getRoleID().getRoleName() : "null"));
            } else {
                logger.warning("Could not verify saved user data");
            }
            
             // Refresh users list
             loadUsers();
             
             // Close modal immediately
             this.showEditModal = false;
             this.userToEdit = null;
             this.selectedUser = null;
             this.editingUserId = null;
             this.editMode = false;
             clearForm();
             
             addSuccessMessage("Cập nhật người dùng '" + userToUpdate.getFullName() + "' thành công!");
             
             logger.info("User updated successfully: " + userToUpdate.getFullName());
             logger.info("Modal closed after successful update");
             logger.info("=== UserController.updateUserFromModal() END ===");
            
        } catch (Exception e) {
            logger.severe("Error updating user from modal: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi cập nhật người dùng: " + e.getMessage());
        }
    }

    /**
     * Select user for editing (legacy method)
     * @param user user to select
     */
    public void selectUser(Users user) {
        try {
            logger.info("=== UserController.selectUser() START ===");
            
            if (user == null) {
                addErrorMessage("Người dùng không tồn tại.");
                return;
            }
            
            // Load dữ liệu user vào form
            this.selectedUser = user;
            this.fullName = user.getFullName();
            this.email = user.getEmail();
            this.roleId = user.getRoleID() != null ? user.getRoleID().getRoleID() : null;
            this.password = null; // Don't show password
            this.confirmPassword = null; // Clear confirm password
            this.editMode = true;
            this.showCreateForm = true; // Hiện form chỉnh sửa
            
            logger.info("Selected user for editing: " + user.getEmail() + " (ID: " + user.getUserID() + ")");
            addSuccessMessage("Đã chọn người dùng '" + user.getFullName() + "' để chỉnh sửa");
            
            logger.info("=== UserController.selectUser() END ===");
            
        } catch (Exception e) {
            logger.severe("Error selecting user: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi chọn người dùng: " + e.getMessage());
        }
    }

    /**
     * Start creating new user
     */
    public void startCreateUser() {
        clearForm();
        this.editMode = false;
        this.showCreateForm = true;
        this.selectedUser = null;
        logger.info("Starting to create new user");
    }

    /**
     * Refresh selected user data from database
     */
    public void refreshSelectedUser() {
        try {
            if (selectedUser != null && selectedUser.getUserID() != null) {
                logger.info("Refreshing selected user data from database: " + selectedUser.getUserID());
                
                // Sử dụng hàm find() đã có sẵn để lấy dữ liệu mới nhất từ database
                Users refreshedUser = usersFacade.find(selectedUser.getUserID());
                
                if (refreshedUser != null) {
                    // Cập nhật dữ liệu form với dữ liệu mới nhất
                    this.selectedUser = refreshedUser;
                    this.fullName = refreshedUser.getFullName();
                    this.email = refreshedUser.getEmail();
                    this.roleId = refreshedUser.getRoleID() != null ? refreshedUser.getRoleID().getRoleID() : null;
                   
                    
                    logger.info("User data refreshed successfully");
                    addSuccessMessage("Đã làm mới dữ liệu người dùng từ database");
                } else {
                    addErrorMessage("Không thể làm mới dữ liệu người dùng");
                }
            } else {
                addErrorMessage("Không có người dùng nào được chọn");
            }
        } catch (Exception e) {
            logger.severe("Error refreshing selected user: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi làm mới dữ liệu người dùng: " + e.getMessage());
        }
    }

    /**
     * Cancel current operation
     */
    public void cancelOperation() {
        clearForm();
        this.editMode = false;
        this.showCreateForm = false;
        this.selectedUser = null;
        this.userToEdit = null;
        this.showEditModal = false;
        logger.info("Cancelled current operation");
    }

   

    public List<Users> getUsersByRole(Integer roleId) {
        try {
            // Sử dụng hàm findByRole() đã có sẵn trong UsersFacadeLocal
            List<Users> result = usersFacade.findByRole(roleId);
            return result != null ? result : new ArrayList<>();
        } catch (Exception e) {
            logger.severe("Error getting users by role: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Get user count by role
     * @param roleId role ID
     * @return number of users with specified role
     */
    public long getUserCountByRole(Integer roleId) {
        try {
            return userService.getUserCountByRole(roleId);
        } catch (Exception e) {
            logger.severe("Error getting user count by role: " + e.getMessage());
            return 0;
        }
    }

    /**
     * Get recent users
     * @param days number of days
     * @return list of recent users
     */
    public List<Users> getRecentUsers(int days) {
        try {
            return userService.getRecentUsers(days);
        } catch (Exception e) {
            logger.severe("Error getting recent users: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * Test database connection and create sample data
     * @return navigation outcome
     */
    public String testDatabaseConnection() {
        try {
            logger.info("=== Testing Database Connection ===");
            
            // Test 1: Check if UsersFacade is injected
            if (usersFacade == null) {
                logger.severe("UsersFacade is null - CDI injection failed");
                addErrorMessage("Lỗi: UsersFacade không được inject");
                return null;
            }
            logger.info("✓ UsersFacade injected successfully");
            
            // Test 2: Check if RolesFacade is injected
            if (rolesFacade == null) {
                logger.severe("RolesFacade is null - CDI injection failed");
                addErrorMessage("Lỗi: RolesFacade không được inject");
                return null;
            }
            logger.info("✓ RolesFacade injected successfully");
            
            // Test 3: Try to get roles from database using rolesFacade.findAll()
            List<Roles> roles = rolesFacade.findAll();
            if (roles == null || roles.isEmpty()) {
                logger.warning("No roles found in database - creating sample roles");
                createSampleRoles();
                roles = rolesFacade.findAll();
            }
            logger.info("✓ Found " + roles.size() + " roles in database");
            
            // Test 4: Try to get users from database using usersFacade.findAll()
            List<Users> users = usersFacade.findAll();
            if (users == null || users.isEmpty()) {
                logger.warning("No users found in database - creating sample users");
                createSampleUsers();
                users = usersFacade.findAll();
            }
            logger.info("✓ Found " + users.size() + " users in database");
            
            // Reload data
            loadUsers();
            loadRoles();
            
            addSuccessMessage("Kết nối database thành công! Tìm thấy " + users.size() + " người dùng và " + roles.size() + " vai trò.");
            
            return null;
            
        } catch (Exception e) {
            logger.severe("Database connection test failed: " + e.getMessage());
            e.printStackTrace();
            addErrorMessage("Lỗi kết nối database: " + e.getMessage());
            return null;
        }
    }
    
    /**
     * Create sample roles if none exist using rolesFacade.create()
     */
    private void createSampleRoles() {
        try {
            // Create Admin role using Roles entity constructor
            Roles adminRole = new Roles();
            adminRole.setRoleName("Admin");
            // Sử dụng hàm create() đã có sẵn trong RolesFacadeLocal
            rolesFacade.create(adminRole);
            logger.info("Created Admin role");
            
            // Create Customer role using Roles entity constructor
            Roles customerRole = new Roles();
            customerRole.setRoleName("Customer");
            // Sử dụng hàm create() đã có sẵn trong RolesFacadeLocal
            rolesFacade.create(customerRole);
            logger.info("Created Customer role");
            
        } catch (Exception e) {
            logger.severe("Error creating sample roles: " + e.getMessage());
        }
    }
    
    /**
     * Create sample users if none exist using usersFacade.create()
     */
    private void createSampleUsers() {
        try {
            // Get roles first using rolesFacade.findAll()
            List<Roles> roles = rolesFacade.findAll();
            if (roles.isEmpty()) {
                logger.warning("No roles available to create users");
                return;
            }
            
            Roles adminRole = roles.stream()
                .filter(r -> "Admin".equals(r.getRoleName()))
                .findFirst()
                .orElse(roles.get(0));
            
            Roles customerRole = roles.stream()
                .filter(r -> "Customer".equals(r.getRoleName()))
                .findFirst()
                .orElse(roles.get(0));
            
            // Create admin user using Users entity constructor
            Users admin = new Users();
            admin.setFullName("Administrator");
            admin.setEmail("admin@example.com");
            admin.setPassword(hashPassword("admin123"));
            admin.setStatus(true);
            admin.setCreatedAt(new Date());
            admin.setRoleID(adminRole);
            // Sử dụng hàm create() đã có sẵn trong UsersFacadeLocal
            usersFacade.create(admin);
            logger.info("Created admin user: " + admin.getEmail());
            
            // Create customer user using Users entity constructor
            Users customer = new Users();
            customer.setFullName("John Doe");
            customer.setEmail("customer@example.com");
            customer.setPassword(hashPassword("customer123"));
            customer.setStatus(true);
            customer.setCreatedAt(new Date());
            customer.setRoleID(customerRole);
            // Sử dụng hàm create() đã có sẵn trong UsersFacadeLocal
            usersFacade.create(customer);
            logger.info("Created customer user: " + customer.getEmail());
            
        } catch (Exception e) {
            logger.severe("Error creating sample users: " + e.getMessage());
        }
    }

    /**
     * Validate user input for creation
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

        if (confirmPassword != null && !confirmPassword.equals(password)) {
            addErrorMessage("Mật khẩu xác nhận không khớp");
            isValid = false;
        }

        if (roleId == null) {
            addErrorMessage("Vui lòng chọn vai trò");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Validate user input for update
     * @return true if valid, false otherwise
     */
    private boolean validateUserInputForUpdate() {
        boolean isValid = true;
        
        logger.info("=== VALIDATION START ===");
        logger.info("fullName: " + fullName);
        logger.info("email: " + email);
        logger.info("password: " + (password != null ? "[PROVIDED]" : "null"));
        logger.info("roleId: " + roleId);

        if (fullName == null || fullName.trim().isEmpty()) {
            logger.warning("Full name validation failed");
            addErrorMessage("Vui lòng nhập họ và tên");
            isValid = false;
        }

        if (email == null || email.trim().isEmpty()) {
            logger.warning("Email validation failed - empty");
            addErrorMessage("Vui lòng nhập email");
            isValid = false;
        } else if (!isValidEmail(email.trim())) {
            logger.warning("Email validation failed - invalid format");
            addErrorMessage("Email không hợp lệ");
            isValid = false;
        }

        // Password is optional for update
        if (password != null && !password.trim().isEmpty()) {
            if (password.length() < 6) {
                logger.warning("Password validation failed - too short");
                addErrorMessage("Mật khẩu phải có ít nhất 6 ký tự");
                isValid = false;
            }
            
            if (confirmPassword != null && !confirmPassword.equals(password)) {
                logger.warning("Password confirmation validation failed");
                addErrorMessage("Mật khẩu xác nhận không khớp");
                isValid = false;
            }
        }

        if (roleId == null) {
            logger.warning("Role ID validation failed - null");
            addErrorMessage("Vui lòng chọn vai trò");
            isValid = false;
        }

        logger.info("Validation result: " + isValid);
        logger.info("=== VALIDATION END ===");
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
        confirmPassword = null;
        roleId = null;
        searchKeyword = null;
        filterRoleId = null;
        showInactiveOnly = false;
        editingUserId = null;
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

    public Users getCurrentUser() {
        return currentUser;
    }

    public void setCurrentUser(Users currentUser) {
        this.currentUser = currentUser;
    }

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

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }


    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
    }

    public Integer getFilterRoleId() {
        return filterRoleId;
    }

    public void setFilterRoleId(Integer filterRoleId) {
        this.filterRoleId = filterRoleId;
    }

    public boolean isShowInactiveOnly() {
        return showInactiveOnly;
    }

    public void setShowInactiveOnly(boolean showInactiveOnly) {
        this.showInactiveOnly = showInactiveOnly;
    }

    public boolean isEditMode() {
        return editMode;
    }

    public void setEditMode(boolean editMode) {
        this.editMode = editMode;
    }

    public boolean isShowCreateForm() {
        return showCreateForm;
    }

    public void setShowCreateForm(boolean showCreateForm) {
        this.showCreateForm = showCreateForm;
    }
    
    public boolean isShowEditModal() {
        return showEditModal;
    }

    public void setShowEditModal(boolean showEditModal) {
        this.showEditModal = showEditModal;
    }
    
    public Users getUserToEdit() {
        return userToEdit;
    }

    public void setUserToEdit(Users userToEdit) {
        this.userToEdit = userToEdit;
    }
    
    public Integer getEditingUserId() {
        return editingUserId;
    }
    
    public void setEditingUserId(Integer editingUserId) {
        this.editingUserId = editingUserId;
    }

    public List<Roles> getAvailableRoles() {
        if (availableRoles == null) {
            availableRoles = new ArrayList<>();
        }
        return availableRoles;
    }

    public void setAvailableRoles(List<Roles> availableRoles) {
        this.availableRoles = availableRoles;
    }
    
    // Utility methods for UI
    
    public String getRoleName(Integer roleId) {
        if (roleId == null) return "";
        for (Roles role : availableRoles) {
            if (role.getRoleID().equals(roleId)) {
                return role.getRoleName();
            }
        }
        return "";
    }
    
    /**
     * View user details
     * @param user user to view
     */
    public void viewUserDetails(Users user) {
        try {
            logger.info("Viewing user details: " + user.getEmail());
            
            // Set selected user for viewing
            this.selectedUser = user;
            
            // Show success message
            addSuccessMessage("Đang xem thông tin người dùng: " + user.getFullName());
            
        } catch (Exception e) {
            logger.severe("Error viewing user details: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi xem thông tin người dùng: " + e.getMessage());
        }
    }
    
    /**
     * Get user orders count using Users entity collection
     * @param user user to check
     * @return number of orders
     */
    public int getUserOrdersCount(Users user) {
        try {
            if (user == null || user.getOrdersCollection() == null) {
                return 0;
            }
            // Sử dụng collection đã có sẵn trong Users entity
            return user.getOrdersCollection().size();
        } catch (Exception e) {
            logger.severe("Error getting user orders count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Get user reviews count using Users entity collection
     * @param user user to check
     * @return number of reviews
     */
    public int getUserReviewsCount(Users user) {
        try {
            if (user == null || user.getReviewsCollection() == null) {
                return 0;
            }
            // Sử dụng collection đã có sẵn trong Users entity
            return user.getReviewsCollection().size();
        } catch (Exception e) {
            logger.severe("Error getting user reviews count: " + e.getMessage());
            return 0;
        }
    }
    
    /**
     * Check if user can be deleted (no orders) using Users entity collection
     * @param user user to check
     * @return true if can be deleted
     */
    public boolean canDeleteUser(Users user) {
        try {
            if (user == null) return false;
            // Sử dụng collection đã có sẵn trong Users entity
            return getUserOrdersCount(user) == 0;
        } catch (Exception e) {
            logger.severe("Error checking if user can be deleted: " + e.getMessage());
            return false;
        }
    }
}
