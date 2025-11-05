package a23088.controller;

import a23088.entity.Products;
import a23088.entity.Categories;
import a23088.mbean.ProductsFacadeLocal;
import a23088.mbean.CategoriesFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.enterprise.context.SessionScoped;
import jakarta.faces.annotation.ManagedProperty;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named("productBean")
@SessionScoped
public class ProductBean implements Serializable {

    @EJB
    private ProductsFacadeLocal productsFacade;

    @EJB
    private CategoriesFacadeLocal categoriesFacade;

    @Inject
    private CartBean cartBean;

    private String searchKeyword;
    private Integer selectedCategoryId;
    private Integer minPrice;
    private Integer maxPrice;

    private int currentPage = 1;
    private int itemsPerPage = 8;

    private List<Products> products;
    private List<Products> filteredProducts;
    private List<Categories> categories;

    public ProductBean() {
        // default constructor
    }

    // Load initial data with optimized performance
    public void init() {
        if (products == null) {
            // Load data efficiently
            products = productsFacade.findPublished();
            categories = categoriesFacade.findAll();
            // Initialize with all products - no filtering applied initially
            filteredProducts = new ArrayList<>(products);
        }
    }

    public List<Products> getProducts() {
        if (products == null) {
            products = productsFacade.findPublished();
        }
        return products;
    }

    // Alias method để tương thích với code cũ
    public List<Products> getAllProducts() {
        return getProducts();
    }

    public List<Categories> getCategories() {
        if (categories == null) {
            categories = categoriesFacade.findAll();
        }
        return categories;
    }

    // Phương thức tìm kiếm từ khóa - chỉ hiển thị sản phẩm chứa từ khóa
    public void SearchKeyword() {
        try {
            System.out.println("=== SearchKeyword called ===");
            System.out.println("searchKeyword value: '" + searchKeyword + "'");
            
            if (searchKeyword != null && !searchKeyword.trim().isEmpty()) {
                String keyword = searchKeyword.trim().toLowerCase();
                System.out.println("Searching for keyword: '" + keyword + "'");
                
                // Lấy tất cả sản phẩm
                List<Products> allProducts = getProducts();
                System.out.println("Total products available: " + allProducts.size());
                
                // Tìm kiếm sản phẩm chứa từ khóa
                List<Products> searchResults = new ArrayList<>();
                for (Products product : allProducts) {
                    boolean containsKeyword = false;
                    
                    // Kiểm tra tên sản phẩm
                    if (product.getProductName() != null && 
                        product.getProductName().toLowerCase().contains(keyword)) {
                        containsKeyword = true;
                        System.out.println("✓ Found in name: " + product.getProductName());
                    }
                    
                    // Kiểm tra mô tả
                    if (!containsKeyword && product.getDescription() != null && 
                        product.getDescription().toLowerCase().contains(keyword)) {
                        containsKeyword = true;
                        System.out.println("✓ Found in description: " + product.getProductName());
                    }
                    
                    // Kiểm tra thương hiệu
                    if (!containsKeyword && product.getBrand() != null && 
                        product.getBrand().toLowerCase().contains(keyword)) {
                        containsKeyword = true;
                        System.out.println("✓ Found in brand: " + product.getProductName());
                    }
                    
                    if (containsKeyword) {
                        searchResults.add(product);
                    }
                }
                
                filteredProducts = searchResults;
                System.out.println("Search completed. Found " + filteredProducts.size() + " products containing '" + keyword + "'");
                
            } else {
                // Không có từ khóa, hiển thị tất cả sản phẩm
                filteredProducts = getProducts();
                System.out.println("No keyword provided, showing all products");
            }
            
            currentPage = 1;
            System.out.println("=== SearchKeyword completed ===");
            
        } catch (Exception e) {
            System.out.println("Error in SearchKeyword: " + e.getMessage());
            e.printStackTrace();
            // Fallback: hiển thị tất cả sản phẩm
            filteredProducts = getProducts();
            currentPage = 1;
        }
    }
    

    // Apply filters with optimized performance
    public void applyFilters() {
        // Sử dụng phương thức SearchKeyword() để tìm kiếm
        SearchKeyword();
        
        // Sau đó áp dụng các bộ lọc khác
        if (filteredProducts != null) {
            filteredProducts = filteredProducts.stream()
                .filter(product -> {
                    // Fast category filter - only filter if a category is actually selected
                    if (selectedCategoryId != null && selectedCategoryId > 0) {
                        if (product.getCategoryID() == null || !product.getCategoryID().getCategoryID().equals(selectedCategoryId)) {
                            return false;
                        }
                    }

                    // Fast price filter
                    double price = product.getPrice();
                    if (minPrice != null && minPrice > 0 && price < minPrice) {
                        return false;
                    }
                    if (maxPrice != null && maxPrice > 0 && price > maxPrice) {
                        return false;
                    }

                    return true;
                })
                .collect(Collectors.toList());
        }
    }

    // Optimized pagination
    public List<Products> getFilteredProducts() {
        if (filteredProducts == null) {
            // If no filters have been applied, show all products
            filteredProducts = new ArrayList<>(getProducts());
        }
        return filteredProducts;
    }

