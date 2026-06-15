# Souvenir E-commerce

## Tài Liệu Dự Án Theo Chuẩn IEEE

**Phiên bản tài liệu:** 1.0  
**Artifact:** `vn.edu.nlu.fit:BACKEND:1.0-SNAPSHOT`  
**Kiểu đóng gói:** WAR  
**Ngôn ngữ chính:** Java 21  
**Ngày cập nhật:** 2026-06-02

---

## Tóm Tắt

Souvenir E-commerce là hệ thống thương mại điện tử phục vụ kinh doanh quà lưu niệm
và đặc sản địa phương. Hệ thống cung cấp các chức năng phía khách hàng như đăng ký,
đăng nhập, xem sản phẩm, tìm kiếm, quản lý giỏ hàng, thanh toán, theo dõi đơn hàng
và đánh giá sản phẩm. Phía quản trị hỗ trợ quản lý sản phẩm, danh mục, banner, khách
hàng, đơn hàng, vai trò, cấu hình hệ thống, thống kê dashboard và xuất báo cáo CSV.

README này được trình bày theo phong cách tài liệu IEEE, điều chỉnh cho phạm vi tài
liệu cấp repository. Nội dung bao gồm phạm vi, yêu cầu, kiến trúc, cấu hình, cài đặt,
kiểm thử và quy ước phát triển.

**Từ khóa:** Java Servlet, JSP, Hibernate ORM, MySQL, HikariCP, RBAC, E-commerce,
Maven, WAR.

---

## Mục Lục

