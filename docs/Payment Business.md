Mình thấy bạn đang đi đúng hướng khi xây dựng e-commerce. Có một tư duy quan trọng mà nhiều dự án nhỏ thường bỏ qua:

> **Một đơn hàng không phải là một giao dịch thanh toán, cũng không phải là một lần vận chuyển.**
>
> Order chỉ là "cam kết mua bán" giữa khách hàng và cửa hàng.

Đây là tư duy của hầu hết các hệ thống thương mại điện tử lớn.

---

# 1. E-commerce là một tập hợp các business process

Nhiều người mới thiết kế theo kiểu:

```
Order
- status
```

và cố gắng nhét tất cả trạng thái vào đây:

```
Pending
Paid
Packing
Shipping
Delivering
Delivered
Completed
Refund
Returned
Cancelled
...
```

Theo thời gian sẽ phát sinh:

* Thanh toán thất bại.
* Thanh toán lại.
* Đổi cổng thanh toán.
* Giao hàng thất bại.
* Giao lại.
* Một đơn nhiều kiện hàng.
* Hoàn tiền.
* Trả hàng.

Lúc này một `status` duy nhất không thể biểu diễn toàn bộ hệ thống.

---

# 2. Bản chất của Order

Order chỉ trả lời một câu hỏi:

> Khách hàng đang mua cái gì?

Order không nên quản lý:

* VNPAY.
* GHN.
* Ví điện tử.
* Shipper.
* Ngân hàng.

Nó chỉ quản lý nghiệp vụ bán hàng.

Ví dụ:

* Đơn mới.
* Đã xác nhận.
* Đang chuẩn bị.
* Đang xử lý.
* Hoàn thành.
* Đã hủy.

Order là trung tâm của hệ thống.

---

# 3. Payment là một business process độc lập

Payment trả lời:

> Tiền đã được thanh toán như thế nào?

Các vấn đề Payment phải xử lý:

## Một Order có thể có nhiều Payment.

Ví dụ:

Thanh toán lần 1:

```
Failed
```

Thanh toán lần 2:

```
Success
```

---

Có thể:

```
Thanh toán online.

+

Thanh toán COD phần còn lại.
```

---

Có thể:

```
Thanh toán.

↓

Hoàn tiền.

↓

Hoàn tiền một phần.
```

---

Payment không nên phụ thuộc Order.

Order cũng không nên chứa logic của Payment.

---

# 4. Shipment là một business process độc lập

Shipment trả lời:

> Hàng hóa đang ở đâu?

Nó không quan tâm:

* Khách mua gì.
* Thanh toán chưa.

Nó chỉ quan tâm:

* Đã tạo vận đơn chưa.
* Shipper lấy hàng chưa.
* Đang giao.
* Giao thành công.
* Trả hàng.

---

Một Order có thể có:

```
1 Order

↓

3 Shipment
```

Ví dụ:

Laptop.

Chuột.

Bàn phím.

Kho khác nhau.

Shipper khác nhau.

---

# 5. Bounded Context

Đây là ý tưởng quan trọng trong Domain-Driven Design.

Mỗi domain có trách nhiệm riêng.

## Order Context

Quản lý:

* sản phẩm
* giá
* khách hàng
* trạng thái mua bán

---

## Payment Context

Quản lý:

* giao dịch
* cổng thanh toán
* webhook
* hoàn tiền

---

## Shipping Context

Quản lý:

* vận đơn
* tracking
* carrier
* giao hàng

---

## Inventory Context

Quản lý:

* tồn kho
* reserve
* release
* restock

---

## Promotion Context

Quản lý:

* voucher
* coupon
* campaign

---

Mỗi context có business rule riêng.

---

# 6. State Machine

Một hệ thống e-commerce thực chất là nhiều state machine chạy song song.

## Order

```
New

↓

Confirmed

↓

Processing

↓

Completed
```

---

## Payment

```
Pending

↓

Success

↓

Refund
```

---

## Shipment

```
Ready

↓

Transit

↓

Delivered
```

---

Chúng tương tác nhưng không đồng nhất.

---

# 7. Event Driven Thinking

Một hệ thống tốt thường suy nghĩ bằng event.

Ví dụ:

```
Payment Success
```

là một event.

Nó có thể kích hoạt:

```
Order Updated

Inventory Reserved

Notification Sent

Shipping Enabled

Reward Point Added
```

---

Tương tự:

```
Shipment Delivered
```

có thể kích hoạt:

```
Order Completed

COD Settlement

Review Invitation

Loyalty Point

Commission Calculation
```

---

Các module không gọi nhau trực tiếp quá nhiều.

Chúng phản ứng với sự kiện.

---

# 8. External Actor

Các hệ thống bên ngoài nên được coi là actor.

Ví dụ:

## Payment

* VNPAY
* MoMo
* Stripe

---

## Shipping

* GHN
* GHTK
* J&T

---

## Notification

* Email
* SMS
* Push Notification

---

## Authentication

* Firebase
* Google
* Facebook

---

Hệ thống không nên phụ thuộc vào vendor cụ thể.

Nó chỉ biết:

```
Payment Provider

Shipping Provider

Notification Provider
```

---

# 9. Adapter Pattern

External actor luôn thay đổi.