    public List<Products> getPagedProducts() {
        List<Products> filtered = getFilteredProducts();
        int size = filtered.size();
        int start = (currentPage - 1) * itemsPerPage;
        
        // Fast bounds checking
        if (start >= size) return new ArrayList<>();
        
        int end = Math.min(start + itemsPerPage, size);
        return filtered.subList(start, end);
    }

    public void nextPage() {
        if (currentPage * itemsPerPage < getFilteredProducts().size()) {
            currentPage++;
        }
    }

    public void previousPage() {
        if (currentPage > 1) {
            currentPage--;
        }
    }


    // Reset tất cả bộ lọc
    public void resetFilters() {
        searchKeyword = null;
        selectedCategoryId = null;
        minPrice = null;
        maxPrice = null;
        currentPage = 1;
        // Reset to show all products
        filteredProducts = new ArrayList<>(getProducts());
    }
    
    

    // Các thuộc tính và phương thức cho trang chi tiết sản phẩm
    private Products currentProduct;
    private int quantity = 1;
    private List<Products> relatedProducts;
    private List<Object> reviews;
    private double averageRating = 0.0;
    private int reviewCount = 0;

    // Phương thức preRenderView cho trang chi tiết
    public void preRenderView(jakarta.faces.event.ComponentSystemEvent event) {
        // Lấy ID sản phẩm từ parameter
        String productId = jakarta.faces.context.FacesContext.getCurrentInstance()
                .getExternalContext().getRequestParameterMap().get("id");
        
        if (productId != null) {
            try {
                Integer id = Integer.parseInt(productId);
                currentProduct = productsFacade.find(id);
                if (currentProduct != null) {
                    loadRelatedProducts();
                    loadReviews();
                }
            } catch (NumberFormatException e) {
                // Xử lý lỗi nếu ID không hợp lệ
                currentProduct = null;
            }
        }
    }

    // Load sản phẩm liên quan
    private void loadRelatedProducts() {
        if (currentProduct != null && currentProduct.getCategoryID() != null) {
            relatedProducts = productsFacade.findPublished().stream()
                .filter(p -> !p.getProductID().equals(currentProduct.getProductID()) 
                        && p.getCategoryID() != null 
                        && p.getCategoryID().getCategoryID().equals(currentProduct.getCategoryID().getCategoryID()))
                .limit(4)
                .collect(Collectors.toList());
        } else {
            relatedProducts = new ArrayList<>();
        }
    }

    // Load đánh giá (demo - cần implement thực tế)
    private void loadReviews() {
        reviews = new ArrayList<>();
        averageRating = 4.5; // Demo rating
        reviewCount = 12; // Demo count
    }

    // Format giá tiền
    public String formatPrice(int price) {
        return String.format("%,d", price);
    }

    // Tạo chuỗi sao đánh giá
    public String getStarsDisplay(double rating) {
        StringBuilder stars = new StringBuilder();
        int fullStars = (int) rating;
        for (int i = 0; i < fullStars; i++) {
            stars.append("★");
        }
        return stars.toString();
    }

    // Getter/Setter cho trang chi tiết
    public Products getCurrentProduct() { return currentProduct; }
    public void setCurrentProduct(Products currentProduct) { this.currentProduct = currentProduct; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public List<Products> getRelatedProducts() { return relatedProducts; }
    public void setRelatedProducts(List<Products> relatedProducts) { this.relatedProducts = relatedProducts; }

    public List<Object> getReviews() { return reviews; }
    public void setReviews(List<Object> reviews) { this.reviews = reviews; }

    public double getAverageRating() { return averageRating; }
    public void setAverageRating(double averageRating) { this.averageRating = averageRating; }

    public int getReviewCount() { return reviewCount; }
    public void setReviewCount(int reviewCount) { this.reviewCount = reviewCount; }

    // Getter/Setter
    public String getSearchKeyword() { return searchKeyword; }
    public void setSearchKeyword(String searchKeyword) { this.searchKeyword = searchKeyword; }

    public Integer getSelectedCategoryId() { return selectedCategoryId; }
    public void setSelectedCategoryId(Integer selectedCategoryId) { this.selectedCategoryId = selectedCategoryId; }

    public Integer getMinPrice() { return minPrice; }
    public void setMinPrice(Integer minPrice) { this.minPrice = minPrice; }

    public Integer getMaxPrice() { return maxPrice; }
    public void setMaxPrice(Integer maxPrice) { this.maxPrice = maxPrice; }

    public int getCurrentPage() { return currentPage; }
    public int getItemsPerPage() { return itemsPerPage; }

    public void setCartBean(CartBean cartBean) {
        this.cartBean = cartBean;
    }
    
    // Get recommended products for cart page
    public List<Products> getRecommendedProducts() {
        if (relatedProducts != null && !relatedProducts.isEmpty()) {
            return relatedProducts;
        }
        
        // If no related products, return some random published products
        List<Products> allProducts = getProducts();
        if (allProducts.size() > 4) {
            return allProducts.subList(0, 4);
        }
        return allProducts;
    }
}
