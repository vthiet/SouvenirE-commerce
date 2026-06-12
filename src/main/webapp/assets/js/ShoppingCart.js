document.addEventListener("DOMContentLoaded", () => {
    const contextPath = document.querySelector('meta[name="context-path"]')?.content || "";
    const formatVnd = (value) => `${Number(value || 0).toLocaleString("vi-VN")}₫`;

    async function updateCart(productId, quantity) {
        const response = await fetch(`${contextPath}/cart/update`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: new URLSearchParams({productId, quantity})
        });
        if (!response.ok) {
            throw new Error("Không thể kết nối đến máy chủ");
        }
        return response.json();
    }

    function updateTotals(data) {
        document.querySelectorAll("[data-cart-total]").forEach((element) => {
            element.textContent = formatVnd(data.total);
        });
        document.getElementById("cartPageQuantity")?.replaceChildren(String(data.totalQuantity));
        document.getElementById("cartSummaryQuantity")?.replaceChildren(String(data.totalQuantity));
        document.getElementById("header-cart-count")?.replaceChildren(String(data.totalQuantity));
    }

    function showEmptyCart() {
        const layout = document.getElementById("cartLayout");
        if (!layout || document.querySelector("[data-cart-item]")) {
            return;
        }
        layout.outerHTML = `
            <section class="cart-empty">
                <i class="fa-solid fa-basket-shopping" aria-hidden="true"></i>
                <h2>Giỏ hàng đang trống</h2>
                <p>Khám phá những món quà mang nét đẹp Việt Nam dành cho bạn.</p>
                <a href="${contextPath}/home" class="cart-primary-action">Khám phá sản phẩm</a>
            </section>`;
    }

    async function submitUpdate(form, removeItem) {
        const item = form.closest("[data-cart-item]");
        const input = form.querySelector('[name="quantity"]');
        const productId = form.querySelector('[name="productId"]').value;
        const oldQuantity = input.dataset.previousValue || input.value;
        const quantity = removeItem ? 0 : input.value;

        form.classList.add("is-loading");
        try {
            const data = await updateCart(productId, quantity);
            if (!data.success) {
                input.value = oldQuantity;
                window.alert(data.message || "Không thể cập nhật giỏ hàng");
                return;
            }
            if (removeItem) {
                item.remove();
                showEmptyCart();
            } else {
                input.dataset.previousValue = input.value;
                item.querySelector("[data-item-subtotal]").textContent = formatVnd(data.itemSubtotal);
            }
            updateTotals(data);
        } catch (error) {
            input.value = oldQuantity;
            window.alert(error.message);
        } finally {
            form.classList.remove("is-loading");
        }
    }

    document.querySelectorAll("[data-cart-update-form]").forEach((form) => {
        const input = form.querySelector('[name="quantity"]');
        input.dataset.previousValue = input.value;
        form.addEventListener("submit", (event) => {
            event.preventDefault();
            submitUpdate(form, false);
        });
    });

    document.querySelectorAll("[data-cart-remove-form]").forEach((form) => {
        form.addEventListener("submit", (event) => {
            event.preventDefault();
            submitUpdate(form, true);
        });
    });
});
