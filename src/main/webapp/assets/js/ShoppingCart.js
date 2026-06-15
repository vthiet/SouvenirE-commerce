document.addEventListener("DOMContentLoaded", () => {
    const contextPath = document.querySelector('meta[name="context-path"]')?.content || "";
    const selectAll = document.getElementById("selectAllCartItems");
    const shopCheckbox = document.querySelector(".shop-checkbox");
    const checkoutButton = document.getElementById("checkoutButton");
    const totalPayEl = document.getElementById("cart-total-pay");
    const totalQtyEl = document.getElementById("cart-total-qty");
    const toast = document.getElementById("cartToast");
    const toastClose = toast?.querySelector("button");
    const removeAllButton = document.getElementById("removeAllCartItems");

    let toastTimer = null;

    const formatVND = (value) => `${Number(value || 0).toLocaleString("vi-VN")} đ`;

    function getCards() {
        return Array.from(document.querySelectorAll(".cart-item-card"));
    }

    function getSelectedCards() {
        return getCards().filter((card) => card.querySelector(".item-checkbox")?.checked);
    }

    function getCardQuantity(card) {
        return parseInt(card.querySelector(".qty-input")?.value, 10) || 0;
    }

    function getCardUnitPrice(card) {
        return Number(card.dataset.unitPrice) || 0;
    }

    function updateHeaderCounts() {
        const totalQty = getCards().reduce((sum, card) => sum + getCardQuantity(card), 0);

        if (totalQtyEl) {
            totalQtyEl.textContent = String(totalQty);
        }

        const allChecked = getCards().length > 0 && getCards().every((card) => card.querySelector(".item-checkbox")?.checked);
        const someChecked = getCards().some((card) => card.querySelector(".item-checkbox")?.checked);

        if (selectAll) {
            selectAll.checked = allChecked;
            selectAll.indeterminate = someChecked && !allChecked;
        }

        if (shopCheckbox) {
            shopCheckbox.checked = allChecked;
            shopCheckbox.indeterminate = someChecked && !allChecked;
        }
    }

    function updateSelectedSummary() {
        const selectedCards = getSelectedCards();
        const selectedTotal = selectedCards.reduce((sum, card) => {
            return sum + getCardUnitPrice(card) * getCardQuantity(card);
        }, 0);

        if (totalPayEl) {
            totalPayEl.textContent = formatVND(selectedTotal);
        }

        const hasSelection = selectedCards.length > 0;
        checkoutButton?.classList.toggle("checkout-btn--active", hasSelection);
        checkoutButton?.classList.toggle("checkout-btn--disabled", !hasSelection);
        checkoutButton?.setAttribute("aria-disabled", String(!hasSelection));

        if (checkoutButton) {
            const selectedIds = selectedCards
                .map((card) => card.dataset.productId)
                .filter(Boolean)
                .join(",");
            checkoutButton.href = hasSelection
                ? `${contextPath}/checkout?items=${encodeURIComponent(selectedIds)}`
                : `${contextPath}/checkout`;
        }

        updateHeaderCounts();
    }

    function updateItemSubtotal(card) {
        const subtotalEl = card.querySelector(".item-price");

        if (subtotalEl) {
            subtotalEl.textContent = formatVND(getCardUnitPrice(card) * getCardQuantity(card));
        }
    }

    function syncBackend(productId, quantity) {
        return fetch(`${contextPath}/cart/update`, {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded",
                "X-Requested-With": "XMLHttpRequest"
            },
            body: `productId=${encodeURIComponent(productId)}&quantity=${encodeURIComponent(quantity)}`
        });
    }

    function showRemoveToast() {
        if (!toast) {
            return;
        }

        toast.hidden = false;

        clearTimeout(toastTimer);
        toastTimer = setTimeout(() => {
            toast.hidden = true;
        }, 3500);
    }

    function showEmptyStateIfNeeded() {
        const currentContainer = document.getElementById("cartItemsContainer");

        if (getCards().length > 0 || !currentContainer) {
            return;
        }

        currentContainer.outerHTML = `
            <div class="cart-empty-state" id="cartEmptyState">
                <i class="fa-solid fa-cart-shopping"></i>
                <p>Giỏ hàng của bạn đang trống.</p>
                <a href="${contextPath}/home">Tiếp tục mua sắm</a>
            </div>
        `;
    }

    function removeCard(card) {
        const productId = card.dataset.productId;

        return syncBackend(productId, 0)
            .then((response) => response.ok ? response.json() : null)
            .then((data) => {
                if (data && data.success === false) {
                    alert("Không thể xóa sản phẩm. Vui lòng thử lại.");
                    return;
                }

                card.remove();
                showEmptyStateIfNeeded();
                showRemoveToast();
                updateSelectedSummary();
            })
            .catch(() => {
                alert("Lỗi kết nối server");
            });
    }

    function setAllChecked(checked) {
        getCards().forEach((card) => {
            const checkbox = card.querySelector(".item-checkbox");

            if (checkbox) {
                checkbox.checked = checked;
            }
        });

        updateSelectedSummary();
    }

    selectAll?.addEventListener("change", () => {
        setAllChecked(selectAll.checked);
    });

    shopCheckbox?.addEventListener("change", () => {
        setAllChecked(shopCheckbox.checked);
    });

    document.querySelector(".cart-shop-remove")?.addEventListener("click", () => {
        const selectedCards = getSelectedCards();

        if (selectedCards.length === 0) {
            return;
        }

        selectedCards.forEach(removeCard);
    });

    removeAllButton?.addEventListener("click", () => {
        const cards = getCards();

        if (cards.length === 0) {
            return;
        }

        if (!window.confirm("Bạn có muốn xóa tất cả sản phẩm trong giỏ hàng không?")) {
            return;
        }

        Promise.all(cards.map(removeCard)).then(() => {
            showRemoveToast();
            updateSelectedSummary();
        });
    });

    toastClose?.addEventListener("click", () => {
        toast.hidden = true;
        clearTimeout(toastTimer);
    });

    checkoutButton?.addEventListener("click", (event) => {
        if (!checkoutButton.classList.contains("checkout-btn--active")) {
            event.preventDefault();
        }
    });

    getCards().forEach((card) => {
        const productId = card.dataset.productId;
        const minusBtn = card.querySelector(".minus-btn");
        const plusBtn = card.querySelector(".plus-btn");
        const qtyInput = card.querySelector(".qty-input");
        const removeBtn = card.querySelector(".remove-item-btn");
        const itemCheckbox = card.querySelector(".item-checkbox");

        itemCheckbox?.addEventListener("change", updateSelectedSummary);

        function commitQuantity(nextQty) {
            const quantity = Math.max(1, Number(nextQty) || 1);

            qtyInput.value = String(quantity);
            updateItemSubtotal(card);
            updateSelectedSummary();
            syncBackend(productId, quantity).catch(() => {
                alert("Lỗi kết nối server");
            });
        }

        minusBtn?.addEventListener("click", () => {
            commitQuantity((parseInt(qtyInput.value, 10) || 1) - 1);
        });

        plusBtn?.addEventListener("click", () => {
            commitQuantity((parseInt(qtyInput.value, 10) || 1) + 1);
        });

        qtyInput?.addEventListener("change", () => {
            commitQuantity(qtyInput.value);
        });

        removeBtn?.addEventListener("click", () => {
            removeCard(card);
        });
    });

    updateSelectedSummary();
});
