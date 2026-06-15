<%@ page contentType="text/html;charset=UTF-8" pageEncoding="UTF-8" %>
<script>
    (function () {
        var savedTheme = localStorage.getItem('inola-theme');
        var prefersDark = window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;
        var activeTheme = savedTheme || (prefersDark ? 'dark' : 'light');
        document.documentElement.setAttribute('data-theme', activeTheme);
    })();
</script>
<style>
    :root {
        --page-background: #f4f6f1;
        --card-background: #ffffff;
        --muted-background: #f7f9f4;
        --input-background: #ffffff;
        --main-text: #344034;
        --heading-text: #1f2933;
        --secondary-text: #667085;
        --muted-text: #8a9488;
        --main-border: #d6ddd2;
        --soft-border: #edf0e8;
        --primary-color: #247a52;
        --primary-hover-color: #1b5e40;
        --accent-color: #c28a35;
        --danger-color: #b42318;
        --success-color: #027a48;
        --focus-outline: 0 0 0 3px rgba(36, 122, 82, 0.16);
        --card-shadow: 0 8px 24px rgba(31, 41, 51, 0.12);
    }

    html[data-theme="dark"] {
        color-scheme: dark;
        --page-background: #101614;
        --card-background: #18211d;
        --muted-background: #202b26;
        --input-background: #151c19;
        --main-text: #d7e0d4;
        --heading-text: #f3f7f1;
        --secondary-text: #aebcad;
        --muted-text: #839181;
        --main-border: #34463d;
        --soft-border: #26342e;
        --primary-color: #58c98d;
        --primary-hover-color: #7addaa;
        --accent-color: #e0ad5c;
        --danger-color: #ff8a80;
        --success-color: #7ae3a7;
        --focus-outline: 0 0 0 3px rgba(88, 201, 141, 0.24);
        --card-shadow: 0 12px 34px rgba(0, 0, 0, 0.34);

        --surface-page: var(--page-background);
        --surface-default: var(--card-background);
        --surface-muted: var(--muted-background);
        --surface-subtle: var(--input-background);
        --text-strong: var(--heading-text);
        --text-default: var(--main-text);
        --text-soft: var(--secondary-text);
        --text-subtle: var(--muted-text);
        --border-default: var(--main-border);
        --border-subtle: var(--soft-border);
        --brand-primary: var(--primary-color);
        --brand-primary-hover: var(--primary-hover-color);
        --brand-accent: var(--accent-color);
        --danger: var(--danger-color);
        --success: var(--success-color);
        --focus-ring: var(--focus-outline);
        --shadow-sm: var(--card-shadow);
        --shadow-md: var(--card-shadow);

        --bg-main: var(--page-background);
        --bg-white: var(--card-background);
        --bg-light: var(--muted-background);
        --text-main: var(--main-text);
        --text-heading: var(--heading-text);
        --text-secondary: var(--secondary-text);
        --text-muted: var(--muted-text);
        --color-primary: var(--primary-color);
        --color-primary-dark: var(--primary-hover-color);
        --color-accent: var(--accent-color);
        --border-light: var(--soft-border);
        --border-normal: var(--main-border);
    }

    html[data-theme="dark"] body,
    html[data-theme="dark"] .auth-page,
    html[data-theme="dark"] .account-page,
    html[data-theme="dark"] .product-page,
    html[data-theme="dark"] .page-container {
        background: var(--page-background) !important;
        color: var(--main-text) !important;
    }

    html[data-theme="dark"] h1,
    html[data-theme="dark"] h2,
    html[data-theme="dark"] h3,
    html[data-theme="dark"] h4,
    html[data-theme="dark"] h5,
    html[data-theme="dark"] h6,
    html[data-theme="dark"] .product-title,
    html[data-theme="dark"] .success-title,
    html[data-theme="dark"] .account-heading h1 {
        color: var(--heading-text) !important;
    }

    html[data-theme="dark"] p,
    html[data-theme="dark"] span,
    html[data-theme="dark"] label,
    html[data-theme="dark"] .supporting-text,
    html[data-theme="dark"] .success-text,
    html[data-theme="dark"] .muted {
        color: inherit;
    }

    html[data-theme="dark"] .site-header,
    html[data-theme="dark"] .header-top,
    html[data-theme="dark"] .header-menu-bar,
    html[data-theme="dark"] .header-breadcrumb,
    html[data-theme="dark"] .site-footer,
    html[data-theme="dark"] .footer-bottom,
    html[data-theme="dark"] .auth-panel,
    html[data-theme="dark"] .profile-panel,
    html[data-theme="dark"] .account-sidebar,
    html[data-theme="dark"] .account-content,
    html[data-theme="dark"] .product-right,
    html[data-theme="dark"] .product-gallery,
    html[data-theme="dark"] .description-content,
    html[data-theme="dark"] .review-summary-block,
    html[data-theme="dark"] .review-mright,
    html[data-theme="dark"] .review-box,
    html[data-theme="dark"] .success-card,
    html[data-theme="dark"] .card,
    html[data-theme="dark"] .product-card,
    html[data-theme="dark"] .user-dropdown,
    html[data-theme="dark"] .header-category-dropdown {
        background: var(--card-background) !important;
        color: var(--main-text) !important;
        border-color: var(--main-border) !important;
        box-shadow: var(--card-shadow);
    }

    html[data-theme="dark"] .card-soft,
    html[data-theme="dark"] .input-wrap,
    html[data-theme="dark"] .order-code,
    html[data-theme="dark"] .empty-state,
    html[data-theme="dark"] .profile-static,
    html[data-theme="dark"] .address-card,
    html[data-theme="dark"] .order-card,
    html[data-theme="dark"] .spec-table td,
    html[data-theme="dark"] .review-filter-bar,
    html[data-theme="dark"] .store-note-block-left {
        background: var(--muted-background) !important;
        color: var(--main-text) !important;
        border-color: var(--main-border) !important;
    }

    html[data-theme="dark"] input,
    html[data-theme="dark"] select,
    html[data-theme="dark"] textarea {
        background: var(--input-background) !important;
        color: var(--main-text) !important;
        border-color: var(--main-border) !important;
    }

    html[data-theme="dark"] input::placeholder,
    html[data-theme="dark"] textarea::placeholder {
        color: var(--muted-text) !important;
    }

    html[data-theme="dark"] a:hover,
    html[data-theme="dark"] .header-top a:hover,
    html[data-theme="dark"] .header-menu-bar a:hover {
        color: var(--primary-color) !important;
    }

    html[data-theme="dark"] .primary-button,
    html[data-theme="dark"] .btn-primary,
    html[data-theme="dark"] .add-cart,
    html[data-theme="dark"] .buy-now-full,
    html[data-theme="dark"] .submit-review {
        background: var(--primary-color) !important;
        color: #08110d !important;
        border-color: var(--primary-color) !important;
    }

    html[data-theme="dark"] .secondary-button,
    html[data-theme="dark"] .btn-secondary,
    html[data-theme="dark"] .filter-btn,
    html[data-theme="dark"] .sort-select,
    html[data-theme="dark"] .qty-btn {
        background: var(--muted-background) !important;
        color: var(--main-text) !important;
        border-color: var(--main-border) !important;
    }

    html[data-theme="dark"] .old-price,
    html[data-theme="dark"] .review-count,
    html[data-theme="dark"] .text-muted,
    html[data-theme="dark"] .supporting-text,
    html[data-theme="dark"] .profile-avatar__hint {
        color: var(--muted-text) !important;
    }

    html[data-theme="dark"] .sale-price,
    html[data-theme="dark"] .normal-price,
    html[data-theme="dark"] .current-price,
    html[data-theme="dark"] .item-price {
        color: var(--accent-color) !important;
    }

    html[data-theme="dark"] img {
        filter: brightness(0.92) contrast(1.03);
    }

    .theme-toggle-button {
        min-width: 38px;
        height: 38px;
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: 8px;
        border: 1px solid var(--border-normal, var(--main-border));
        border-radius: 999px;
        background: var(--bg-white, var(--card-background));
        color: var(--text-main, var(--main-text));
        cursor: pointer;
        transition: background .2s ease, border-color .2s ease, color .2s ease;
    }

    .theme-toggle-button:hover,
    .theme-toggle-button:focus-visible {
        border-color: var(--color-primary, var(--primary-color));
        color: var(--color-primary, var(--primary-color));
        outline: none;
        box-shadow: var(--focus-ring, var(--focus-outline));
    }

    .theme-toggle-button .theme-label {
        font-size: 13px;
        font-weight: 600;
    }

    .auth-theme-toggle {
        position: fixed;
        top: 18px;
        right: 18px;
        z-index: 20;
        padding: 0 14px;
    }

    @media (max-width: 640px) {
        .theme-toggle-button .theme-label {
            display: none;
        }
    }