Ví dụ:

GHN:

```
picked
```

GHTK:

```
taking
```

J&T:

```
pickup_success
```

Trong hệ thống:

```
PICKED_UP
```

Adapter có nhiệm vụ chuyển đổi.

Điều này tránh việc business logic phụ thuộc API bên ngoài.

---

# 10. Webhook là nguồn sự thật từ bên ngoài

Có hai kiểu giao tiếp:

## Chủ động

```
Create Payment

Create Shipment
```

---

## Bị động

```
Payment Callback

Shipping Callback
```

Hệ thống phải chấp nhận rằng:

> Sau khi gửi request, trạng thái cuối cùng sẽ được xác nhận bằng webhook.

---

# 11. Idempotency

Khái niệm này cực kỳ quan trọng.

Webhook có thể gửi:

```
Success

Success

Success

Success
```

4 lần.

Kết quả vẫn phải là:

```
Payment Success
```

chỉ một lần.

---

Tương tự:

```
Delivered

Delivered

Delivered
```

không được cộng điểm thưởng 3 lần.

---

# 12. Eventual Consistency

Trong e-commerce không phải mọi thứ đều cập nhật ngay lập tức.

Ví dụ:

```
Payment Success

↓

Inventory Update

↓

Shipment Create

↓

Email Send
```

Có thể mất vài giây.

Đó là điều bình thường.

Hệ thống chỉ cần đảm bảo cuối cùng dữ liệu nhất quán.

---

# 13. Compensation

Nếu một bước thất bại thì phải có cơ chế bù trừ.

Ví dụ:

```
Reserve Inventory

↓

Payment Failed
```

thì:

```
Release Inventory
```

---

Hoặc:

```
Payment Success

↓

Shipment Create Failed
```

thì:

```
Retry

hoặc

Refund
```

---

# 14. Audit Trail

Không nên chỉ lưu trạng thái hiện tại.

Ví dụ:

```
Order

Current Status:
Completed
```

là chưa đủ.

Nên biết:

```
Created

Confirmed

Paid

Packed

Shipped

Delivered

Completed
```

kèm thời gian.

Điều này rất hữu ích cho:

* CSKH.
* Khiếu nại.
* Báo cáo.
* Debug.

---

# 15. Soft Delete

Trong e-commerce rất ít khi xóa dữ liệu.

Thay vì:

```
DELETE
```

thường là:

```
Cancelled

Inactive

Archived
```

vì dữ liệu giao dịch có giá trị pháp lý và nghiệp vụ.

---

# 16. Saga Thinking

Một đơn hàng là chuỗi nhiều hành động:

```
Create Order

↓

Reserve Inventory

↓

Create Payment

↓

Confirm Payment

↓

Prepare Goods

↓

Create Shipment

↓

Deliver

↓

Complete
```

Nếu một bước lỗi sẽ có hành động bù trừ.

Đây là ý tưởng của mô hình Saga trong hệ phân tán.

---

# 17. Aggregate Root

Trong tư duy DDD:

```
Order
```

là Aggregate Root.

Nó quản lý:

```
OrderItem

Address Snapshot

Discount Snapshot
```

Nhưng:

```
Payment

Shipment

Inventory
```

thường là aggregate khác.

Chúng liên kết bằng ID và event, không giữ quan hệ object phức tạp.

---

# 18. Snapshot

Một sai lầm phổ biến:

```
Order

-> Product
```

rồi lấy tên và giá từ Product hiện tại.

Nếu shop đổi giá:

```
100k

↓

150k
```

thì đơn hàng cũ bị sai.

Vì vậy Order nên lưu snapshot:

* tên sản phẩm lúc mua;
* giá lúc mua;
* địa chỉ giao hàng lúc đặt;
* phí ship;
* voucher đã áp dụng.

Order là "bức ảnh" của giao dịch tại thời điểm phát sinh.

---

# 19. Open/Closed Principle trong e-commerce

Một kiến trúc tốt cho phép mở rộng mà ít sửa code cũ.

Ví dụ thêm:

* cổng thanh toán mới;
* đơn vị vận chuyển mới;
* chương trình khuyến mãi mới;
* chương trình tích điểm mới;

chỉ cần thêm implementation mới thay vì sửa logic của Order.

---

# Tư tưởng cốt lõi

Nếu phải tóm tắt toàn bộ lý thuyết thiết kế một hệ thống thương mại điện tử, mình sẽ gói gọn trong 5 nguyên tắc:

1. **Phân tách theo nghiệp vụ (Business Capability)**: Order, Payment, Shipment, Inventory, Promotion... là các domain độc lập.

2. **State độc lập**: Đừng cố dùng một `status` duy nhất cho cả hệ thống; mỗi domain có vòng đời riêng.

3. **Event-driven**: Các module giao tiếp chủ yếu thông qua các sự kiện nghiệp vụ thay vì phụ thuộc trực tiếp.

4. **External systems là actor**: VNPAY, GHN, Firebase... là đối tác bên ngoài, cần được bao bọc bởi adapter để tránh phụ thuộc chặt.

5. **Order là trung tâm nhưng không làm tất cả**: Order đại diện cho giao dịch mua bán, còn Payment quản lý dòng tiền và Shipment quản lý dòng hàng hóa.
