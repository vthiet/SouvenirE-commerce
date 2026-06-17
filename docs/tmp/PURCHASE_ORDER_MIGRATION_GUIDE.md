# Hướng dẫn migrate phiếu nhập kho

## Mục tiêu

Tài liệu này hướng dẫn cách cập nhật database để chạy được các chức năng phiếu nhập kho mới:

- Phiếu nhập nhiều sản phẩm
- Lưu nháp
- Xem chi tiết từng dòng sản phẩm
- Sửa phiếu nhập
- Hủy phiếu nhập và hoàn tồn kho nếu cần

## Tóm tắt cơ chế migrate

Project hiện đã có cơ chế migrate tự động trong:

- `src/main/java/nlu/fit/web/souvenirecommerce/core/config/SchemaMigrationRunner.java`

Khi application khởi động, `DbContextListener` sẽ gọi `SchemaMigrationRunner.runBeforeHibernate()` trước khi Hibernate lên, nên:

1. Bảng `purchase_orders` và `purchase_order_items` sẽ được tạo nếu chưa có.
2. Cột `status` sẽ được thêm vào `purchase_orders` nếu DB cũ chưa có.
3. Các cột legacy như `product_id`, `product_name`, `stock_before`, `stock_after` sẽ được giữ để tương thích.
4. Dữ liệu phiếu nhập cũ kiểu "1 phiếu = 1 sản phẩm" sẽ được tách sang `purchase_order_items`.
5. Các dòng cũ chưa có trạng thái sẽ được chuẩn hóa về `FINALIZED`.

## Cách migrate khuyến nghị

### Bước 1: Backup database

Trước khi chạy migrate, hãy backup DB hiện tại.

Ví dụ:

```bash
mysqldump -u root -p SouvenirDb > backup_souvenirdb.sql
```

### Bước 2: Build lại application

```bash
./mvnw clean compile
```

Hoặc build WAR:

```bash
./mvnw clean package
```

### Bước 3: Deploy bản mới và restart Tomcat

Sau khi deploy WAR mới, chỉ cần restart Tomcat/container.

Khi app start:

- `SchemaMigrationRunner` sẽ tự chạy
- Schema purchase order sẽ được đồng bộ tự động

Đây là cách khuyến nghị cho DB đã có dữ liệu thật.

## Khi nào cần chạy SQL thủ công

Bạn chỉ cần chạy SQL thủ công nếu:

- app không khởi động được để trigger migration
- muốn tạo DB từ đầu trên một server mới
- muốn kiểm tra schema bằng tay trước khi deploy

### File SQL khởi tạo

File này chứa DDL chuẩn cho schema mới:

```text
src/main/resources/sql/purchase_orders.sql
```

Chạy trên database mới:

```bash
mysql -u root -p SouvenirDb < src/main/resources/sql/purchase_orders.sql
```

## Nếu DB đang có dữ liệu cũ

Trong trường hợp database cũ đã có bảng `purchase_orders` kiểu legacy, bạn **không nên chỉ chạy** `purchase_orders.sql`, vì file đó chỉ tạo schema mới khi bảng chưa tồn tại.

Lúc này hãy để `SchemaMigrationRunner` xử lý khi app start.

Nếu cần migrate tay, làm theo thứ tự:

1. Thêm cột `status` vào `purchase_orders`
2. Gán giá trị mặc định `FINALIZED` cho các dòng cũ
3. Đảm bảo `item_count` tồn tại
4. Tạo bảng `purchase_order_items`
5. Chuyển dữ liệu legacy `product_id`, `product_name`, `quantity`, `unit_cost` sang bảng chi tiết
6. Kiểm tra lại tồn kho và dữ liệu phiếu nhập

## SQL tham khảo khi migrate tay

> Chỉ dùng phần này nếu bạn buộc phải thao tác trực tiếp trên DB.

```sql
ALTER TABLE purchase_orders
  ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'FINALIZED';

UPDATE purchase_orders
SET status = 'FINALIZED'
WHERE status IS NULL OR TRIM(status) = '';

ALTER TABLE purchase_orders
  ADD COLUMN item_count INT NOT NULL DEFAULT 0;
```

