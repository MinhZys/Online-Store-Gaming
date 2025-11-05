
package a23088.controller;

import a23088.entity.Categories;
import a23088.mbean.CategoriesFacadeLocal;
import jakarta.inject.Named;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * CategoryController - CDI Managed Bean for comprehensive category management
 * Demonstrates integration with CategoriesFacade
 * @author ADMIN
 */
@Named(value = "categoryController")
@SessionScoped
public class CategoryController implements Serializable {

    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(CategoryController.class.getName());

    // Inject EJB services
    @Inject
    private CategoriesFacadeLocal categoriesFacade;

    // Category management fields
    private List<Categories> categories;
    private Categories selectedCategory;
    
    // Form fields for category operations
    private String categoryName;
    
    // Search and filter fields
    private String searchKeyword;
    
    // UI state fields
    private boolean editMode = false;
    private boolean showCreateForm = false;
    
    // Modal state
    private boolean showEditModal = false;
    private Categories categoryToEdit;
    private Integer editingCategoryId; // Store category ID for backup

    /**
     * Constructor
     */
    public CategoryController() {
        logger.info("=== CategoryController Constructor ===");
        initializeData();
    }

    /**
     * Initialize data after injection
     */
    public void init() {
        logger.info("=== CategoryController.init() START ===");
        try {
            loadCategories();
            logger.info("Categories loaded successfully. Count: " + (categories != null ? categories.size() : 0));
        } catch (Exception e) {
            logger.severe("Error in init(): " + e.getMessage());
            addErrorMessage("An error occurred while initializing data.: " + e.getMessage());
        }
        logger.info("=== CategoryController.init() END ===");
    }

    /**
     * Initialize data
     */
    private void initializeData() {
        logger.info("=== CategoryController.initializeData() START ===");
        try {
            categories = new ArrayList<>();
            clearForm();
            logger.info("Data initialized successfully");
        } catch (Exception e) {
            logger.severe("Error initializing data: " + e.getMessage());
        }
        logger.info("=== CategoryController.initializeData() END ===");
    }

    /**
     * Load categories from database
     */
    public void loadCategories() {
        logger.info("=== CategoryController.loadCategories() START ===");
        try {
            if (categoriesFacade != null) {
                categories = categoriesFacade.findAll();
                logger.info("Loaded " + (categories != null ? categories.size() : 0) + " categories from database");
            } else {
                logger.warning("categoriesFacade is null!");
                addErrorMessage("Unable to connect to database");
            }
        } catch (Exception e) {
            logger.severe("Error loading categories: " + e.getMessage());
            addErrorMessage("An error occurred while loading the category list. : " + e.getMessage());
        }
        logger.info("=== CategoryController.loadCategories() END ===");
    }

    /**
     * Create new category
     */
    public String createCategory() {
        logger.info("=== CategoryController.createCategory() START ===");
        try {
            if (!validateCategoryInput()) {
                return null;
            }

            // Check if category name already exists
            Categories existingCategory = findCategoryByName(categoryName.trim());
            if (existingCategory != null) {
                addErrorMessage("Category name  '" + categoryName + "' already exists");
                return null;
            }

            // Create new category
            Categories newCategory = new Categories();
            newCategory.setCategoryName(categoryName.trim());

            // Save to database
            categoriesFacade.create(newCategory);

            // Refresh categories list
            loadCategories();

            // Clear form
            clearForm();

            addSuccessMessage("Category creation'" + categoryName + "'successful!");

            logger.info("Category created successfully: " + categoryName);
            logger.info("=== CategoryController.createCategory() END ===");

            return "admin/categories";

        } catch (Exception e) {
            logger.severe("Error creating category: " + e.getMessage());
            addErrorMessage("An error occurred while creating the category: " + e.getMessage());
            return null;
        }
    }