1. [Giới Thiệu](#1-giới-thiệu)
2. [Mô Tả Tổng Quan](#2-mô-tả-tổng-quan)
3. [Yêu Cầu Hệ Thống](#3-yêu-cầu-hệ-thống)
4. [Kiến Trúc Và Thiết Kế](#4-kiến-trúc-và-thiết-kế)
5. [Đặc Tả Chức Năng](#5-đặc-tả-chức-năng)
6. [Dữ Liệu Và Cấu Hình](#6-dữ-liệu-và-cấu-hình)
7. [Cài Đặt Và Vận Hành](#7-cài-đặt-và-vận-hành)
8. [Kiểm Thử](#8-kiểm-thử)
9. [Quy Ước Phát Triển](#9-quy-ước-phát-triển)
10. [Giới Hạn Hiện Tại](#10-giới-hạn-hiện-tại)
11. [Cấu Trúc Dự Án](#11-cấu-trúc-dự-án)
12. [Tài Liệu Tham Khảo](#12-tài-liệu-tham-khảo)

---

## 1. Giới Thiệu

### 1.1 Mục Đích

Tài liệu này mô tả repository Souvenir E-commerce ở mức kỹ thuật. Đối tượng sử dụng
gồm lập trình viên, người bảo trì, người review, giảng viên hoặc thành viên mới cần
cài đặt, chạy, đánh giá và mở rộng hệ thống.

### 1.2 Phạm Vi

Dự án là ứng dụng web render phía server, sử dụng Jakarta Servlet và JSP. Phạm vi
chức năng chính gồm:

- Website khách hàng: trang chủ, danh mục, chi tiết sản phẩm, tìm kiếm, đánh giá,
  giỏ hàng và đặt hàng.
- Khu vực người dùng: hồ sơ cá nhân, avatar, mật khẩu, địa chỉ, đánh giá và lịch sử
  đơn hàng.
- Khu vực quản trị: dashboard, sản phẩm, danh mục, banner, khách hàng, đơn hàng,
  vai trò và cấu hình hệ thống.
- Xác thực bằng username/email và hỗ trợ Google OAuth.
- Truy cập dữ liệu bằng Hibernate ORM và một số DAO/JDBC legacy.
- Tích hợp Cloudinary cho upload ảnh.
- Hỗ trợ gửi email xác thực trong luồng đăng ký.

### 1.3 Thuật Ngữ Và Từ Viết Tắt

| Thuật ngữ | Diễn giải |
| --- | --- |
| DAO | Data Access Object |
| DTO | Data Transfer Object |
| JSP | JavaServer Pages |
| ORM | Object-Relational Mapping |
| RBAC | Role-Based Access Control |
| WAR | Web Application Archive |

---

## 2. Mô Tả Tổng Quan

### 2.1 Bối Cảnh Sản Phẩm

Souvenir E-commerce là ứng dụng web nguyên khối. Server chịu trách nhiệm định tuyến
qua Servlet, render JSP, quản lý session và lưu trữ dữ liệu vào MySQL.

Codebase đang chuyển dần từ mô hình DAO/JDBC cũ sang kiến trúc package theo tính
năng, kết hợp service và repository dùng Hibernate.

### 2.2 Nhóm Người Dùng

| Nhóm | Mô tả |
| --- | --- |
| Guest | Xem trang chủ, danh mục, chi tiết sản phẩm, tìm kiếm, đăng ký và đăng nhập. |
| Customer | Quản lý hồ sơ, địa chỉ, avatar, mật khẩu, giỏ hàng, thanh toán, đơn hàng và đánh giá. |
| Staff/Admin | Thao tác các trang quản trị theo quyền được cấp. |
| Super Admin | Quản lý vai trò, quyền và các nghiệp vụ quản trị cấp hệ thống. |

### 2.3 Môi Trường Vận Hành

| Thành phần | Phiên bản yêu cầu / đang dùng |
| --- | --- |
| JDK | 21 |
| Build tool | Maven Wrapper / Maven |
| Servlet container | Container tương thích Jakarta Servlet 6, ví dụ Tomcat 10.1+ |
| Database | MySQL |
| ORM | Hibernate ORM 7.3.0.Final |
| Connection pool | HikariCP 7.0.2 |
| View technology | JSP + JSTL |

---

## 3. Yêu Cầu Hệ Thống

### 3.1 Yêu Cầu Chức Năng

| Mã | Yêu cầu |
| --- | --- |
| FR-01 | Hệ thống cho phép đăng ký, xác thực mã email, đăng nhập, đăng nhập Google và đăng xuất. |
| FR-02 | Hệ thống hiển thị trang chủ, danh mục, banner, thẻ sản phẩm, chi tiết sản phẩm và đánh giá. |
| FR-03 | Hệ thống cung cấp tìm kiếm sản phẩm và gợi ý tìm kiếm. |
| FR-04 | Hệ thống cho phép customer quản lý giỏ hàng và tiến hành thanh toán. |
| FR-05 | Hệ thống cho phép người dùng quản lý hồ sơ, avatar, địa chỉ, mật khẩu, đơn hàng và đánh giá. |
| FR-06 | Hệ thống cho phép admin quản lý sản phẩm, danh mục, banner, khách hàng, đơn hàng, vai trò và cấu hình. |
| FR-07 | Hệ thống cung cấp dashboard thống kê và xuất báo cáo CSV cho quản trị. |

### 3.2 Yêu Cầu Phi Chức Năng

| Mã | Yêu cầu |
| --- | --- |
| NFR-01 | Hệ thống sử dụng connection pool khi truy cập cơ sở dữ liệu. |
| NFR-02 | Hệ thống ưu tiên quản lý Hibernate session và transaction theo request thông qua filter. |
| NFR-03 | Thông tin database, mail và Cloudinary phải được cấu hình ngoài mã nguồn nghiệp vụ. |
| NFR-04 | Hệ thống ghi log bằng Logback. |
| NFR-05 | Hệ thống được đóng gói thành WAR để deploy lên runtime Jakarta Servlet 6. |

---

## 4. Kiến Trúc Và Thiết Kế

### 4.1 Phong Cách Kiến Trúc

Codebase tổ chức chính theo package tính năng:

```text
nlu.fit.web.souvenirecommerce
├── common      Lớp nền tảng, enum và tiện ích dùng chung
├── core        Cấu hình Hibernate, filter, exception, annotation
├── features    Các module auth, cart, dashboard, order, product, user
├── legacy      DAO/model/controller cũ đang được giữ trong quá trình migrate
└── model       Hibernate entity
```

### 4.2 Trách Nhiệm Theo Tầng

| Tầng | Trách nhiệm |
| --- | --- |
| Servlet / Controller | Nhận HTTP request, kiểm tra tham số, gọi service, forward JSP hoặc trả JSON. |
| Service | Xử lý nghiệp vụ và phối hợp repository/DAO. |
| Repository / DAO | Truy cập database bằng Hibernate hoặc JDBC legacy. |
| Entity / Model | Biểu diễn dữ liệu domain được lưu trữ. |
| JSP / Assets | Render giao diện server-side, CSS và JavaScript phía trình duyệt. |
| Filter | Áp dụng logging, header và vòng đời transaction cho request. |

### 4.3 Thành Phần Runtime Chính

| Thành phần | Vị trí |
| --- | --- |
| Hibernate bootstrap | `src/main/java/nlu/fit/web/souvenirecommerce/core/config/HibernateUtil.java` |
| Startup/shutdown listener | `src/main/java/nlu/fit/web/souvenirecommerce/core/config/DbContextListener.java` |
| Logging filter | `src/main/java/nlu/fit/web/souvenirecommerce/core/filters/LoggingFilter.java` |
| Transaction filter | `src/main/java/nlu/fit/web/souvenirecommerce/core/filters/TransactionFilter.java` |
| Header filter | `src/main/java/nlu/fit/web/souvenirecommerce/core/filters/HeaderFilter.java` |
| Servlet configuration | `src/main/webapp/WEB-INF/web.xml` |

### 4.4 Thư Viện Chính

| Dependency | Mục đích |
| --- | --- |
| `hibernate-core`, `hibernate-hikaricp` | ORM và tích hợp connection pool cho Hibernate |
| `mysql-connector-j` | MySQL JDBC driver |
| `HikariCP` | Connection pool |
| `jakarta.servlet.jsp.jstl` | JSTL cho JSP |
| `jackson-databind`, `gson` | Xử lý JSON |
| `cloudinary-http5` | Upload và quản lý ảnh qua Cloudinary |
| `jakarta.mail` | Gửi email |
| `jbcrypt` | Hash mật khẩu |
| `logback-classic` | Logging |
| `junit-jupiter` | Unit test |
| `lombok` | Giảm boilerplate code |

---

## 5. Đặc Tả Chức Năng

### 5.1 Module Xác Thực

| Endpoint | Mô tả |
| --- | --- |
| `/login` | Trang đăng nhập và xử lý đăng nhập |
| `/login-google` | Luồng đăng nhập Google OAuth |
| `/logout` | Đăng xuất |
| `/api/check-email`, `/api/signup/check-email`, `/api/login/check-email` | Kiểm tra email |
| `/api/signup/send-code` | Gửi mã xác thực đăng ký |
| `/api/signup/verify-code` | Xác thực mã đăng ký |
| `/api/signup`, `/signup` | Luồng đăng ký |

### 5.2 Module Sản Phẩm Và Catalogue

| Endpoint | Mô tả |
| --- | --- |
| `/home` | Trang chủ |
| `/category` | Trang danh mục / loại sản phẩm |
| `/product` | Trang chi tiết sản phẩm |
| `/search` | Tìm kiếm sản phẩm |
| `/search-suggestions` | Gợi ý tìm kiếm |
| `/reviews` | Đánh giá sản phẩm |
| `/header` | Dữ liệu danh mục cho header |

### 5.3 Module Giỏ Hàng Và Đơn Hàng

| Endpoint | Mô tả |
| --- | --- |
| `/cart` | Trang giỏ hàng |
| `/cart/add` | Thêm sản phẩm vào giỏ |
| `/cart/remove` | Xóa sản phẩm khỏi giỏ |
| `/checkout` | Thanh toán |
| `/order-success` | Trang đặt hàng thành công |
| `/user/orders` | Lịch sử và chi tiết đơn hàng của người dùng |

### 5.4 Module Người Dùng

| Endpoint | Mô tả |
| --- | --- |
| `/user/profile` | Xem và cập nhật hồ sơ |
| `/user/upload-avatar` | Upload avatar |
| `/user/change-password` | Đổi mật khẩu |
| `/user/address/*` | Thêm, sửa, xóa và đặt địa chỉ mặc định |
| `/user/review` | Trang và thao tác đánh giá của người dùng |

### 5.5 Module Quản Trị

| Endpoint | Mô tả |
| --- | --- |
| `/admin/dashboard` | Dashboard thống kê |
| `/admin/products` | Quản lý sản phẩm |
| `/admin/categories` | Quản lý danh mục |
| `/admin/banner` | Quản lý banner |
| `/admin/customers` | Quản lý khách hàng |
| `/admin/orders` | Quản lý đơn hàng |
| `/admin/roles` | Quản lý vai trò và quyền |
| `/admin/settings` | Cấu hình hệ thống |
| `/admin/export-report` | Xuất báo cáo CSV |

---

## 6. Dữ Liệu Và Cấu Hình

### 6.1 File Cấu Hình

| File | Mục đích |
| --- | --- |
| `src/main/resources/example.application.properties` | Mẫu cấu hình runtime và secret |
| `src/main/resources/application.properties` | Cấu hình local, không nên chứa secret thật khi commit |
| `src/main/resources/example.hibernate.cfg.xml` | Mẫu cấu hình Hibernate XML |
| `src/main/resources/hibernate.cfg.xml` | Cấu hình Hibernate XML đang giữ trong project |
| `src/main/resources/logback.xml` | Cấu hình Logback |

### 6.2 Thuộc Tính Cần Thiết

Tạo hoặc cập nhật `src/main/resources/application.properties` từ file mẫu:

```properties
# Database
db.url=jdbc:mysql://localhost:3306/SouvenirDb?createDatabaseIfNotExist=true
db.username=root
db.password=
db.driver=com.mysql.cj.jdbc.Driver

# HikariCP
hikari.minimumIdle=2
hikari.maximumPoolSize=10
hikari.idleTimeout=30000
hikari.connectionTimeout=20000
hikari.maxLifetime=1800000
hikari.poolName=SouvenirJdbcPool
hibernate.hikari.poolName=SouvenirHibernatePool

# Cloudinary
cloud_name=
cloud_api_key=
cloud_api_secret=

# Mail
app_mail=
app_password=
```

Không commit mật khẩu, API key hoặc app password thật.

### 6.3 Ghi Chú Database

Ứng dụng sử dụng MySQL và các entity Hibernate chính:

- `User`, `UserCredential`, `OAuthAccount`, `UserSession`
- `Role`, `Permission`
- `Product`, `Category`
- `VerificationCode`
- `Address`, `Province`, `Ward`

Khi khởi động, `DbContextListener` khởi tạo Hibernate và kiểm tra role `Customer`.
Nếu role này chưa tồn tại, cần seed dữ liệu role/quyền trước khi dùng các flow đăng ký
và phân quyền.

---

## 7. Cài Đặt Và Vận Hành

### 7.1 Điều Kiện Tiên Quyết

Cần cài đặt:

- JDK 21
- MySQL Server
- Tomcat 10.1+ hoặc container tương thích Jakarta Servlet 6
- Maven hoặc Maven Wrapper có sẵn trong project

Kiểm tra Java:

```bash
java -version
```

### 7.2 Clone Và Cấu Hình

```bash
git clone <repository-url>
cd SouvenirE-commerce
cp src/main/resources/example.application.properties src/main/resources/application.properties
```

Cập nhật `src/main/resources/application.properties` theo thông tin database,
Cloudinary và email local.

### 7.3 Build

```bash
./mvnw clean package
```

File WAR được sinh ra tại:

```text
target/BACKEND-1.0-SNAPSHOT.war
```

### 7.4 Deploy

Deploy WAR lên Tomcat 10.1+:

```bash
cp target/BACKEND-1.0-SNAPSHOT.war $CATALINA_HOME/webapps/
```

Khởi động Tomcat và truy cập:

```text
http://localhost:8080/BACKEND-1.0-SNAPSHOT/home
```

Nếu đổi tên WAR thành `ROOT.war`, URL là:

```text
http://localhost:8080/home
```

### 7.5 Lệnh Thường Dùng

```bash
# Chạy test
./mvnw test

# Build WAR
./mvnw clean package

# Xóa build output
./mvnw clean
```

---

## 8. Kiểm Thử

### 8.1 Framework

Project dùng JUnit Jupiter. Test hiện nằm trong:

```text
src/test/java
```

Chạy test:

```bash
./mvnw test
```

### 8.2 Phạm Vi Test Nên Bổ Sung

- Test authentication cho signup, verify code, login và hash mật khẩu.
- Test authorization cho role và permission.
- Test product service cho search, category filter và chi tiết sản phẩm.
- Test cart cho add, remove, update số lượng và kiểm tra tồn kho.
- Test order cho checkout, lưu order item và chuyển trạng thái.
- Test tích hợp controller cho các route quan trọng của customer và admin.

---

## 9. Quy Ước Phát Triển

### 9.1 Quy Ước Package

Code mới nên đặt theo cấu trúc feature-based:

```text
features/<module>/controller
features/<module>/service
features/<module>/dto
features/<module>/repository
```

Code dùng chung đặt tại:

```text
common/
core/
model/entity/
```

Code DAO/JDBC cũ giữ trong `legacy/` cho đến khi migrate.

### 9.2 Quy Ước Transaction

Ưu tiên dùng Hibernate `getCurrentSession()` với `TransactionFilter` theo từng
request. Tránh tự mở session, tự commit hoặc rollback trong repository nếu không có
lý do migration rõ ràng.

### 9.3 Quy Ước Commit Message

Dùng Conventional Commit:

```text
feat(auth): add email verification flow
fix(cart): correct quantity validation
refactor(product): migrate product DAO to repository
docs(readme): update installation guide
test(order): add checkout service tests
chore(deps): update maven dependencies
```

### 9.4 Quy Ước Bảo Mật

- Không commit credential production.
- Hash mật khẩu bằng BCrypt.
- Validate input tại controller/service boundary.
- Bảo vệ route `/admin/*` và `/user/*` bằng authentication/authorization filter.
- Thống nhất tên role và permission theo mô hình RBAC.

---

## 10. Giới Hạn Hiện Tại

Codebase hiện còn một số điểm cần hoàn thiện:

- DAO/JDBC legacy và repository Hibernate mới đang tồn tại song song.
- Route-level authorization cần được rà soát và enforce đầy đủ.
- Guest cart và merge cart sau khi login chưa được chuẩn hóa hoàn toàn.
- Luồng quên mật khẩu / reset mật khẩu chưa hoàn thiện end-to-end.
- Thanh toán mới ở mức cơ bản, chưa tích hợp payment gateway đầy đủ.
- Cần bổ sung test tự động cho các luồng nghiệp vụ quan trọng.

---

## 11. Cấu Trúc Dự Án

```text
SouvenirE-commerce/
├── docs/                         Tài liệu kỹ thuật và ghi chú module
├── logs/                         Runtime logs
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── nlu/fit/web/souvenirecommerce/
│   │   │       ├── common/
│   │   │       ├── core/
│   │   │       ├── features/
│   │   │       ├── legacy/
│   │   │       └── model/
│   │   ├── resources/
│   │   │   ├── application.properties
│   │   │   ├── example.application.properties
│   │   │   ├── hibernate.cfg.xml
│   │   │   └── logback.xml
│   │   └── webapp/
│   │       ├── WEB-INF/
│   │       ├── admin/
│   │       ├── assets/
│   │       ├── user/
│   │       └── *.jsp
│   └── test/
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

## 12. Tài Liệu Tham Khảo

- Cấu trúc tài liệu IEEE-style, tham chiếu cách trình bày Software Requirements
  Specification và Software Design Description.
- Jakarta Servlet/JSP technology stack.
- Hibernate ORM documentation.
- Maven WAR project conventions.
- Tài liệu nội bộ trong `docs/`, đặc biệt:
  - `docs/Architectural & Development Guidelines.md`
  - `docs/Modules Description.md`
  - `docs/UI Design Guidelines IEEE.md`
