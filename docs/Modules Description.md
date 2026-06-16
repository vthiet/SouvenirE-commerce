## Modules Description - SouvenirE-commerce

Tài liệu này tổng hợp theo code hiện tại trong project (controller/service/DAO/JSP) để trả lời 3 câu hỏi cho từng module:
1. **Hiện trạng đang có**
2. **Các tính năng cơ bản còn thiếu**
3. **Trạng thái mong đợi (target)**

---

## 0) Tổng quan hiện trạng kiến trúc

- Dự án đã có đủ khung module chính: `auth`, `user`, `product/catalog`, `cart`, `order`, `dashboard`.
- Đã có nhiều endpoint hoạt động bằng `@WebServlet`.
- Đang tồn tại song song 2 hướng data access:
  - Hướng mới: `AbsBaseRepository` + Hibernate `getCurrentSession()`
  - Hướng cũ: nhiều `legacy.dao` dùng `openSession()` hoặc JDBC trực tiếp.
- Admin có nhiều trang (`/admin/*`) nhưng **chưa khóa quyền ở filter** vì `AuthFilter` đang bị comment mapping.

---

## 1) Module Xác thực & Phân quyền (`auth`)

### 1.1 Hiện trạng đang có

- Đăng nhập thường: `/login` (`LoginServlet`)
- Đăng nhập Google OAuth: `/login-google` (`LoginGoogleServlet`)
- Đăng xuất: `/logout` (`LogoutServlet`)
- Đăng ký + xác thực email bằng OTP:
  - gửi mã: `/api/signup/send-code`
  - verify mã: `/api/signup/verify-code`
  - tạo tài khoản: `/api/signup`, `/signup`
- Kiểm tra email tồn tại: `/api/check-email`, `/api/signup/check-email`
- Lưu user vào session theo nhiều key (`currentUser`, `userInSession`, `user`, `authUser`)

### 1.2 Các tính năng cơ bản còn thiếu

- **Quên mật khẩu**: UI đã có link `/forgot-password` nhưng **không có servlet/controller**.
- Chưa có luồng reset password qua email/token end-to-end.
- Chưa có cơ chế giới hạn số lần login sai (rate limit / lockout).
- Chưa có CSRF protection cho form đăng nhập/đăng ký.
- Chưa có central exception handling cho auth flow.

### 1.3 Trạng thái mong đợi

- Hoàn thiện luồng reset password 3 bước: nhập email -> xác thực mã/token -> đặt mật khẩu mới.
- Chuẩn hóa session principal về 1 key chính (ví dụ `authUser`) để giảm lỗi đồng bộ.
- Bổ sung security baseline: lockout tạm thời, throttling, CSRF token.

---

## 2) Module Người dùng & Phân quyền (`user`)

### 2.1 Hiện trạng đang có

- User profile:
  - xem/sửa profile: `/user/profile`
  - đổi avatar (Cloudinary): action `change_avatar`
  - đổi mật khẩu: `/user/change-password`
- Địa chỉ người dùng:
  - add/edit/delete/default qua `/user/address/*`
- Đơn hàng user:
  - danh sách + chi tiết: `/user/orders`
- Admin quản lý user:
  - `/admin/customers` (CRUD cơ bản + đổi trạng thái)
- Admin phân quyền:
  - `/admin/roles` (tạo/sửa/xóa role group, gán permission, gán user)

### 2.2 Các tính năng cơ bản còn thiếu

- **Authorization filter chưa active**:
  - `AuthFilter` không được map -> route `/admin/*` chưa được bảo vệ đúng theo role/permission.
- Tên role chưa đồng nhất với yêu cầu nghiệp vụ ban đầu (`SUPER_ADMIN`, `ADMIN`, `STAFF`, `CUSTOMER`):
  - code đang có `Super Admin`, `Admin`, `Sales`, `User`, `Customer`.
- Chưa enforce rõ quy tắc “chỉ SUPER_ADMIN được quản lý ADMIN/STAFF”.
- Chưa có module audit log khi đổi quyền hoặc khóa user.

### 2.3 Trạng thái mong đợi

- Bật filter bảo mật cho `/admin/*`, `/user/*`.
- Thống nhất role model chuẩn nghiệp vụ (mapping rõ ràng role cũ -> role mới).
- Tách rõ chính sách:
  - `SUPER_ADMIN`: role management toàn quyền
  - `ADMIN`: quản trị nghiệp vụ
  - `STAFF`: tác vụ vận hành
  - `CUSTOMER`: người mua.

---

## 3) Module Catalogue (`product`, `category`, `banner`)

### 3.1 Hiện trạng đang có

- Front-office:
  - Trang chủ: `/home`
  - Danh mục + filter/sort: `/category?id=...`
  - Chi tiết sản phẩm: `/product?id=...`
  - Search JSON: `/search`
  - Search suggestion (AJAX): `/search-suggestions`
  - Review theo sản phẩm: `/reviews`
- Back-office:
  - Quản lý sản phẩm: `/admin/products` (add/edit/delete/search/paging)
  - Quản lý danh mục: `/admin/categories`
  - Quản lý banner: `/admin/banner`

### 3.2 Các tính năng cơ bản còn thiếu

- **Brand module chưa tồn tại** (không thấy `Brand` entity/DAO/controller chuẩn).
- Chưa có quản lý media thống nhất (một phần local upload, một phần Cloudinary).
- Chưa có soft-delete và lifecycle rõ cho sản phẩm/danh mục ở toàn bộ flow.
- Chưa có API filter nâng cao cho search page (keyword + sort + paging đồng bộ JSON response).