    /**
     * Select category for editing (populate form)
     * @param categoryId ID of category to edit
     */
    public void selectCategoryForEdit(Integer categoryId) {
        try {
            logger.info("=== CategoryController.selectCategoryForEdit() START ===");
            logger.info("Selecting category for edit ID: " + categoryId);
            
            if (categoryId == null) {
                logger.severe("Category ID is null!");
                addErrorMessage("Invalid category ID.");
                return;
            }
            
            // Get category from database
            Categories category = categoriesFacade.find(categoryId);
            if (category == null) {
                addErrorMessage("No category found with ID : " + categoryId);
                return;
            }
            
            // Set category to edit
            this.categoryToEdit = category;
            this.editingCategoryId = category.getCategoryID();
            
            // Populate form fields
            this.categoryName = category.getCategoryName();
            
            // Set edit mode
            this.editMode = true;
            
            logger.info("Form populated - CategoryName: " + this.categoryName);
            logger.info("Edit mode activated for category: " + category.getCategoryName());
            logger.info("=== CategoryController.selectCategoryForEdit() END ===");
            
        } catch (Exception e) {
            logger.severe("Error selecting category for edit: " + e.getMessage());
            addErrorMessage("An error occurred while selecting a category to edit. : " + e.getMessage());
        }
    }
    
    /**
     * Update category from form
     */
    public String updateCategory() {
        try {
            logger.info("=== CategoryController.updateCategory() START ===");
            
            if (categoryToEdit == null) {
                addErrorMessage("There are no categories to update.");
                return null;
            }
            
            // Validate input
            if (!validateCategoryInput()) {
                return null;
            }
            
            // Check if category name already exists (excluding current category)
            Categories existingCategory = findCategoryByName(categoryName.trim());
            if (existingCategory != null && !existingCategory.getCategoryID().equals(categoryToEdit.getCategoryID())) {
                addErrorMessage("Category Name '" + categoryName + "'has existed");
                return null;
            }
            
            // Update category data
            categoryToEdit.setCategoryName(categoryName.trim());
            
            // Save to database
            categoriesFacade.edit(categoryToEdit);
            
            // Refresh categories list
            loadCategories();
            
            // Clear form and reset edit mode
            clearForm();
            this.editMode = false;
            this.categoryToEdit = null;
            this.editingCategoryId = null;
            
            addSuccessMessage("Update catalog'" + categoryToEdit.getCategoryName() + "' success!");
            
            logger.info("Category updated successfully: " + categoryToEdit.getCategoryName());
            logger.info("=== CategoryController.updateCategory() END ===");
            
            return null;
            
        } catch (Exception e) {
            logger.severe("Error updating category: " + e.getMessage());
            addErrorMessage("An error occurred while updating the catalog.: " + e.getMessage());
            return null;
        }
    }

    /**
     * Open edit modal for category
     * @param categoryId ID of category to edit
     */
    public void openEditModal(Integer categoryId) {
        try {
            logger.info("=== CategoryController.openEditModal() START ===");
            logger.info("Opening edit modal for category ID: " + categoryId);
            
            if (categoryId == null) {
                logger.severe("Category ID is null!");
                addErrorMessage("ID Invalid category.");
                return;
            }
            
            // Get category from database
            Categories category = categoriesFacade.find(categoryId);
            if (category == null) {
                addErrorMessage("No category found with ID: " + categoryId);
                return;
            }
            
            // Set category to edit
            this.categoryToEdit = category;
            this.editingCategoryId = category.getCategoryID();
            
            // Populate form fields
            this.categoryName = category.getCategoryName();
            
            // Show modal
            this.showEditModal = true;
            this.editMode = true;
            
            logger.info("Form populated - CategoryName: " + this.categoryName);
            
            // Force FacesContext to update
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("editCategoryModal");
            FacesContext.getCurrentInstance().getPartialViewContext().getRenderIds().add("editCategoryModalForm");
            
            logger.info("Edit modal opened for category: " + category.getCategoryName());
            logger.info("=== CategoryController.openEditModal() END ===");
            
        } catch (Exception e) {
            logger.severe("Error opening edit modal: " + e.getMessage());
            addErrorMessage(" An error occurred while opening the edit modal: " + e.getMessage());
        }
    }
    
