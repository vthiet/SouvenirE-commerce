# TÀI LIỆU HƯỚNG DẪN KIẾN TRÚC VÀ QUY CHUẨN PHÁT TRIỂN DỰ ÁN

**Dự án:** Hệ thống Thương mại Điện tử Souvenir E-Commerce

**Công nghệ áp dụng:** Java Servlet, JSP, Hibernate ORM, Lombok, Gson

**Tiêu chuẩn tài liệu:** Định dạng cấu trúc IEEE

---

## 1. TỔNG QUAN (INTRODUCTION)

### 1.1 Mục đích (Purpose)

Tài liệu này định nghĩa cấu trúc mã nguồn theo tính năng (Feature-based), cơ chế quản lý giao dịch tập trung (Transaction Management), và quy chuẩn xử lý ngoại lệ (Exception Handling) cho toàn bộ thành viên trong đội ngũ phát triển. Mục tiêu nhằm giảm thiểu xung đột mã nguồn (Code Conflict) khi làm việc nhóm và đảm bảo tính toàn vẹn dữ liệu (ACID).

### 1.2 Phạm vi hệ thống (Scope)

Áp dụng cho toàn bộ các module tính năng và phân quyền trong hệ thống bao gồm: `SUPER_ADMIN`, `ADMIN`, `STAFF`, và `CUSTOMER`.

---

## 2. KIẾN TRÚC THƯ MỤC CỐT LÕI (ARCHITECTURAL PATTERN)

Hệ thống chuyển dịch từ mô hình phân lớp truyền thống (Package-by-layer) sang mô hình **Phát triển theo Tính năng (Feature-based / Package-by-feature)**. Mỗi tính năng lớn sẽ cô lập toàn bộ Controller (Servlet), Model (Entity), DTO và Service/Repository bên trong một thư mục duy nhất.

### 2.1 Cấu trúc mã nguồn Java (`src/main/java/`)

```text
src/main/java/com/yourdomain/ecommerce/
│
├── core/                           # Các cấu hình hệ thống cốt lõi
│   ├── config/                     # Cấu hình Hibernate, AppConfig
│   │   └── HibernateUtil.java
│   ├── filters/                    # Bộ lọc Authentication & Authorization (RBAC)
│   │   └── SecurityFilter.java     # Kiểm tra quyền: SUPER_ADMIN, ADMIN, STAFF, CUSTOMER
│   └── exception/                  # Xử lý ngoại lệ toàn hệ thống
│
├── common/                         # Tiện ích và lớp dùng chung (Shared Components)
│   ├── base/
│   │   ├── BaseRepository.java     # CRUD chung bằng Hibernate
│   │   └── BaseService.java
│   └── utils/
│       ├── GsonUtil.java           # Helper cho Gson (parse JSON)
│       └── StringUtil.java
│
├── features/                       # NƠI CHỨA CÁC TÍNH NĂNG (FEATURE-BASED)
│   │
│   ├── auth/                       # Module Xác thực
│   │   ├── controller/             # LoginServlet, RegisterServlet, LogoutServlet
│   │   ├── dto/                    # LoginRequest, RegisterDTO
│   │   ├── service/
│   │   └── AuthRepository.java     # (Nếu có query riêng liên quan đến auth)
│   │
│   ├── user/                       # Quản lý tài khoản (Profiles, Phân quyền)
│   │   ├── model/                  # User.java, Role.java (Entity Hibernate mới)
│   │   ├── controller/             # UserAdminServlet, ProfileServlet
│   │   ├── dto/                    # UserResponseDTO (Dùng Gson để trả về cho Ajax)
│   │   └── service/
│   │
│   ├── product/                    # Module Sản phẩm & Danh mục
│   │   ├── model/                  # Product.java, Category.java, Brand.java
│   │   ├── controller/             # ProductDetailServlet, ProductManagementServlet
│   │   ├── dto/
│   │   └── service/
│   │
│   ├── cart/                       # Module Giỏ hàng
│   │   ├── model/                  # CartItem.java (Có thể lưu DB hoặc Session)
│   │   ├── controller/             # CartServlet (Thêm, sửa, xóa item qua Ajax)
│   │   └── service/
│   │
│   ├── order/                      # Module Đơn hàng & Thanh toán
│   │   ├── model/                  # Order.java, OrderDetail.java, PaymentMethod.java
│   │   ├── controller/             # CheckoutServlet, OrderHistoryServlet, OrderTrackingServlet
│   │   └── service/
│   │
│   └── dashboard/                  # Thống kê (Dành cho Admin/Staff/SuperAdmin)
│       ├── controller/             # ReportServlet
│       └── service/                # Tính toán doanh thu, top sản phẩm
│
└── legacy/                         # BIỆN PHÁP TẠM THỜI: Chứa code JDBC + Model cũ để tránh conflict 
    ├── model/                      # Giữ lại các class cũ chưa kịp migrate
    └── dao/                        # Các class JDBC cũ (Sẽ xóa dần khi migrate xong)

```