Sau đó tạo bảng chi tiết:

```sql
CREATE TABLE IF NOT EXISTS purchase_order_items (
  id BIGINT NOT NULL AUTO_INCREMENT,
  purchase_order_id BIGINT NOT NULL,
  product_id BIGINT NOT NULL,
  product_name VARCHAR(255) NOT NULL,
  quantity INT NOT NULL,
  unit_cost DECIMAL(15,2) NOT NULL,
  line_amount DECIMAL(15,2) NOT NULL,
  stock_before INT NOT NULL,
  stock_after INT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uk_purchase_order_items_order_product (purchase_order_id, product_id),
  CONSTRAINT fk_purchase_order_items_purchase_order
    FOREIGN KEY (purchase_order_id) REFERENCES purchase_orders (id)
    ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## Kiểm tra sau migrate

Sau khi restart app, kiểm tra:

### 1. Bảng và cột

- `purchase_orders.status` phải tồn tại
- `purchase_order_items` phải tồn tại
- Các dòng cũ không bị mất

### 2. Dữ liệu phiếu nhập

Chạy query:

```sql
SELECT id, po_code, status, item_count, total_amount
FROM purchase_orders
ORDER BY id DESC;
```

### 3. Phiếu nhập nhiều sản phẩm

Tạo mới một phiếu nhập gồm 2 sản phẩm trở lên, sau đó:

- lưu nháp
- mở trang chi tiết
- sửa phiếu
- finalize
- hủy phiếu

Nếu mọi thao tác chạy đúng, migration đã sẵn sàng.

## Cách xác nhận hệ thống đã dùng đúng migration

Khi app start thành công, log sẽ đi qua luồng:

- migrate password cũ
- đảm bảo unique phone
- ép engine InnoDB
- tạo/đồng bộ purchase order schema

Nếu app vẫn vào được trang quản trị và tạo được phiếu nhập, nghĩa là schema mới đã áp dụng đúng.

## Lỗi thường gặp

### 1. App báo thiếu cột `status`

Nguyên nhân:

- DB cũ chưa được migrate
- app chưa restart sau khi update code

Khắc phục:

- restart Tomcat
- kiểm tra `SchemaMigrationRunner`
- nếu cần, chạy SQL thủ công

### 2. Lưu phiếu nhập bị lỗi vì dữ liệu cũ

Nguyên nhân:

- phiếu cũ còn kiểu 1 sản phẩm / 1 phiếu
- dữ liệu legacy chưa được tách sang `purchase_order_items`

Khắc phục:

- để migration runner chạy lại khi start
- kiểm tra log xem đã migrate legacy items chưa

### 3. Mở trang chi tiết nhưng không thấy dữ liệu dòng sản phẩm

Nguyên nhân:

- `purchase_order_items` chưa có dữ liệu
- phiếu nhập đó là dữ liệu cũ chưa migrate

Khắc phục:

- migrate lại DB
- kiểm tra query `SELECT * FROM purchase_order_items WHERE purchase_order_id = ?`

## Luồng deploy ngắn gọn

1. Backup DB
2. `./mvnw clean package`
3. Deploy WAR mới
4. Restart Tomcat
5. Mở `/admin/stock-imports`
6. Tạo thử phiếu nhập mới
7. Kiểm tra trang chi tiết `/admin/purchase-order-detail?id=...`

## File liên quan

- `src/main/java/nlu/fit/web/souvenirecommerce/core/config/SchemaMigrationRunner.java`
- `src/main/resources/sql/purchase_orders.sql`
- `src/main/java/nlu/fit/web/souvenirecommerce/features/dashboard/controller/AdminStockImportController.java`
- `src/main/java/nlu/fit/web/souvenirecommerce/features/dashboard/controller/AdminPurchaseOrderDetailController.java`
- `src/main/webapp/admin/stock-imports.jsp`
- `src/main/webapp/admin/purchase-order-detail.jsp`