### 3.3 Trạng thái mong đợi

- Bổ sung `Brand` đầy đủ: entity + admin CRUD + gắn sản phẩm + filter phía user.
- Chuẩn hóa upload ảnh theo 1 cơ chế duy nhất (ưu tiên cloud + lưu `public_id`).
- Chuẩn hóa endpoint catalog theo REST-ish pattern để frontend dễ mở rộng.

---

## 4) Module Giỏ hàng (`cart`)

### 4.1 Hiện trạng đang có

- Xem giỏ: `/cart`
- Thêm vào giỏ: `/cart/add`
- Xóa khỏi giỏ: `/cart/remove`
- Dữ liệu giỏ lưu `Session` bằng `Cart` + `CartItem`.

### 4.2 Các tính năng cơ bản còn thiếu

- `UpdateCartController` **chưa có `@WebServlet` mapping** -> endpoint update số lượng chưa được expose.
- Luồng hiện tại yêu cầu login mới thêm được cart (không đúng kỳ vọng guest cart phổ biến).
- Chưa có đồng bộ cart session -> database khi login.
- `RemoveCartController` redirect hard-code `"/cart"` (thiếu context path an toàn deploy).

### 4.3 Trạng thái mong đợi

- Hoàn thiện đầy đủ add/update/remove cho cả guest.
- Khi guest login, merge cart session vào cart user trong DB.
- Thêm validate stock tại thời điểm update + checkout.

---

## 5) Module Đơn hàng (`order`)

### 5.1 Hiện trạng đang có

- Checkout: `/checkout`
- Trang thành công: `/order-success`
- User xem đơn: `/user/orders`
- Admin xử lý đơn: `/admin/orders` (lọc trạng thái, cập nhật trạng thái, xem chi tiết)
- Export CSV báo cáo đơn hàng qua `/admin/export-report?type=orders`

### 5.2 Các tính năng cơ bản còn thiếu

- `OrderSuccessController` forward tới `/WEB-INF/views/order-success.jsp` nhưng file chưa tồn tại.
- Checkout đang fallback `userId = 1` cho guest (rủi ro dữ liệu và ownership).
- Chưa có luồng hủy đơn cho customer theo trạng thái.
- Chưa có payment integration thực (COD/chuyển khoản mới ở mức param form).
- Chưa có quy trình refund/return cho admin.
- Chưa có transaction boundary thống nhất toàn bộ DAO order (legacy DAO còn tách rời).

### 5.3 Trạng thái mong đợi

- Hoàn thiện state machine đơn hàng rõ ràng:
  - `PENDING -> CONFIRMED -> PACKING -> SHIPPING -> DELIVERED -> COMPLETED/CANCELLED`
- Tách order domain rõ: `Order`, `OrderItem`, `OrderStatus`, `Payment`, `Shipment`.
- Implement cancel/refund theo quyền + điều kiện trạng thái.

---

## 6) Module Dashboard & Báo cáo (`dashboard`)

### 6.1 Hiện trạng đang có

- Dashboard admin: `/admin/dashboard`
  - tổng sản phẩm, tổng khách hàng, doanh thu, đơn hàng
  - top products, recent orders, monthly data, low stock
- Trang admin nghiệp vụ:
  - `/admin/products`, `/admin/categories`, `/admin/customers`, `/admin/orders`, `/admin/roles`, `/admin/settings`, `/admin/banner`
- Export CSV:
  - summary / products / orders / customers

### 6.2 Các tính năng cơ bản còn thiếu

- Chưa tách dashboard theo vai trò STAFF/ADMIN/SUPER_ADMIN.
- Chưa có API thời gian thực hoặc chuẩn JSON cho chart (phụ thuộc render server-side là chính).
- Chưa có lịch gửi báo cáo định kỳ (email/report job).
- Chưa có monitoring/alerting cho chỉ số vận hành.

### 6.3 Trạng thái mong đợi

- Dashboard theo role:
  - STAFF: đơn hôm nay, đơn chờ xử lý, tồn kho thấp
  - ADMIN/SUPER_ADMIN: revenue, growth, customer acquisition, category performance.
- Bổ sung scheduled report và export theo phạm vi thời gian tùy chọn.

---

## 7) Các vấn đề nền tảng cần chốt trước khi mở rộng feature

1. **Bảo mật route**
   - Bật và kiểm thử `AuthFilter` + policy mapping bắt buộc cho `/admin/*`.
2. **Chuẩn hóa transaction**
   - Dồn dần từ `legacy DAO` về `currentSession + TransactionFilter` để tránh lệch commit/rollback.
3. **Chuẩn hóa dữ liệu phiên đăng nhập**
   - Chỉ dùng 1 key user chính trong session.
4. **Đóng các lỗ hổng functional**
   - Thêm servlet quên mật khẩu.
   - Tạo `order-success.jsp`.
   - Expose route update cart.
5. **Chuẩn hóa role model**
   - Chốt final role matrix và migrate dữ liệu role hiện tại.

---

## 8) Backlog ưu tiên đề xuất (theo thứ tự)

1. Bật `AuthFilter` + test phân quyền admin/user.
2. Hoàn thiện module quên mật khẩu.
3. Sửa cart flow (guest cart + update mapping + merge khi login).
4. Fix order success page + chuẩn hóa checkout không dùng `userId=1`.
5. Chuẩn hóa transaction ở các DAO order/user.
6. Bổ sung Brand module.
7. Tách dashboard theo role và thêm báo cáo định kỳ.