### 2.2 Cấu trúc thư mục giao diện (`src/main/webapp/`)

```text
src/main/webapp/
├── WEB-INF/
│   └── views/
│       ├── admin/                  # Giao diện cho SUPER_ADMIN, ADMIN, STAFF
│       │   ├── product-manage.jsp
│       │   └── order-list.jsp
│       ├── client/                 # Giao diện cho CUSTOMER và Guest
│       │   ├── home.jsp
│       │   ├── product-detail.jsp
│       │   └── cart.jsp
│       └── auth/
│           ├── login.jsp
│           └── register.jsp
├── assets/                         # CSS, JS, Images
└── index.jsp

```

---

## 3. QUY CHUẨN THIẾT KẾ TẦNG DỮ LIỆU VÀ GIAO DỊCH (DATA ACCESS & TRANSACTION POLICY)

### 3.1 Quy định đặt tên và phân rã đối tượng

* **Không sử dụng hậu tố DAO.** Tất cả các lớp giao tiếp cơ sở dữ liệu qua Hibernate phải sử dụng hậu tố **`Repository`** (Ví dụ: `ProductRepository`).
* **Tư duy theo hướng Nghiệp vụ (Domain-Driven):** Một `Repository` có thể quản lý nhiều Entity phụ thuộc tuyến tính. Ví dụ: `OrderRepository` sẽ xử lý lưu trữ cho cả `Order` và `OrderDetail` thông qua cơ chế `CascadeType.ALL` của Hibernate, không tách rời thành `OrderDetailDAO`.

### 3.2 Cơ chế Quản lý Giao dịch Tập trung: "Session-per-request"

Để giải quyết triệt để lỗi lồng Transaction hoặc rò rỉ kết nối (Connection Leak), toàn bộ dự án thống nhất quản lý vòng đời kết nối theo luồng Request của Web Server.

#### Bước 1: Yêu cầu cấu hình bắt buộc (`hibernate.cfg.xml`)

Mọi thành viên không tự ý mở Session thủ công, phải kích hoạt ngữ cảnh Thread Local:

```xml
<property name="current_session_context_class">thread</property>

```

#### Bước 2: Hiện thực hóa lớp nền tảng `AbsBaseRepository`

Các Repository cụ thể khi tạo mới chỉ cần kế thừa `AbsBaseRepository` và thực thi nghiệp vụ thô, **tuyệt đối không viết lệnh đóng/mở hay commit transaction tại đây**.

```java
public abstract class AbsBaseRepository<K, T> implements IRepository<K, T> {
    private final Class<T> entityClass;

    protected AbsBaseRepository(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    protected Session getSession() {
        return HibernateUtil.getSessionFactory().getCurrentSession();
    }

    @Override
    public Optional<T> save(T entity) {
        getSession().persist(entity);
        getSession().flush();
        return Optional.of(entity);
    }

    @Override
    public void update(T entity) {
        getSession().merge(entity);
    }

    @Override
    public Optional<T> findById(K id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(getSession().find(entityClass, id));
    }

    @Override
    public List<T> findAll() {
        String hql = "from " + entityClass.getSimpleName();
        return getSession().createQuery(hql, entityClass).getResultList();
    }

    @Override
    public void delete(K id) {
        if (id == null) return;
        T entity = getSession().find(entityClass, id);
        if (entity != null) {
            getSession().remove(entity);
        }
    }
}

```

---

## 4. QUY TRÌNH XỬ LÝ LỖI TẬP TRUNG (EXCEPTION PROPAGATION & CONTROL)

Hệ thống áp dụng kiến trúc **Ném lỗi tập trung (Exception Propagation Chain)** từ tầng dưới cùng lên bề mặt hiển thị.

### 4.1 Mô hình phân rã luồng lỗi

