# Online Store - Login System với JSF + CDI

## 🎯 Tóm tắt
Đã hoàn thành việc tạo hệ thống đăng nhập cho ứng dụng Online Store sử dụng JSF và CDI.

## 📁 Các file đã tạo/cập nhật

### 1. **LoginBean.java** - CDI Managed Bean
- **Vị trí**: `online-store-war/src/java/a23088/controller/LoginBean.java`
- **Chức năng**: Xử lý đăng nhập, xác thực user, quản lý session
- **Annotations**: `@Named("loginBean")`, `@SessionScoped`

### 2. **login.xhtml** - Trang đăng nhập JSF
- **Vị trí**: `online-store-war/web/login.xhtml`
- **Tính năng**: Giao diện đẹp, responsive, validation

### 3. **beans.xml** - CDI Configuration
- **Vị trí**: `online-store-war/web/WEB-INF/beans.xml`
- **Chức năng**: Kích hoạt CDI cho ứng dụng

### 4. **web.xml** - Web Configuration (đã cập nhật)
- **Vị trí**: `online-store-war/web/WEB-INF/web.xml`
- **Cải tiến**: Thêm cấu hình CDI và JSF

## 🔧 Cách deploy và chạy

### Phương pháp 1: Sử dụng NetBeans IDE (Khuyến nghị)
1. Mở NetBeans IDE
2. File → Open Project → Chọn thư mục `online-store`
3. Right-click project → Clean and Build
4. Right-click project → Run

### Phương pháp 2: Sử dụng Ant (nếu có)
```bash
cd online-store
ant clean
ant run
```

### Phương pháp 3: Deploy thủ công
1. Build project trong NetBeans
2. Copy file `.war` từ `dist/` folder
3. Deploy vào GlassFish Server

## 🗄️ Database Schema

### Entity Users
- `userID` (Primary Key)
- `fullName` 
- `email` (dùng để đăng nhập)
- `password` (hash MD5)
- `status` (boolean - active/inactive)
- `roleID` (foreign key đến Roles)

### Entity Roles
- `roleID` (Primary Key)
- `roleName` (Admin/User)

## 🔐 Tính năng đăng nhập

### Xác thực
- Đăng nhập bằng email và password
- Hash password bằng MD5
- Kiểm tra trạng thái tài khoản
- Phân quyền theo role

### Navigation
- **Admin**: Redirect đến `/admin/index.xhtml`
- **User**: Redirect đến `/index.xhtml`
- **Lỗi**: Hiển thị thông báo trên trang login

### Session Management
- Lưu thông tin user vào HttpSession
- Các method tiện ích: `isUserLoggedIn()`, `isAdmin()`
- Đăng xuất và xóa session

## 🎨 Giao diện
- Design hiện đại với gradient background
- Responsive cho mobile
- Form validation với JSF
- Hiển thị lỗi đẹp mắt
- CSS animations và hover effects

## ⚠️ Lưu ý quan trọng

### Nếu gặp lỗi deployment:
1. **Kiểm tra GlassFish Server** đang chạy
2. **Kiểm tra Database Connection** trong persistence.xml
3. **Kiểm tra CDI** - file `beans.xml` phải có trong WEB-INF
4. **Kiểm tra JSF** - web.xml phải có cấu hình FacesServlet

### Troubleshooting:
- **"The module has not been deployed"**: Thường do thiếu `beans.xml` hoặc lỗi cấu hình CDI
- **"EntityManager not found"**: Kiểm tra persistence unit name
- **"Bean not found"**: Kiểm tra CDI annotations và beans.xml

## 🚀 Sẵn sàng sử dụng!
Hệ thống đăng nhập đã hoàn thiện và sẵn sàng để deploy lên GlassFish Server.
