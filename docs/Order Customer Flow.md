# 1. Luồng tổng quát của một đơn hàng

```mermaid
stateDiagram-v2
    [*] --> CREATED

    CREATED --> PAYMENT_PENDING: VNPAY
    CREATED --> PROCESSING: COD

    PAYMENT_PENDING --> PAYMENT_SUCCESS
    PAYMENT_PENDING --> PAYMENT_FAILED
    PAYMENT_PENDING --> PAYMENT_EXPIRED
    PAYMENT_PENDING --> CANCELLED

    PAYMENT_SUCCESS --> PROCESSING

    PAYMENT_FAILED --> CANCELLED
    PAYMENT_EXPIRED --> CANCELLED

    PROCESSING --> WAIT_PICKUP

    WAIT_PICKUP --> PICKING

    PICKING --> SHIPPING

    SHIPPING --> DELIVERED

    DELIVERED --> COMPLETED

    SHIPPING --> DELIVERY_FAILED

    DELIVERY_FAILED --> SHIPPING

    DELIVERY_FAILED --> RETURNED

    RETURNED --> CANCELLED

    COMPLETED --> [*]
    CANCELLED --> [*]
```

---

# 2. Tách riêng Payment

Đây là phần quan trọng với VNPAY.

```mermaid
stateDiagram-v2

    [*] --> PENDING

    PENDING --> SUCCESS
    PENDING --> FAILED
    PENDING --> EXPIRED
    PENDING --> CANCELLED

    SUCCESS --> REFUNDED

    FAILED --> [*]
    EXPIRED --> [*]
    CANCELLED --> [*]
    REFUNDED --> [*]
```

Ví dụ:

```
10:00

Create Payment

↓

PENDING

↓

Redirect VNPAY

↓

IPN Callback

↓

SUCCESS
```

Hoặc

```
10:15

Scheduler

↓

EXPIRED
```

---

# 3. Shipment

GHN có nhiều trạng thái nhưng ta map về trạng thái nội bộ.

```mermaid
stateDiagram-v2

    [*] --> NOT_CREATED

    NOT_CREATED --> WAIT_PICKUP

    WAIT_PICKUP --> PICKING

    PICKING --> SHIPPING

    SHIPPING --> DELIVERED

    SHIPPING --> DELIVERY_FAILED

    DELIVERY_FAILED --> SHIPPING

    DELIVERY_FAILED --> RETURNED

    DELIVERED --> [*]

    RETURNED --> [*]
```

---

# 4. Các service tương tác

```mermaid
flowchart TD

A[User Checkout]

A-->B[Create Order]

B-->C[Create Payment]

C-->D{Payment Method}

D-->|COD|E[Order Processing]

D-->|VNPAY|F[Redirect VNPAY]

F-->G[IPN Callback]

G-->H{Success?}

H-->|Yes|E

H-->|No|I[Cancel Order]

E-->J[Create GHN Order]

J-->K[Wait Pickup]

K-->L[Shipping]

L-->M[Delivered]

M-->N[Completed]
```

---

# 5. Scheduler

Cái này thường bị bỏ sót.

```mermaid
flowchart LR

A[Scheduler mỗi phút]

A-->B[Payment Pending]

B-->C{Expired?}

C-->|No|D[Nothing]

C-->|Yes|E[Payment Expired]

E-->F[Cancel Order]

F-->G[Restore Inventory]
```

---

# 6. Webhook

Cả GHN và VNPAY đều nên đi qua webhook.

```mermaid
flowchart TD

VNPAY-->Webhook

GHN-->Webhook

Webhook-->PaymentService

Webhook-->ShipmentService

PaymentService-->OrderService

ShipmentService-->OrderService

OrderService-->OrderHistory
```

---

# 7. Order History

```mermaid
flowchart LR

A[Created]

A-->B[Payment Pending]

B-->C[Payment Success]

C-->D[Wait Pickup]

D-->E[Picking]

E-->F[Shipping]

F-->G[Delivered]

G-->H[Completed]
```

Nếu timeout:

```mermaid
flowchart LR

A[Created]

A-->B[Payment Pending]

B-->C[Payment Expired]

C-->D[Cancelled]
```

---

# 8. ERD đơn giản

```mermaid
erDiagram

USER ||--o{ ORDER : places

ORDER ||--|| PAYMENT : has

ORDER ||--|| SHIPMENT : has

ORDER ||--o{ ORDER_HISTORY : logs

USER {
    bigint id
    string name
}

ORDER {
    bigint id
    bigint user_id
    string order_status
    decimal total
    datetime created_at
}

PAYMENT {
    bigint id
    bigint order_id
    string method
    string status
    string transaction_id
    datetime expired_at
    datetime paid_at
}

SHIPMENT {
    bigint id
    bigint order_id
    string ghn_code
    string status
    string tracking_code
}

ORDER_HISTORY {
    bigint id
    bigint order_id
    string status
    string description
    datetime created_at
}
```

---

# 9. Sơ đồ mình khuyên dùng cho dự án của bạn

Đây là sơ đồ tổng hợp toàn bộ nghiệp vụ:

```mermaid
flowchart TD

U[User Checkout]

U-->O[Create Order]

O-->P[Create Payment]

P-->PM{Payment}

PM-->|COD|PR[Processing]

PM-->|VNPAY|VP[VNPAY]

VP-->IPN[IPN Callback]

IPN-->CHK{Success}

CHK-->|Yes|PR

CHK-->|No|CAN[Cancel]

SCH[Scheduler]

SCH-->EXP{Expired}

EXP-->|Yes|CAN

PR-->GHN[Create GHN Order]

GHN-->WP[Wait Pickup]

WP-->PK[Picking]

PK-->SH[Shipping]

SH-->DEL[Delivered]

DEL-->COM[Completed]

SH-->FAIL[Delivery Failed]

FAIL-->RET[Returned]

RET-->CAN

O-->HIS[Order History]

P-->HIS

GHN-->HIS

CAN-->HIS

COM-->HIS
```

## Tuy nhiên, sau khi làm khá nhiều hệ thống dạng này, mình còn bổ sung một nguyên tắc thiết kế quan trọng:

**`Order` không nên "điều khiển" Payment và Shipment.**

Thay vào đó:

* `Payment` phát sinh sự kiện **PaymentSucceeded**.
* `OrderService` nhận sự kiện đó và quyết định tạo đơn GHN.
* `GHN Webhook` phát sinh **ShipmentDelivered**.
* `OrderService` nhận sự kiện và cập nhật Order thành Completed.
* Mọi thay đổi đều ghi vào `OrderHistory`.

Đây là kiến trúc gần với cách các hệ thống e-commerce thực tế vận hành và sẽ giúp bạn dễ mở rộng thêm MoMo, ZaloPay, GHTK, Viettel Post hoặc xử lý hoàn tiền mà không phải sửa quá nhiều logic cũ.