1. **Tầng Repository / Service:** Không sử dụng khối `try-catch` bọc ngoài trừ khi cần bổ sung thông tin nghiệp vụ. Nếu phát hiện vi phạm logic (ví dụ: hết hàng), chủ động ném các ngoại lệ Runtime chuyên biệt (`Custom Business Exception`).
2. **Tầng Servlet:** Thực hiện `try-catch` tập trung để điều hướng luồng giao diện (Forward về trang JSP hoặc dùng Gson để kết xuất JSON về cho Ajax).
3. **Tầng Filter (`TransactionFilter`):** Nằm ngoài cùng, có nhiệm vụ tối cao là bắt lỗi cuối cùng để quyết định `Commit` hay `Rollback` dữ liệu.

### 4.2 Triển khai mã nguồn chuẩn cho bộ lọc `TransactionFilter`

```java
package nlu.fit.web.souvenirecommerce.core.filters;

import jakarta.servlet.*;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TransactionFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(TransactionFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        Session session = HibernateUtil.getSessionFactory().getCurrentSession();
        Transaction transaction = null;

        try {
            // Khởi tạo một giao dịch duy nhất cho toàn bộ chu kỳ Request
            transaction = session.beginTransaction();

            // Chuyển tiếp Request qua tầng Servlet và Service
            chain.doFilter(request, response);

            // Nếu không có bất kỳ ngoại lệ nào phát sinh, tiến hành commit dữ liệu xuống DB
            if (transaction != null && transaction.isActive()) {
                transaction.commit();
            }
        } catch (Exception e) {
            // Nếu phát sinh bất kỳ lỗi nào từ bất kỳ tầng nào, thực hiện hủy bỏ toàn bộ giao dịch
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
            log.error("Transaction successfully rolled back due to error: ", e);
            throw new ServletException(e);
        }
    }
}

```

### 4.3 Quy tắc bắt buộc tại tầng Servlet (Crucial Rule for Developers)

Khi thành viên viết mã nguồn tại các Servlet, nếu có thực hiện `try-catch` để xử lý giao diện hiển thị, **bắt buộc phải ném tiếp (rethrow) ngoại lệ ra ngoài** ở cuối khối `catch`. Nếu nuốt lỗi (Swallow Exception), `TransactionFilter` sẽ hiểu sai là request thành công và tự động commit dữ liệu lỗi vào DB.

```java
try {
    orderService.processCheckout(order, items);
    response.getWriter().write(new Gson().toJson(new ResponseDTO("SUCCESS")));
} catch (BusinessException e) {
    // 1. Xử lý hiển thị giao diện tại Servlet
    request.setAttribute("errorMsg", e.getMessage());
    request.getRequestDispatcher("/WEB-INF/views/client/cart.jsp").forward(request, response);
    
    // 2. BẮT BUỘC: Ném tiếp lỗi ra ngoài để Filter kích hoạt Rollback dữ liệu
    throw new RuntimeException("Trigger filter rollback: " + e.getMessage(), e);
}

```

### 4.4 Cơ chế định danh vết lỗi đa luồng (Multi-thread Trace Logging với MDC)

Trong môi trường E-commerce có nhiều người dùng truy cập đồng thời, các dòng log xuất ra file sẽ bị đan xen nhau. Hệ thống quy định sử dụng giải pháp **MDC (Mapped Diagnostic Context)** để tự động "gắn thẻ" (Tag) mã định danh request (`requestId`) và thông tin tài khoản (`user`) vào mọi dòng log được sinh ra trong cùng một chu kỳ Request.

#### Bước 1: Triển khai mã nguồn `LoggingFilter` tại tầng `core/filters/`

Bộ lọc này sẽ chạy trước `TransactionFilter` để thiết lập ngữ cảnh Log ngay khi request vừa chạm vào hệ thống.

```java
package nlu.fit.web.souvenirecommerce.core.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.slf4j.MDC;
import java.io.IOException;
import java.util.UUID;

public class LoggingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) 
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpSession session = httpRequest.getSession(false);
        
        // 1. Sinh mã định danh Request ID duy nhất (Độ dài 8 ký tự để tối ưu không gian hiển thị)
        String requestId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("requestId", requestId);
        
        // 2. Kiểm tra trạng thái đăng nhập để gắn định danh đối tượng vào Log
        if (session != null && session.getAttribute("user") != null) {
            // Giả sử đối tượng User lưu trong session có hàm getUsername()
            var userObj = session.getAttribute("user");
            MDC.put("user", userObj.toString()); 
        } else {
            MDC.put("user", "Guest"); // Khách vãng lai chưa đăng nhập
        }

        try {
            // Chuyển tiếp sang TransactionFilter và các tầng xử lý bên trong
            chain.doFilter(request, response);
        } finally {
            // 3. QUAN TRỌNG: Bắt buộc phải xóa sạch cấu hình MDC khi kết thúc Request
            // Nhằm tránh rò rỉ bộ nhớ (Memory Leak) do cơ chế tái sử dụng Thread (Thread Pool) của Tomcat
            MDC.clear();
        }
    }
}

```