</style>
<script>
    document.addEventListener('DOMContentLoaded', function () {
        var buttons = document.querySelectorAll('[data-theme-toggle]');

        function syncButtons(theme) {
            buttons.forEach(function (button) {
                var icon = button.querySelector('[data-theme-icon]');
                var label = button.querySelector('[data-theme-label]');
                var isDark = theme === 'dark';
                button.setAttribute('aria-pressed', String(isDark));
                button.setAttribute('title', isDark ? 'Chuyển sang giao diện sáng' : 'Chuyển sang giao diện tối');
                if (icon) {
                    icon.className = isDark ? 'fa-solid fa-sun' : 'fa-solid fa-moon';
                }
                if (label) {
                    label.textContent = isDark ? 'Sáng' : 'Tối';
                }
            });
        }

        function setTheme(theme) {
            document.documentElement.setAttribute('data-theme', theme);
            localStorage.setItem('inola-theme', theme);
            syncButtons(theme);
        }

        buttons.forEach(function (button) {
            button.addEventListener('click', function () {
                var currentTheme = document.documentElement.getAttribute('data-theme') === 'dark' ? 'dark' : 'light';
                setTheme(currentTheme === 'dark' ? 'light' : 'dark');
            });
        });

        syncButtons(document.documentElement.getAttribute('data-theme') || 'light');
    });
</script>
