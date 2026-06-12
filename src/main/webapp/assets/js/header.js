document.addEventListener("DOMContentLoaded", () => {
    const menuButton = document.getElementById("headerMenuButton");
    const categoryDropdown = document.getElementById("headerCategoryDropdown");
    const overlay = document.getElementById("headerOverlay");
    const contextPath = document.querySelector("meta[name='context-path']")?.getAttribute("content") || "";

    const userToggle = document.getElementById("userToggle");
    const userDropdown = document.getElementById("userDropdown");
    const cartLink = document.querySelector(".header-cart[data-cart-toggle='true']");
    const cartLoginPopover = document.getElementById("cartLoginPopover");
    const cartPreviewContent = document.getElementById("cartPreviewContent");
    const searchForm = document.getElementById("headerSearchForm");
    const searchInput = document.getElementById("headerSearchInput");
    const searchClear = document.getElementById("headerSearchClear");
    const searchDropdown = document.getElementById("headerSearchDropdown");
    const searchList = document.getElementById("headerSearchList");
    const searchClearAll = document.getElementById("headerSearchClearAll");
    const recentSearchKey = "inolaRecentSearches";
    let searchTimer = null;

    function formatCurrency(value) {
        return new Intl.NumberFormat("vi-VN").format(Number(value) || 0) + " đ";
    }

    function escapeHtml(value) {
        return String(value ?? "")
            .replace(/&/g, "&amp;")
            .replace(/</g, "&lt;")
            .replace(/>/g, "&gt;")
            .replace(/"/g, "&quot;")
            .replace(/'/g, "&#039;");
    }

    function getRecentSearches() {
        try {
            const value = JSON.parse(localStorage.getItem(recentSearchKey) || "[]");
            return Array.isArray(value) ? value.filter(Boolean).slice(0, 6) : [];
        } catch (error) {
            return [];
        }
    }

    function saveRecentSearch(keyword) {
        const normalized = String(keyword || "").trim();
        if (!normalized) {
            return;
        }

        const lower = normalized.toLowerCase();
        const next = [
            normalized,
            ...getRecentSearches().filter((item) => item.toLowerCase() !== lower)
        ].slice(0, 6);
        localStorage.setItem(recentSearchKey, JSON.stringify(next));
    }

    function removeRecentSearch(keyword) {
        const lower = String(keyword || "").toLowerCase();
        const next = getRecentSearches().filter((item) => item.toLowerCase() !== lower);
        localStorage.setItem(recentSearchKey, JSON.stringify(next));
        renderSearchDropdown(searchInput?.value || "");
    }

    function openSearchDropdown() {
        if (!searchDropdown) {
            return;
        }
        searchDropdown.hidden = false;
        searchDropdown.setAttribute("aria-hidden", "false");
    }

    function closeSearchDropdown() {
        if (!searchDropdown) {
            return;
        }
        searchDropdown.hidden = true;
        searchDropdown.setAttribute("aria-hidden", "true");
    }

    function submitSearch(keyword) {
        const normalized = String(keyword || "").trim();
        if (!normalized || !searchForm || !searchInput) {
            return;
        }
        searchInput.value = normalized;
        saveRecentSearch(normalized);
        searchForm.submit();
    }

    function renderRecentSearches() {
        if (!searchList) {
            return;
        }

        const recent = getRecentSearches();
        if (recent.length === 0) {
            searchList.innerHTML = `
                <div class="header-search-empty">
                    <div class="header-search-empty-art">
                        <i class="fa-solid fa-magnifying-glass"></i>
                        <i class="fa-solid fa-pencil"></i>
                    </div>
                    <p>Không có từ khóa tìm kiếm gần đây</p>
                </div>
            `;
            return;
        }

        searchList.innerHTML = recent.map((item) => `
            <div class="header-search-row" data-recent-search="${escapeHtml(item)}">
                <i class="fa-solid fa-magnifying-glass"></i>
                <span>${escapeHtml(item)}</span>
                <button type="button" data-remove-search="${escapeHtml(item)}" aria-label="Xóa ${escapeHtml(item)}">
                    <i class="fa-solid fa-xmark"></i>
                </button>
            </div>
        `).join("");
    }

    function renderSuggestions(items, keyword) {
        if (!searchList) {
            return;
        }

        const suggestions = Array.isArray(items) ? items : [];
        if (suggestions.length === 0) {
            searchList.innerHTML = `<div class="header-search-empty">Không có gợi ý phù hợp</div>`;
            return;
        }

        searchList.innerHTML = suggestions.map((item) => {
            const name = escapeHtml(item.name || keyword);
            return `
                <div class="header-search-row" data-suggestion-search="${name}">
                    <i class="fa-solid fa-magnifying-glass"></i>
                    <strong>${name}</strong>
                    <span class="search-row-meta">Search</span>
                </div>
            `;
        }).join("");
    }

    function fetchSearchSuggestions(keyword) {
        fetch(`${contextPath}/search-suggestions?q=${encodeURIComponent(keyword)}`, {
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
            .then((response) => response.ok ? response.json() : [])
            .then((items) => {
                if ((searchInput?.value || "").trim() === keyword) {
                    renderSuggestions(items, keyword);
                }
            })
            .catch(() => renderSuggestions([], keyword));
    }

    function renderSearchDropdown(value) {
        const keyword = String(value || "").trim();
        if (searchClear) {
            searchClear.hidden = keyword.length === 0;
        }

        openSearchDropdown();

        clearTimeout(searchTimer);
        if (!keyword) {
            renderRecentSearches();
            return;
        }

        searchTimer = setTimeout(() => fetchSearchSuggestions(keyword), 180);
    }

    function renderCartPreview(data, shouldOpen = false) {
        const cartCount = Number(data?.cartCount) || 0;
        const items = Array.isArray(data?.items) ? data.items : [];

        document.getElementById("header-cart-count")?.replaceChildren(String(cartCount));

        if (!cartPreviewContent) {
            return;
        }

        if (cartCount <= 0 || items.length === 0) {
            cartPreviewContent.innerHTML = `
                <div class="cart-preview-empty">
                    <div class="cart-preview-empty-art">
                        <i class="fa-solid fa-cart-shopping"></i>
                        <i class="fa-solid fa-star"></i>
                    </div>
                    <p>Giỏ hàng trống</p>
                </div>
            `;
        } else {
            const itemHtml = items.map((item) => {
                const itemName = escapeHtml(item.name);
                const imageUrl = escapeHtml(item.imageUrl || "");

                return `
                    <div class="cart-preview-item">
                        <img src="${imageUrl}" alt="${itemName}">
                        <div class="cart-preview-info">
                            <p class="cart-preview-name">${itemName}</p>
                            <p class="cart-preview-price">${item.quantity} x ${formatCurrency(item.price)}</p>
                        </div>
                    </div>
                `;
            }).join("");

            cartPreviewContent.innerHTML = `
                <h3>Sản phẩm mới thêm</h3>
                <div class="cart-preview-list">
                    ${itemHtml}
                </div>
                <div class="cart-preview-footer">
                    <span>${cartCount} Sản phẩm trong giỏ hàng</span>
                    <a href="${contextPath}/cart">Xem giỏ hàng</a>
                </div>
            `;
        }

        if (shouldOpen && cartLoginPopover && cartLink) {
            cartLoginPopover.hidden = false;
            cartLoginPopover.classList.add("open");
            cartLoginPopover.setAttribute("aria-hidden", "false");
            cartLink.setAttribute("aria-expanded", "true");
        }
    }

    function syncCartPreview() {
        if (!cartPreviewContent) {
            return;
        }

        fetch(`${contextPath}/cart/summary`, {
            headers: {
                "X-Requested-With": "XMLHttpRequest"
            }
        })
            .then((response) => response.ok ? response.json() : null)
            .then((data) => {
                if (data?.success) {
                    renderCartPreview(data);
                }
            })
            .catch(() => {});
    }

    window.renderCartPreviewFromSummary = renderCartPreview;
    window.syncCartPreview = syncCartPreview;

    function closeAll() {
        categoryDropdown?.classList.remove("open");
        userDropdown?.classList.remove("open");
        cartLoginPopover?.classList.remove("open");
        closeSearchDropdown();
        if (cartLoginPopover) {
            cartLoginPopover.hidden = true;
        }
        overlay?.classList.remove("active");

        menuButton?.setAttribute("aria-expanded", "false");
        cartLink?.setAttribute("aria-expanded", "false");
        categoryDropdown?.setAttribute("aria-hidden", "true");
        cartLoginPopover?.setAttribute("aria-hidden", "true");
    }

    menuButton?.addEventListener("click", (event) => {
        event.preventDefault();
        event.stopPropagation();

        if (!categoryDropdown) {
            return;
        }

        const isOpen = categoryDropdown.classList.toggle("open");

        overlay?.classList.toggle("active", isOpen);
        userDropdown?.classList.remove("open");
        cartLoginPopover?.classList.remove("open");
        if (cartLoginPopover) {
            cartLoginPopover.hidden = true;
        }

        menuButton.setAttribute("aria-expanded", String(isOpen));
        cartLink?.setAttribute("aria-expanded", "false");
        categoryDropdown.setAttribute("aria-hidden", String(!isOpen));
        cartLoginPopover?.setAttribute("aria-hidden", "true");
    });

    userToggle?.addEventListener("click", (event) => {
        event.preventDefault();
        event.stopPropagation();

        if (!userDropdown) {
            return;
        }

        const isOpen = userDropdown.classList.toggle("open");

        overlay?.classList.toggle("active", isOpen);
        categoryDropdown?.classList.remove("open");
        cartLoginPopover?.classList.remove("open");
        if (cartLoginPopover) {
            cartLoginPopover.hidden = true;
        }

        menuButton?.setAttribute("aria-expanded", "false");
        cartLink?.setAttribute("aria-expanded", "false");
        categoryDropdown?.setAttribute("aria-hidden", "true");
        cartLoginPopover?.setAttribute("aria-hidden", "true");
    });

    cartLink?.addEventListener("click", (event) => {
        event.preventDefault();
        event.stopPropagation();

        if (!cartLoginPopover) {
            return;
        }

        const isOpen = !cartLoginPopover.classList.contains("open");

        overlay?.classList.remove("active");
        categoryDropdown?.classList.remove("open");
        userDropdown?.classList.remove("open");

        menuButton?.setAttribute("aria-expanded", "false");
        cartLink.setAttribute("aria-expanded", String(isOpen));
        categoryDropdown?.setAttribute("aria-hidden", "true");
        cartLoginPopover.hidden = !isOpen;
        cartLoginPopover.classList.toggle("open", isOpen);
        cartLoginPopover.setAttribute("aria-hidden", String(!isOpen));
    });

    searchInput?.addEventListener("focus", () => {
        renderSearchDropdown(searchInput.value);
    });

    searchInput?.addEventListener("input", () => {
        renderSearchDropdown(searchInput.value);
    });

    searchClear?.addEventListener("click", (event) => {
        event.preventDefault();
        searchInput.value = "";
        searchInput.focus();
        renderSearchDropdown("");
    });

    searchClearAll?.addEventListener("click", (event) => {
        event.preventDefault();
        localStorage.removeItem(recentSearchKey);
        renderSearchDropdown(searchInput?.value || "");
    });

    searchDropdown?.addEventListener("click", (event) => {
        const removeButton = event.target.closest("[data-remove-search]");
        if (removeButton) {
            event.preventDefault();
            event.stopPropagation();
            removeRecentSearch(removeButton.dataset.removeSearch);
            return;
        }

        const row = event.target.closest("[data-recent-search], [data-suggestion-search]");
        if (!row) {
            return;
        }

        event.preventDefault();
        const keyword = row.dataset.recentSearch
            || row.dataset.suggestionSearch
            || searchInput?.value
            || "";
        submitSearch(keyword);
    });

    searchForm?.addEventListener("submit", (event) => {
        const keyword = searchInput?.value.trim() || "";
        if (!keyword) {
            event.preventDefault();
            renderSearchDropdown("");
            return;
        }
        saveRecentSearch(keyword);
    });

    overlay?.addEventListener("click", () => {
        closeAll();
    });

    document.addEventListener("click", (event) => {
        if (!menuButton?.contains(event.target) && !categoryDropdown?.contains(event.target) && !userToggle?.contains(event.target) && !userDropdown?.contains(event.target) && !cartLink?.contains(event.target) && !cartLoginPopover?.contains(event.target) && !searchForm?.contains(event.target)) {
            closeAll();
        }
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeAll();
        }
    });

    document.querySelectorAll(".header-category-dropdown a").forEach((link) => {
        link.addEventListener("click", () => {
            closeAll();
        });
    });

    document.querySelectorAll(".header-menu-bar a").forEach((link) => {
        link.addEventListener("click", (event) => {
            const href = link.getAttribute("href");

            if (!href) {
                return;
            }

            const hashIndex = href.indexOf("#");

            if (hashIndex === -1) {
                return;
            }

            const targetSelector = href.substring(hashIndex);
            const target = document.querySelector(targetSelector);

            if (!target) {
                return;
            }

            event.preventDefault();
            closeAll();

            target.scrollIntoView({
                behavior: "smooth", block: "start"
            });
        });
    });

    syncCartPreview();

    window.addEventListener("pageshow", () => {
        syncCartPreview();
    });
});
