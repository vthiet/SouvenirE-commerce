document.addEventListener("DOMContentLoaded", () => {
    const filterForm = document.querySelector(".filter-sidebar form");
    const content = document.querySelector(".product-type-content");
    if (!filterForm || !content) return;

    const sortSelect = filterForm.querySelector("select[name='sort']");
    const ratingRadios = filterForm.querySelectorAll("input[name='rating']");
    const priceRangeRadios = filterForm.querySelectorAll("input[name='priceRange']");
    const minPriceInput = filterForm.querySelector("input[name='minPrice']");
    const maxPriceInput = filterForm.querySelector("input[name='maxPrice']");
    let nativeSubmit = false;

    function resetPageToFirst() {
        let pageInput = filterForm.querySelector("input[name='page']");
        if (!pageInput) {
            pageInput = document.createElement("input");
            pageInput.type = "hidden";
            pageInput.name = "page";
            filterForm.appendChild(pageInput);
        }
        pageInput.value = "1";
    }

    function buildUrlFromForm() {
        const url = new URL(filterForm.action, window.location.origin);
        const formData = new FormData(filterForm);
        url.search = "";

        formData.forEach((value, key) => {
            const normalized = String(value || "").trim();
            if (normalized) {
                url.searchParams.set(key, normalized);
            }
        });

        url.searchParams.set("ajax", "true");
        return url;
    }

    function cleanAjaxUrl(url) {
        const clean = new URL(url.toString());
        clean.searchParams.delete("ajax");
        return clean;
    }

    function setLoading(loading) {
        content.classList.toggle("is-loading", loading);
    }

    async function loadFragment(url, pushState = true) {
        setLoading(true);
        try {
            const response = await fetch(url, {
                headers: {"X-Requested-With": "XMLHttpRequest"}
            });
            if (!response.ok) {
                throw new Error("Filter request failed");
            }

            const html = await response.text();
            const current = document.getElementById("productTypeResults");
            if (!current) {
                throw new Error("Result container missing");
            }
            current.outerHTML = html;

            if (pushState) {
                window.history.pushState({}, "", cleanAjaxUrl(url));
            }
        } catch (error) {
            nativeSubmit = true;
            const fallback = cleanAjaxUrl(url);
            window.location.href = fallback.toString();
        } finally {
            setLoading(false);
        }
    }

    function submitAjax() {
        resetPageToFirst();
        loadFragment(buildUrlFromForm());
    }

    function resetFilterControls() {
        if (minPriceInput) minPriceInput.value = "";
        if (maxPriceInput) maxPriceInput.value = "";
        priceRangeRadios.forEach((radio) => {
            radio.checked = false;
        });
        ratingRadios.forEach((radio) => {
            radio.checked = radio.value === "";
        });
        if (sortSelect) {
            sortSelect.value = "popular";
        }
        resetPageToFirst();
    }

    sortSelect?.addEventListener("change", submitAjax);

    ratingRadios.forEach((radio) => {
        radio.addEventListener("change", submitAjax);
    });

    priceRangeRadios.forEach((radio) => {
        radio.addEventListener("change", () => {
            if (minPriceInput) minPriceInput.value = radio.dataset.minPrice || "";
            if (maxPriceInput) maxPriceInput.value = radio.dataset.maxPrice || "";
        });
    });

    [minPriceInput, maxPriceInput].forEach((input) => {
        input?.addEventListener("input", () => {
            priceRangeRadios.forEach((radio) => {
                radio.checked = false;
            });
        });
    });

    filterForm.addEventListener("submit", (event) => {
        if (nativeSubmit) {
            return;
        }
        event.preventDefault();
        submitAjax();
    });

    document.addEventListener("click", (event) => {
        const paginationLink = event.target.closest("#productTypeResults .pagination a");
        if (paginationLink) {
            event.preventDefault();
            const url = new URL(paginationLink.href, window.location.origin);
            url.searchParams.set("ajax", "true");
            loadFragment(url);
            return;
        }

        const clearAll = event.target.closest("#productTypeResults .active-filter-clear");
        if (clearAll) {
            event.preventDefault();
            resetFilterControls();
            const url = new URL(clearAll.href, window.location.origin);
            url.searchParams.set("ajax", "true");
            loadFragment(url);
        }
    });

    window.addEventListener("popstate", () => {
        const url = new URL(window.location.href);
        url.searchParams.set("ajax", "true");
        loadFragment(url, false);
    });
});
