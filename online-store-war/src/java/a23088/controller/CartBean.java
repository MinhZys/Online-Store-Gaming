package a23088.controller;

import a23088.entity.Cart;
import a23088.entity.CartItems;
import a23088.entity.OrderItems;
import a23088.entity.Orders;
import a23088.entity.Products;
import a23088.entity.Users;
import a23088.mbean.CartFacadeLocal;
import a23088.mbean.CartItemsFacadeLocal;
import a23088.mbean.OrderItemsFacadeLocal;
import a23088.mbean.OrdersFacadeLocal;
import a23088.mbean.ProductsFacadeLocal;
import a23088.mbean.UsersFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.ejb.TransactionAttribute;
import jakarta.ejb.TransactionAttributeType;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.context.FacesContext;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Named("cartBean")
@SessionScoped
public class CartBean implements Serializable {

    @EJB
    private ProductsFacadeLocal productsFacade;
    
    @EJB
    private OrdersFacadeLocal ordersFacade;
    
    @EJB
    private OrderItemsFacadeLocal orderItemsFacade;
    
    @EJB
    private UsersFacadeLocal usersFacade;
    
    @EJB
    private CartFacadeLocal cartFacade;
    
    @EJB
    private CartItemsFacadeLocal cartItemsFacade;
    
    @Inject
    private LoginBean loginBean;

    private List<CartItems> cartItems;
    private Cart currentCart;
    private int totalItems = 0;
    private double totalAmount = 0.0;

    // Test properties
    private Integer testProductId = 1;
    private Integer testQuantity = 1;

    public CartBean() {
        cartItems = new ArrayList<>();
    }

    // Initialize cart
    public void init() {
        loadCartFromDatabase();
    }
    
    // Load cart from database for current user
    private void loadCartFromDatabase() {
        try {
            Users currentUser = loginBean.getCurrentUser();
            if (currentUser == null) {
                cartItems = new ArrayList<>();
                currentCart = null;
                updateCartSummary();
                return;
            }
            
            // Get or create cart for user
            currentCart = getOrCreateUserCart(currentUser);
            
            if (currentCart != null) {
                // Load cart items from database
                cartItems = cartItemsFacade.findByCartID(currentCart.getCartID());
                if (cartItems == null) {
                    cartItems = new ArrayList<>();
                }
            } else {
                cartItems = new ArrayList<>();
            }
            
            updateCartSummary();
        } catch (Exception e) {
            e.printStackTrace();
            cartItems = new ArrayList<>();
            currentCart = null;
            updateCartSummary();
        }
    }
    