    /**
     * Close edit modal
     */
    public void closeEditModal() {
        this.showEditModal = false;
        this.categoryToEdit = null;
        this.editingCategoryId = null;
        this.editMode = false;
        clearForm();
    }
    
    /**
     * Update category from modal
     */
    public void updateCategoryFromModal() {
        try {
            logger.info("=== CategoryController.updateCategoryFromModal() START ===");
            
            // Get category ID from request parameter
            String categoryIdParam = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editingCategoryId");
            logger.info("editingCategoryId from request: " + categoryIdParam);
            
            if (categoryIdParam == null) {
                logger.severe("No category ID provided!");
                addErrorMessage("Không có ID danh mục để cập nhật.");
                return;
            }
            
            // Parse category ID
            Integer categoryId;
            try {
                categoryId = Integer.parseInt(categoryIdParam);
                logger.info("Parsed category ID: " + categoryId);
            } catch (NumberFormatException e) {
                logger.severe("Invalid category ID: " + categoryIdParam);
                addErrorMessage("ID danh mục không hợp lệ.");
                return;
            }
            
            // Load category from database
            Categories categoryToUpdate = categoriesFacade.find(categoryId);
            if (categoryToUpdate == null) {
                logger.severe("Category not found with ID: " + categoryId);
                addErrorMessage("Không tìm thấy danh mục với ID: " + categoryId);
                return;
            }
            
            logger.info("Loaded category from database: " + categoryToUpdate.getCategoryName() + " (ID: " + categoryToUpdate.getCategoryID() + ")");
            
            // Get form data from request parameters (user input)
            String formCategoryName = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap().get("editCategoryModalForm:editCategoryName");
            
            logger.info("Form data from request - CategoryName: " + formCategoryName);
            
            // Use form data if available, otherwise use loaded category data
            if (formCategoryName != null && !formCategoryName.trim().isEmpty()) {
                this.categoryName = formCategoryName;
                logger.info("Using form data from request parameters");
            } else {
                logger.info("Form data is null, populating from loaded category...");
                this.categoryName = categoryToUpdate.getCategoryName();
                logger.info("Form data populated from loaded category - CategoryName: " + this.categoryName);
            }
            
            // Validate input using the populated data
            logger.info("Validating input...");
            logger.info("Validation data - categoryName: " + this.categoryName);
            
            // Simple validation
            if (this.categoryName == null || this.categoryName.trim().isEmpty()) {
                addErrorMessage("Vui lòng nhập tên danh mục");
                return;
            }
            
            logger.info("Input validation passed!");
            
            // Check for name conflicts (only if name changed)
            if (!categoryToUpdate.getCategoryName().equals(categoryName.trim())) {
                Categories existingCategory = findCategoryByName(categoryName.trim());
                if (existingCategory != null && !existingCategory.getCategoryID().equals(categoryToUpdate.getCategoryID())) {
                    addErrorMessage("Tên danh mục '" + categoryName + "' đã tồn tại.");
                    return;
                }
            }
            
            // Log original data before update
            logger.info("BEFORE UPDATE - Name: " + categoryToUpdate.getCategoryName());
            
            // Update category data
            logger.info("Updating category data...");
            categoryToUpdate.setCategoryName(categoryName.trim());
            logger.info("AFTER UPDATE - Name: " + categoryToUpdate.getCategoryName());
            
            // Save to database
            logger.info("Saving to database...");
            categoriesFacade.edit(categoryToUpdate);
            logger.info("Category saved to database successfully!");
            
            // Verify data was saved by reloading from database
            Categories savedCategory = categoriesFacade.find(categoryToUpdate.getCategoryID());
            if (savedCategory != null) {
                logger.info("VERIFICATION - Saved category data - Name: " + savedCategory.getCategoryName());
            } else {
                logger.warning("Could not verify saved category data");
            }
            
            // Refresh categories list
            loadCategories();
            
            // Close modal immediately
            this.showEditModal = false;
            this.categoryToEdit = null;
            this.editingCategoryId = null;
            this.editMode = false;
            clearForm();
            
            addSuccessMessage("Cập nhật danh mục '" + categoryToUpdate.getCategoryName() + "' thành công!");
            
            logger.info("Category updated successfully: " + categoryToUpdate.getCategoryName());
            logger.info("Modal closed after successful update");
            logger.info("=== CategoryController.updateCategoryFromModal() END ===");
            
        } catch (Exception e) {
            logger.severe("Error updating category from modal: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi cập nhật danh mục: " + e.getMessage());
        }
    }