#### Bước 2: Thứ tự cấu hình các Bộ lọc (Filter Chain Mapping) trong `web.xml`

Để hệ thống vận hành đúng logic, thứ tự bọc của các Filter phải tuân thủ nghiêm ngặt quy trình: **Log phải được thiết lập trước khi Transaction bắt đầu.**

Các thành viên cấu hình file `web.xml` theo đúng thứ tự khai báo từ trên xuống dưới như sau:

```xml
<filter>
    <filter-name>LoggingFilter</filter-name>
    <filter-class>nlu.fit.web.souvenirecommerce.core.filters.LoggingFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>LoggingFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

<filter>
    <filter-name>TransactionFilter</filter-name>
    <filter-class>nlu.fit.web.souvenirecommerce.core.filters.TransactionFilter</filter-class>
</filter>
<filter-mapping>
    <filter-name>TransactionFilter</filter-name>
    <url-pattern>/*</url-pattern>
</filter-mapping>

```

#### Bước 3: Quy chuẩn cấu hình File hiển thị Log (`logback.xml` hoặc `log4j.properties`)

Mọi thành viên khi cấu hình công cụ ghi Log (Appender Pattern) tại máy cục bộ hoặc môi trường triển khai phải bổ sung hai cú pháp `%X{requestId}` và `%X{user}` vào định dạng đầu ra:

```xml
<encoder>
    <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - [%X{requestId}] [%X{user}] - %msg%n</pattern>
</encoder>

```

---

## 5. KẾT QUẢ ĐẦU RA ĐẠT CHUẨN KHI ĐỘI NGŨ VẬN HÀNH

Sau khi tích hợp `LoggingFilter`, khi các thành viên viết mã nguồn gọi lệnh thông thường như: `log.info("Xử lý đơn hàng thành công");` tại bất kỳ đâu (Service, Repository, Servlet), hệ thống sẽ tự động xuất ra file log có cấu trúc như sau:

```text
2026-05-31 01:15:10.123 [Tomcat-exec-4] INFO  ProductService - [a1b2c3d4] [Guest] - Request to fetch all entities
2026-05-31 01:15:11.456 [Tomcat-exec-2] INFO  OrderService   - [f7e6d5c4] [thietvv] - Request to save entity: Order
2026-05-31 01:15:12.001 [Tomcat-exec-2] ERROR TransactionFilter - [f7e6d5c4] [thietvv] - Transaction successfully rolled back due to error: Sản phẩm ID 5 đã hết hàng!

```

* **Quy trình Trace lỗi khi Debug:** Khi hệ thống gặp sự cố, thành viên chỉ cần copy mã Request ID (Ví dụ: `f7e6d5c4`) và tiến hành lệnh `Filter` trong file log. Toàn bộ hành trình từ lúc User `thietvv` nhấn nút, đi qua những hàm nào, và lỗi bùng phát tại đâu sẽ hiện ra tường minh, loại bỏ hoàn toàn việc đoán lỗi thủ công.

---

## 6. HƯỚNG DẪN DI TRÚ CUỐN CHIẾU (MIGRATION ROADMAP FOR TEAM)

Để việc chuyển đổi từ mã nguồn JDBC cũ sang kiến trúc mới này không làm gián đoạn tiến độ và tránh xung đột khi merge code:

1. **Bước 1:** Đưa toàn bộ file mã nguồn cũ chưa chỉnh sửa vào package `legacy`. Chỉnh sửa lại các đường dẫn `import` trên các Servlet cũ hướng vào package này để hệ thống tiếp tục chạy ổn định.
2. **Bước 2:** Thành viên được phân công module nào (ví dụ: `product`) sẽ tạo thư mục tính năng tương ứng trong `features/product/`. Tiến hành định nghĩa các Entity mới bằng các Annotation của Hibernate và Lombok.
3. **Bước 3:** Tạo `ProductRepository` kế thừa từ `AbsBaseRepository`, thay thế hoàn toàn các hàm viết bằng câu lệnh SQL thô.
4. **Bước 4:** Cập nhật lại Servlet để gọi Service mới. Khi module mới đã chạy kiểm thử thành công, tiến hành xóa bỏ vĩnh viễn file DAO tương ứng trong thư mục `legacy`.