    // Get existing cart or create new one for user
    private Cart getOrCreateUserCart(Users user) {
        try {
            // Try to find existing cart for user
            List<Cart> userCarts = cartFacade.findByUserID(user.getUserID());
            if (userCarts != null && !userCarts.isEmpty()) {
                // Return the first cart (assuming one cart per user)
                return userCarts.get(0);
            }
            
            // Create new cart for user
            Cart newCart = new Cart();
            newCart.setUserID(user);
            newCart.setCreatedAt(new Date());
            cartFacade.create(newCart);
            
            return newCart;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    private Integer quantity = 1;

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    // Add product to cart with database persistence
    public String addToCart(Integer productId, Integer quantity) {
        try {
            // Check if user is logged in
            Users currentUser = loginBean.getCurrentUser();
            if (currentUser == null) {
                addErrorMessage("Bạn cần đăng nhập để thêm sản phẩm vào giỏ hàng!");
                return "login?faces-redirect=true";
            }

            // Fast input validation
            if (productId == null || quantity == null || quantity <= 0) {
                addErrorMessage("Invalid product or quantity");
                return null;
            }

            // Get product with error handling
            Products product = productsFacade.find(productId);
            if (product == null) {
                addErrorMessage("Product not found");
                return null;
            }

            // Quick availability check
            if (!product.getStatus()) {
                addErrorMessage("Product is not available");
                return null;
            }

            if (product.getStock() < quantity) {
                addErrorMessage("Insufficient stock. Available: " + product.getStock());
                return null;
            }

            // Ensure cart exists for user
            if (currentCart == null) {
                currentCart = getOrCreateUserCart(currentUser);
            }

            // Find existing cart item
            CartItems existingItem = findCartItemByProduct(productId);

            if (existingItem != null) {
                // Check stock before updating
                int newTotalQuantity = existingItem.getQuantity() + quantity;
                if (newTotalQuantity > product.getStock()) {
                    addErrorMessage("Cannot add more items. Available stock: " + product.getStock());
                    return null;
                }

                // Update quantity in database
                existingItem.setQuantity(newTotalQuantity);
                cartItemsFacade.edit(existingItem);
                addSuccessMessage("Updated " + product.getProductName() + " quantity to " + newTotalQuantity);
            } else {
                // Create new cart item and save to database
                CartItems newItem = new CartItems();
                newItem.setCartID(currentCart);
                newItem.setProductID(product);
                newItem.setQuantity(quantity);
                
                cartItemsFacade.create(newItem);
                cartItems.add(newItem);
                addSuccessMessage("Added " + product.getProductName() + " to cart");
            }

            // Update summary efficiently
            updateCartSummary();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error adding product to cart: " + e.getMessage());
            return null;
        }
    }
    
    
    
    
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String checkout() {
        try {
            // 1️⃣ Lấy thông tin user hiện tại từ LoginBean
            Users currentUser = loginBean.getCurrentUser();

            if (currentUser == null) {
                addErrorMessage("Bạn cần đăng nhập để thanh toán!");
                return "login?faces-redirect=true";
            }

            if (cartItems == null || cartItems.isEmpty()) {
                addErrorMessage("Giỏ hàng đang trống!");
                return null;
            }

            // 2️⃣ Tạo đơn hàng mới (Orders) với thông tin mặc định
            Orders order = new Orders();
            order.setOrderDate(new Date());
            order.setStatus("Pending");
            // Không set paymentMethod nữa - để database tự xử lý
            order.setTotalAmount((int) totalAmount);
            order.setAddress("Sẽ cập nhật sau"); // Địa chỉ mặc định
            order.setPhone("Sẽ cập nhật sau"); // Số điện thoại mặc định
            order.setUserID(currentUser);

            ordersFacade.create(order); // lưu Orders vào DB

            // 3️⃣ Lưu chi tiết từng sản phẩm vào OrderItems và cập nhật stock
            for (CartItems cartItem : cartItems) {
                OrderItems item = new OrderItems();
                item.setOrderID(order);
                item.setProductID(cartItem.getProductID());
                item.setQuantity(cartItem.getQuantity());
                item.setUnitPrice(cartItem.getProductID().getPrice());
                orderItemsFacade.create(item);
                
                // 4️⃣ Cập nhật stock sản phẩm
                Products product = cartItem.getProductID();
                product.setStock(product.getStock() - cartItem.getQuantity());
                productsFacade.edit(product);
            }

            // 5️⃣ Lưu ID đơn hàng vừa tạo để hiển thị trong trang thành công
            FacesContext.getCurrentInstance().getExternalContext()
                    .getSessionMap().put("currentOrder", order);

            // 6️⃣ Xóa giỏ hàng sau khi lưu thành công
            clearCart();

            // Log thông báo tạo đơn hàng thành công ra console
            System.out.println("==========================================");
            System.out.println("🛒 ĐƠN HÀNG ĐÃ ĐƯỢC TẠO THÀNH CÔNG! 🛒");
            System.out.println("==========================================");
            System.out.println("Mã đơn hàng: #" + order.getOrderID());
            System.out.println("Khách hàng: " + currentUser.getFullName());
            System.out.println("Email: " + currentUser.getEmail());
            System.out.println("Tổng tiền: " + String.format("%,.0f", (double)order.getTotalAmount()) + "₫");
            System.out.println("Số sản phẩm: " + cartItems.size());
            System.out.println("Trạng thái: " + order.getStatus());
            System.out.println("Thời gian: " + new java.util.Date());
            System.out.println("==========================================");

            addSuccessMessage("Đơn hàng đã được tạo thành công! Mã đơn hàng: #" + order.getOrderID());
            
            // 7️⃣ Chuyển hướng sang trang xác nhận thanh toán
            return "checkout?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Lỗi khi tạo đơn hàng: " + e.getMessage());
            return null;
        }
    }

    
    
    
    

    // Find product in cart
    private CartItems findCartItemByProduct(Integer productId) {
        for (CartItems item : cartItems) {
            if (item.getProductID().getProductID().equals(productId)) {
                return item;
            }
        }
        return null;
    }

    // Update product quantity with database persistence
    public void updateQuantity(CartItems item, int newQuantity) {
        try {
            if (newQuantity <= 0) {
                removeFromCart(item);
                addSuccessMessage("Removed " + item.getProductID().getProductName() + " from cart");
                return;
            }

            // Fast stock check
            if (newQuantity > item.getProductID().getStock()) {
                addErrorMessage("Cannot update quantity. Available stock: " + item.getProductID().getStock());
                return;
            }

            // Update quantity in database
            item.setQuantity(newQuantity);
            cartItemsFacade.edit(item);
            updateCartSummary();
            addSuccessMessage("Updated " + item.getProductID().getProductName() + " quantity to " + newQuantity);
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error updating quantity: " + e.getMessage());
        }
    }

    // Remove product from cart with database persistence
    public void removeFromCart(CartItems item) {
        try {
            String productName = item.getProductID().getProductName();
            
            // Remove from database
            cartItemsFacade.remove(item);
            
            // Remove from local list
            cartItems.remove(item);
            updateCartSummary();
            addSuccessMessage("Removed " + productName + " from cart");
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error removing item from cart: " + e.getMessage());
        }
    }

    // Remove all products from cart with database persistence
    public void clearCart() {
        try {
            // Remove all cart items from database
            for (CartItems item : cartItems) {
                cartItemsFacade.remove(item);
            }
            
            // Clear local list
            cartItems.clear();
            updateCartSummary();
            addSuccessMessage("Cart cleared successfully");
        } catch (Exception e) {
            e.printStackTrace();
            addErrorMessage("Error clearing cart: " + e.getMessage());
        }
    }

    // Get current cart (simplified for session-based cart)
    private Cart getCurrentCartInternal() {
        // For session-based cart, we don't need to persist to database
        // This method is kept for compatibility but not used in current implementation
        return null;
    }

    // Update cart summary with optimized performance
    private void updateCartSummary() {
        totalItems = 0;
        totalAmount = 0.0;

        // Use enhanced for loop for better performance
        for (CartItems item : cartItems) {
            int quantity = item.getQuantity();
            double price = item.getProductID().getPrice();

            totalItems += quantity;
            totalAmount += price * quantity;
        }
    }

    // Get list of products in cart
    public List<CartItems> getCartItems() {
        // Load from database if not already loaded
        if (cartItems == null) {
            loadCartFromDatabase();
        }
        updateCartSummary();
        return cartItems;
    }

    // Load products from session (simplified for session-based cart)
    private void loadCartItems() {
        // For session-based cart, items are already in the cartItems list
        // No need to load from database
        if (cartItems == null) {
            cartItems = new ArrayList<>();
        }
        updateCartSummary();
    }

    // Check if cart is empty
    public boolean isCartEmpty() {
        // Load from database if not already loaded
        if (cartItems == null) {
            loadCartFromDatabase();
        }
        return cartItems == null || cartItems.isEmpty();
    }

    // Format price
    public String formatPrice(double price) {
        return String.format("%,.0f", price);
    }

    // Calculate total for an item
    public double getItemTotal(CartItems item) {
        return item.getProductID().getPrice() * item.getQuantity();
    }

    // Getter/Setter
    public int getTotalItems() {
        return totalItems;
    }

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Cart getCurrentCart() {
        return currentCart;
    }

    public void setCurrentCart(Cart currentCart) {
        this.currentCart = currentCart;
    }

    // Test properties getters and setters
    public Integer getTestProductId() {
        return testProductId;
    }

    public void setTestProductId(Integer testProductId) {
        this.testProductId = testProductId;
    }

    public Integer getTestQuantity() {
        return testQuantity;
    }

    public void setTestQuantity(Integer testQuantity) {
        this.testQuantity = testQuantity;
    }

    // Cập nhật thông tin đơn hàng với transaction
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public String updateOrderInfo(String address, String phone, Integer orderId) {
        try {
            // Tìm đơn hàng theo ID
            Orders order = ordersFacade.find(orderId);
            if (order == null) {
                addErrorMessage("Không tìm thấy đơn hàng!");
                return null;
            }

            // Cập nhật thông tin
            order.setAddress(address.trim());
            order.setPhone(phone.trim());
            order.setStatus("Confirmed");
            
            // Lưu cập nhật
            ordersFacade.edit(order);

            addSuccessMessage("Đơn hàng đã được xác nhận thành công!");
            return "order-success?faces-redirect=true";

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = "Lỗi khi cập nhật đơn hàng: " + e.getMessage();
            if (e.getCause() != null) {
                errorMessage += " - Nguyên nhân: " + e.getCause().getMessage();
            }
            addErrorMessage(errorMessage);
            return null;
        }
    }

    // Message handling methods
    private void addSuccessMessage(String message) {
        jakarta.faces.context.FacesContext.getCurrentInstance().addMessage(null,
                new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_INFO,
                        message, null));
    }

    private void addErrorMessage(String message) {
        jakarta.faces.context.FacesContext.getCurrentInstance().addMessage(null,
                new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR,
                        message, null));
    }
}