    /**
     * Navigate to edit page for category
     * @param categoryId category ID to edit
     * @return navigation string
     */
    public String navigateToEditPage(Integer categoryId) {
        try {
            logger.info("=== CategoryController.navigateToEditPage() START ===");
            logger.info("Navigating to edit page for category ID: " + categoryId);
            
            if (categoryId == null) {
                addErrorMessage("ID danh mục không hợp lệ.");
                return null;
            }
            
            // Lấy category từ database
            Categories category = categoriesFacade.find(categoryId);
            if (category == null) {
                addErrorMessage("Không tìm thấy danh mục với ID: " + categoryId);
                return null;
            }
            
            // Set category to edit
            this.categoryToEdit = category;
            
            // Populate form fields
            this.categoryName = category.getCategoryName();
            
            // Set edit mode
            this.editMode = true;
            
            logger.info("Navigating to edit page for category: " + category.getCategoryName());
            logger.info("=== CategoryController.navigateToEditPage() END ===");
            
            return "edit-category";
            
        } catch (Exception e) {
            logger.severe("Error navigating to edit page: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi mở trang chỉnh sửa: " + e.getMessage());
            return null;
        }
    }

    /**
     * Update category from edit page
     */
    public String updateCategoryFromEditPage() {
        try {
            logger.info("=== CategoryController.updateCategoryFromEditPage() START ===");
            
            if (categoryToEdit == null) {
                addErrorMessage("Không có danh mục để cập nhật.");
                return null;
            }
            
            // Validate input
            if (!validateCategoryInput()) {
                return null;
            }
            
            // Check if category name already exists (excluding current category)
            Categories existingCategory = findCategoryByName(categoryName.trim());
            if (existingCategory != null && !existingCategory.getCategoryID().equals(categoryToEdit.getCategoryID())) {
                addErrorMessage("Tên danh mục '" + categoryName + "' đã tồn tại");
                return null;
            }
            
            // Update category data
            categoryToEdit.setCategoryName(categoryName.trim());
            
            // Save to database
            categoriesFacade.edit(categoryToEdit);
            
            // Refresh categories list
            loadCategories();
            
            addSuccessMessage("Cập nhật danh mục '" + categoryToEdit.getCategoryName() + "' thành công!");
            
            logger.info("Category updated successfully: " + categoryToEdit.getCategoryName());
            logger.info("=== CategoryController.updateCategoryFromEditPage() END ===");
            
            return "admin/categories";
            
        } catch (Exception e) {
            logger.severe("Error updating category from edit page: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi cập nhật danh mục: " + e.getMessage());
            return null;
        }
    }

    /**
     * Delete category
     * @param category category to delete
     */
   public void deleteCategory(Categories category) {
    try {
        logger.info("=== CategoryController.deleteCategory() START ===");

        if (category == null) {
            addErrorMessage("Category does not exist.");
            return;
        }

        // Check if the category has related products
        if (category.getProductsCollection() != null && !category.getProductsCollection().isEmpty()) {
            addErrorMessage("Cannot delete category '" + category.getCategoryName() + "' because it still has related products.");
            return;
        }

        // Delete from database
        categoriesFacade.remove(category);

        // Refresh category list
        loadCategories();

        addSuccessMessage("Category '" + category.getCategoryName() + "' deleted successfully!");

        logger.info("Category deleted successfully: " + category.getCategoryName());
        logger.info("=== CategoryController.deleteCategory() END ===");

    } catch (Exception e) {
        logger.severe("Error while deleting category: " + e.getMessage());
        addErrorMessage("An error occurred while deleting category: " + e.getMessage());
    }
}


