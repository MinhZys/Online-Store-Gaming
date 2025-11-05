package a23088.controller;

import a23088.entity.Users;
import a23088.entity.Roles;
import a23088.mbean.RolesFacadeLocal;
import a23088.mbean.UsersFacadeLocal;
import jakarta.inject.Named;
import jakarta.enterprise.context.RequestScoped;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.ejb.EJB;
import java.io.Serializable;
import java.util.Date;

@Named(value = "registerBean")
@RequestScoped
public class RegisterBean implements Serializable {

    private static final long serialVersionUID = 1L;

    @EJB
    private UsersFacadeLocal usersFacade;

    @EJB
    private RolesFacadeLocal rolesFacade;

    // Form fields
    private String fullName;
    private String email;
    private String password;
    private String confirmPassword;

    /**
     * Constructor
     */
   

    /**
     * TEST METHOD - Very simple version
     */
    public String register() {
        try {
            // STEP 1: Basic validation
            if (fullName == null || fullName.trim().isEmpty()) {
                addError("Please enter your full name.");
                return null;
            }
            if (email == null || email.trim().isEmpty()) {
                addError("Please enter your email.");
                return null;
            }
            if (password == null || password.isEmpty()) {
                addError("Please enter your password.");
                return null;
            }
            if (password.length() < 6) {
                addError("Password must be at least 6 characters long.");
                return null;
            }
            if (!password.equals(confirmPassword)) {
                addError("Password confirmation does not match.");
                return null;
            }
            String emailLower = email.trim().toLowerCase();
            Users existing = usersFacade.findByEmail(emailLower);
            if (existing != null) {
                addError("This email is already registered.");
                return null;
            }

            // STEP 3: Get Role
            Roles role = rolesFacade.find(7);
            if (role == null) {
                addError("System error: Role does not exist.");
                return null;
            }

            Users user = new Users();
            user.setFullName(fullName.trim());
            user.setEmail(emailLower);
            user.setPasswordHashed(password);
            user.setStatus(true);
            user.setCreatedAt(new Date());
            user.setRoleID(role);

            usersFacade.create(user);
            addSuccess("Registration successful! Welcome, " + user.getFullName());
            clearForm();
            return "login?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            addError("An error occurred: " + e.getMessage());
            return null;
        }
    }

    /**
     * Clear form
     */
    public void clearForm() {
        fullName = null;
        email = null;
        password = null;
        confirmPassword = null;
    }

    /**
     * Add error message
     */
    private void addError(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", msg));
    }

    /**
     * Add success message
     */
    private void addSuccess(String msg) {
        FacesContext.getCurrentInstance().addMessage(null,
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", msg));
    }

    // ==================== Getters & Setters ====================
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        System.out.println(">> setFullName: " + fullName);
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        System.out.println(">> setEmail: " + email);
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
}