    /**
     * Search categories
     */
    public void searchCategories() {
        logger.info("=== CategoryController.searchCategories() START ===");
        try {
            if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
                loadCategories();
                return;
            }
            
            // Load all categories first
            loadCategories();
            
            // Filter categories by keyword
            List<Categories> filteredCategories = new ArrayList<>();
            String keyword = searchKeyword.trim().toLowerCase();
            
            for (Categories category : categories) {
                if (category.getCategoryName().toLowerCase().contains(keyword)) {
                    filteredCategories.add(category);
                }
            }
            
            categories = filteredCategories;
            logger.info("Found " + categories.size() + " categories matching: " + searchKeyword);
            
        } catch (Exception e) {
            logger.severe("Error searching categories: " + e.getMessage());
            addErrorMessage("Có lỗi xảy ra khi tìm kiếm: " + e.getMessage());
        }
        logger.info("=== CategoryController.searchCategories() END ===");
    }

    /**
     * Start create category
     */
    public void startCreateCategory() {
        clearForm();
        showCreateForm = true;
        editMode = false;
    }

    /**
     * Cancel operation
     */
    public String cancelOperation() {
        clearForm();
        showCreateForm = false;
        editMode = false;
        categoryToEdit = null;
        editingCategoryId = null;
        return "admin/categories";
    }

    /**
     * Cancel edit operation
     */
    public String cancelEdit() {
        clearForm();
        categoryToEdit = null;
        editMode = false;
        return "admin/categories";
    }

    /**
     * Back to category list
     */
    public String backToList() {
        return "admin/categories";
    }

    /**
     * Find category by name
     */
    private Categories findCategoryByName(String categoryName) {
        try {
            if (categories != null) {
                for (Categories category : categories) {
                    if (category.getCategoryName().equalsIgnoreCase(categoryName)) {
                        return category;
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Error finding category by name: " + e.getMessage());
        }
        return null;
    }

    /**
     * Validate category input
     */
    private boolean validateCategoryInput() {
        boolean isValid = true;

        if (categoryName == null || categoryName.trim().isEmpty()) {
            addErrorMessage("Vui lòng nhập tên danh mục");
            isValid = false;
        } else if (categoryName.trim().length() < 2) {
            addErrorMessage("Tên danh mục phải có ít nhất 2 ký tự");
            isValid = false;
        } else if (categoryName.trim().length() > 100) {
            addErrorMessage("Tên danh mục không được vượt quá 100 ký tự");
            isValid = false;
        }

        return isValid;
    }

    /**
     * Clear form
     */
    private void clearForm() {
        categoryName = "";
        searchKeyword = "";
        selectedCategory = null;
        categoryToEdit = null;
        showCreateForm = false;
        editMode = false;
        showEditModal = false;
    }

    /**
     * Add success message
     */
    private void addSuccessMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_INFO, "success", message));
    }

    /**
     * Add error message
     */
    private void addErrorMessage(String message) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", message));
    }

    // Getters and Setters
    public List<Categories> getCategories() {
        if (categories == null) {
            categories = new ArrayList<>();
        }
        return categories;
    }

    public void setCategories(List<Categories> categories) {
        this.categories = categories;
    }

    public Categories getSelectedCategory() {
        return selectedCategory;
    }

    public void setSelectedCategory(Categories selectedCategory) {
        this.selectedCategory = selectedCategory;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSearchKeyword() {
        return searchKeyword;
    }

    public void setSearchKeyword(String searchKeyword) {
        this.searchKeyword = searchKeyword;
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

    public Categories getCategoryToEdit() {
        return categoryToEdit;
    }

    public void setCategoryToEdit(Categories categoryToEdit) {
        this.categoryToEdit = categoryToEdit;
    }
    
    public Integer getEditingCategoryId() {
        return editingCategoryId;
    }
    
    public void setEditingCategoryId(Integer editingCategoryId) {
        this.editingCategoryId = editingCategoryId;
    }
}
