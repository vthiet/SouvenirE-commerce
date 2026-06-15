# TAI LIEU QUY CHUAN THIET KE GIAO DIEN - SOUVENIR E-COMMERCE

**Du an:** He thong Thuong mai Dien tu Souvenir E-commerce  
**Loai tai lieu:** Software Design Description / UI Design Guidelines  
**Dinh dang:** IEEE-style, tham chieu cach cau truc cua IEEE 1016 cho tai lieu thiet ke phan mem va IEEE 830/29148 cho dac ta yeu cau  
**Nen tang:** Java Servlet, JSP, CSS, JavaScript  
**Pham vi ap dung:** `src/main/webapp`, `WEB-INF/views`, `WEB-INF/layout`, `assets/css`, `assets/js`  
**Phien ban:** 1.0  
**Ngay lap:** 2026-06-08  
**Trang thai:** Baseline de ap dung va review

---

## Muc Luc

1. [Gioi Thieu](#1-gioi-thieu)
2. [Pham Vi Va Muc Tieu Thiet Ke](#2-pham-vi-va-muc-tieu-thiet-ke)
3. [Tong Quan He Thong Giao Dien](#3-tong-quan-he-thong-giao-dien)
4. [Nguyen Tac Thiet Ke](#4-nguyen-tac-thiet-ke)
5. [Nhan Dien Thuong Hieu Va Visual Direction](#5-nhan-dien-thuong-hieu-va-visual-direction)
6. [Design Tokens](#6-design-tokens)
7. [Mau Sac](#7-mau-sac)
8. [Typography](#8-typography)
9. [Khoang Cach, Luoi Va Bo Cuc](#9-khoang-cach-luoi-va-bo-cuc)
10. [Thanh Phan Giao Dien](#10-thanh-phan-giao-dien)
11. [Responsive Design](#11-responsive-design)
12. [Light Mode Va Dark Mode](#12-light-mode-va-dark-mode)
13. [Trang Thai, Tuong Tac Va Chuyen Dong](#13-trang-thai-tuong-tac-va-chuyen-dong)
14. [Hinh Anh, Icon Va Media](#14-hinh-anh-icon-va-media)
15. [Accessibility Va Usability](#15-accessibility-va-usability)
16. [Quy Uoc CSS/JSP](#16-quy-uoc-cssjsp)
17. [Kiem Thu Giao Dien](#17-kiem-thu-giao-dien)
18. [Quan Ly Thay Doi Design System](#18-quan-ly-thay-doi-design-system)
19. [Checklist Nghiem Thu](#19-checklist-nghiem-thu)
20. [Phu Luc](#20-phu-luc)

---

## 1. Gioi Thieu

### 1.1 Muc Dich

Tai lieu nay dinh nghia quy chuan thiet ke giao dien cho website Souvenir E-commerce. Muc tieu la tao mot baseline thong nhat ve mau sac, font chu, kich thuoc chu, padding, margin, responsive, light mode, dark mode, component state va cach bao tri giao dien.

Tai lieu nay phuc vu cac nhom sau:

| Nhom nguoi dung tai lieu | Nhu cau |
| --- | --- |
| Developer frontend/JSP | Biet cach ap dung CSS token, component, layout va responsive vao trang moi |
| Developer backend co sua JSP | Biet cac quy tac giao dien toi thieu de khong pha vo design system |
| QA/Tester | Co checklist de nghiem thu UI tren desktop, tablet, mobile va dark mode |
| Nhom thiet ke/san pham | Co co so de review consistency, usability va brand identity |
| Giang vien/reviewer | Co tai lieu structured theo phong cach IEEE de danh gia tinh chuyen nghiep cua thiet ke |

### 1.2 Pham Vi

Tai lieu ap dung cho:

| Khu vuc | Vi tri code |
| --- | --- |
| Layout chinh | `src/main/webapp/WEB-INF/layout/base.jsp`, `header.jsp`, `footer.jsp` |
| Trang khach hang | `home.jsp`, `productType.jsp`, `cart.jsp`, `checkout.jsp`, `WEB-INF/views/product/*` |
| Trang tai khoan | `WEB-INF/views/account/*`, `assets/css/account/*` |
| Trang xac thuc | `WEB-INF/views/auth/*`, `assets/css/auth/*` |
| Trang quan tri | `src/main/webapp/admin/*`, `assets/css/admin-*`, `*Style.css` |
| CSS dung chung | `assets/css/theme.css`, `Base.css`, `Component.css`, `layout/header.css`, `layout/footer.css` |

Tai lieu khong mo ta logic nghiep vu, database, security backend hoac API. Cac noi dung do duoc quy dinh trong tai lieu kien truc va README cua du an.

### 1.3 Tai Lieu Lien Quan

| Tai lieu | Vai tro |
| --- | --- |
| `README.md` | Tong quan he thong theo chuan IEEE |
| `docs/Architectural & Development Guidelines.md` | Quy chuan kien truc va phat trien |
| `docs/Modules Description.md` | Mo ta module va hien trang chuc nang |
| `src/main/webapp/assets/css/theme.css` | Nguon token mau sac hien tai |
| `src/main/webapp/assets/css/Base.css` | Reset, typography co ban, container va product card |
| `src/main/webapp/assets/css/Component.css` | Button, card, form, table, pagination va component co ban |

### 1.4 Dinh Nghia Va Tu Viet Tat

| Thuat ngu | Dinh nghia |
| --- | --- |
| Design Token | Bien thiet ke tai su dung duoc, vi du `--brand-primary`, `--space-4`, `--radius-md` |
| Surface | Nen cua trang, card, modal, dropdown hoac khu vuc noi dung |
| Breakpoint | Moc chieu rong man hinh de thay doi layout responsive |
| State | Trang thai UI nhu hover, focus, active, disabled, error, success |
| WCAG | Web Content Accessibility Guidelines, bo huong dan kha dung va tiep can web |
| CTA | Call To Action, nut hanh dong chinh |
| BEM | Cach dat ten CSS theo Block-Element-Modifier |

---

## 2. Pham Vi Va Muc Tieu Thiet Ke

### 2.1 Muc Tieu Chinh

| Ma | Muc tieu | Tieu chi chap nhan |
| --- | --- | --- |
| DG-01 | Dong nhat nhan dien giao dien | Trang khach hang, tai khoan va admin su dung chung token mau, font, spacing va radius |
| DG-02 | Tang tinh chuyen nghiep | UI co hierarchy ro rang, can le nhat quan, state day du, khong dung mau ngau nhien |
| DG-03 | Ho tro responsive | Layout hoat dong tot tu 360px den 1440px+ |
| DG-04 | Ho tro light/dark mode | Moi mau nen, text, border, form, card, table duoc map qua token |
| DG-05 | Dam bao accessibility | Tuong phan mau, focus state, keyboard navigation va target size dat muc co ban |
| DG-06 | De bao tri | Token tap trung trong `theme.css`; component dung chung khong bi duplicate tuy tien |

### 2.2 Ranh Gioi Thiet Ke

| Trong pham vi | Ngoai pham vi |
| --- | --- |
| Mau sac, typography, spacing, layout, component states | Thiet ke database |
| Responsive cho JSP/CSS | Refactor backend |
| Light mode, dark mode | Tich hop payment gateway |
| Quy uoc CSS va QA UI | Viet test backend |
| Design governance | Chinh sach kinh doanh |

### 2.3 Gia Dinh Thiet Ke

| Ma | Gia dinh | Ly do |
| --- | --- | --- |
| AS-01 | Website render server-side bang JSP | Da duoc xac nhan trong codebase |
| AS-02 | CSS thuan la nen tang chinh | Khong co framework frontend nhu React/Vue trong du an |
| AS-03 | `theme.css` la nguon token trung tam | File hien tai da dinh nghia brand palette va alias |
| AS-04 | Giao dien phuc vu nguoi dung Viet Nam | Noi dung, tien te, san pham dac san dia phuong va ngon ngu chinh la tieng Viet |

---

## 3. Tong Quan He Thong Giao Dien

### 3.1 Kien Truc UI Hien Tai

```text
src/main/webapp/
├── WEB-INF/layout/
│   ├── base.jsp              # Khung HTML, load global CSS/JS, header, footer
│   ├── header.jsp            # Header chinh
│   └── footer.jsp            # Footer chinh
├── WEB-INF/views/
│   ├── auth/                 # Login/signup
│   ├── account/              # Ho so, dia chi, don hang
│   └── product/              # Chi tiet san pham, product card
├── admin/                    # Trang quan tri
└── assets/
    ├── css/
    │   ├── theme.css         # Token mau, surface, text, shadow, radius
    │   ├── Base.css          # Reset, base typography, container, product list/card
    │   ├── Component.css     # Component dung chung
    │   ├── layout/           # Header/footer
    │   ├── account/          # Trang tai khoan
    │   └── auth/             # Login/signup
    ├── js/
    └── images/
```

### 3.2 Luong Tai CSS Khuyen Nghi

Thu tu load CSS phai dam bao token co truoc component:

```html
<link rel="stylesheet" href="/assets/css/theme.css">
<link rel="stylesheet" href="/assets/css/Base.css">
<link rel="stylesheet" href="/assets/css/layout/header.css">
<link rel="stylesheet" href="/assets/css/layout/footer.css">
<link rel="stylesheet" href="/assets/css/account/account-common.css">
<link rel="stylesheet" href="/assets/css/{pageCss}">
```

Quy tac:

| Thu tu | Loai CSS | Muc dich |
| --- | --- | --- |
| 1 | `theme.css` | Dinh nghia token va theme |
| 2 | `Base.css` | Reset, global element, container, product list/card |
| 3 | Layout CSS | Header, footer, shell |
| 4 | Common component CSS | Button, card, form, table, badge |
| 5 | Page CSS | Dieu chinh rieng cho tung trang |

Page CSS khong duoc override token bang hard-code neu co the them token vao `theme.css`.

---

## 4. Nguyen Tac Thiet Ke

### 4.1 Nguyen Tac Nhan Dien

| Ma | Nguyen tac | Mo ta |
| --- | --- | --- |
| DP-01 | Vietnamese Market Warmth | Giao dien phai tao cam giac gan gui, tin cay, am ap, phu hop san pham dac san va qua luu niem |
| DP-02 | Commerce First | Gia, khuyen mai, CTA mua hang va tinh trang san pham phai noi bat nhung khong gay roi |
| DP-03 | Trust Before Decoration | Thanh toan, dia chi, don hang va admin phai ro rang, an toan, it hieu ung thua |
| DP-04 | Token-Driven Design | Moi mau, bo goc, shadow, spacing nen tham chieu token |
| DP-05 | Mobile Practicality | Mobile uu tien doc duoc, bam duoc, khong can thao tac tinh vi |
| DP-06 | Progressive Enhancement | Chuc nang co ban phai dung duoc khi animation hoac mot so JS khong tai duoc |

### 4.2 He Thong Uu Tien Thi Giac

Thu tu uu tien khi thiet ke mot trang:

1. Nhiem vu chinh cua nguoi dung: tim san pham, xem gia, them vao gio, thanh toan, quan ly don.
2. Tin hieu tin cay: trang thai, ton kho, thong tin van chuyen, bao mat, lien he.
3. Dieu huong: header, breadcrumb, filter, pagination.
4. Noi dung phu: mo ta, banner, goi y san pham, lien ket footer.
5. Trang tri: gradient, pattern, icon, animation.

### 4.3 Quy Tac Khong Duoc Lam

| Ma | Quy tac |
| --- | --- |
| DN-01 | Khong them mau hard-code moi vao CSS neu mau do co vai tro lap lai |
| DN-02 | Khong dung nhieu font khac nhau tren cung mot page neu khong co ly do brand ro rang |
| DN-03 | Khong an focus outline cua input/button/link neu khong thay bang focus state dat chuan |
| DN-04 | Khong dat text quan trong tren anh neu khong co overlay dam bao tuong phan |
| DN-05 | Khong dung animation lap lai lien tuc cho thanh phan nghiep vu nhu form thanh toan |
| DN-06 | Khong thiet ke mobile bang cach chi thu nho desktop |

---

## 5. Nhan Dien Thuong Hieu Va Visual Direction

### 5.1 Dinh Vi Hinh Anh

Souvenir E-commerce nen the hien cam giac:

| Thuoc tinh | Cach the hien |
| --- | --- |
| Dia phuong | Mau xanh la cho nong san/tu nhien, vang nau cho thu cong/dac san |
| Tin cay | Nen sang, border ro, CTA on dinh, trang thai form minh bach |
| Am ap | Accent vang nau, anh san pham chat luong, khoang trang rong |
| Hien dai | Grid gon, card ro, type scale nhat quan, responsive day du |

### 5.2 Phong Cach Giao Dien

| Hang muc | Huong dan |
| --- | --- |
| Layout | Rong, thoang, container toi da 1400px, grid san pham ro rang |
| Shape | Radius vua phai 6-12px, tranh bo tron qua nhieu voi admin/table |
| Shadow | Dung de nang card/dropdown/modal, khong lap dung tren tat ca block |
| Color | Xanh la la mau hanh dong/tin cay, vang nau la diem nhan thuong mai |
| Imagery | Anh san pham nen sach, sang, ti le dong nhat, uu tien 1:1 cho card |

---

## 6. Design Tokens

### 6.1 Nguyen Tac Token

Token la nguon su that cho design system. Moi token can co:

| Thanh phan | Vi du | Ghi chu |
| --- | --- | --- |
| Ten co y nghia | `--brand-primary` | Noi ro vai tro, khong dat `--green-1` neu token dung theo brand |
| Gia tri on dinh | `#247a52` | Thay doi phai qua review |
| Alias legacy | `--color-primary` | Duy tri de CSS cu khong vo |
| Theme mapping | Light/dark | Cung ten token, khac gia tri theo theme |

### 6.2 Token Hien Tai Can Giu Lam Baseline

Nguon: `src/main/webapp/assets/css/theme.css`.

| Nhom | Token | Gia tri light | Vai tro |
| --- | --- | --- | --- |
| Brand | `--brand-primary` | `#247a52` | Mau chu dao, CTA chinh, link hover, focus brand |
| Brand | `--brand-primary-hover` | `#1b5e40` | Hover/active cho primary action |
| Brand | `--brand-primary-soft` | `#e9f5ee` | Nen nhe cho state selected/info brand |
| Brand | `--brand-secondary` | `#3f6f86` | Mau phu, chart, highlight secondary |
| Brand | `--brand-accent` | `#c28a35` | Accent thuong mai, sale, diem nhan dac san |
| Brand | `--brand-accent-hover` | `#9f6f25` | Hover cho accent action |
| Brand | `--brand-accent-soft` | `#fff4df` | Nen sale/promotion nhe |
| Surface | `--surface-page` | `#f4f6f1` | Nen toan trang |
| Surface | `--surface-default` | `#ffffff` | Card, form, dropdown, modal |
| Surface | `--surface-muted` | `#f7f9f4` | Section nhe, table header, input group |
| Surface | `--surface-subtle` | `#fbfcf8` | Nen cuc nhe |
| Surface | `--surface-inverse` | `#1f2933` | Nen dao cho footer/CTA dac biet |
| Text | `--text-strong` | `#1f2933` | Heading, text quan trong |
| Text | `--text-default` | `#344034` | Body text |
| Text | `--text-soft` | `#667085` | Secondary text |
| Text | `--text-subtle` | `#8a9488` | Placeholder, metadata |
| Border | `--border-subtle` | `#edf0e8` | Border rat nhe |
| Border | `--border-default` | `#d6ddd2` | Border form/card |
| Border | `--border-strong` | `#b7c5b1` | Border nhan manh |
| Feedback | `--success` | `#027a48` | Thanh cong |
| Feedback | `--warning` | `#b54708` | Canh bao |
| Feedback | `--danger` | `#b42318` | Loi/nguy hiem |
| Commerce | `--price` | `#1f2933` | Gia mac dinh |
| Commerce | `--sale` | `#c28a35` | Gia sale/khuyen mai |
| Commerce | `--rating` | `#f5a623` | Sao danh gia |
| Shape | `--radius-sm` | `6px` | Input, badge, pagination |
| Shape | `--radius-md` | `8px` | Card nho, dropdown |
| Shape | `--radius-lg` | `12px` | Card lon, button noi bat |

### 6.3 Token Can Bo Sung

De design system day du hon, nen bo sung cac token sau vao `theme.css` trong lan refactor UI tiep theo.

```css
:root {
    /* Spacing scale */
    --space-0: 0;
    --space-1: 4px;
    --space-2: 8px;
    --space-3: 12px;
    --space-4: 16px;
    --space-5: 20px;
    --space-6: 24px;
    --space-8: 32px;
    --space-10: 40px;
    --space-12: 48px;
    --space-16: 64px;

    /* Typography scale */
    --font-sans: 'Google Sans Flex', 'Segoe UI', Arial, sans-serif;
    --font-display: 'Google Sans Flex', 'Segoe UI', Arial, sans-serif;
    --font-mono: 'JetBrains Mono', 'Consolas', monospace;

    --text-xs: 12px;
    --text-sm: 14px;
    --text-md: 16px;
    --text-lg: 18px;
    --text-xl: 20px;
    --text-2xl: 24px;
    --text-3xl: 32px;
    --text-4xl: 40px;

    --line-tight: 1.2;
    --line-normal: 1.5;
    --line-relaxed: 1.65;

    /* Layout */
    --container-max: 1400px;
    --container-padding-desktop: 40px;
    --container-padding-tablet: 24px;
    --container-padding-mobile: 16px;

    /* Z-index */
    --z-header: 5100;
    --z-dropdown: 5300;
    --z-modal: 7000;
    --z-toast: 8000;
}
```

### 6.4 Quy Tac Dat Ten Token

| Loai token | Mau ten | Vi du |
| --- | --- | --- |
| Brand | `--brand-{role}` | `--brand-primary` |
| Surface | `--surface-{role}` | `--surface-muted` |
| Text | `--text-{strength}` | `--text-soft` |
| Border | `--border-{strength}` | `--border-default` |
| Feedback | `--{state}` va `--{state}-bg` | `--danger`, `--danger-bg` |
| Spacing | `--space-{step}` | `--space-4` |
| Radius | `--radius-{size}` | `--radius-md` |
| Shadow | `--shadow-{size}` | `--shadow-sm` |

---

## 7. Mau Sac

### 7.1 Bang Mau Chinh

| Vai tro | Mau | Hex | Cach dung |
| --- | --- | --- | --- |
| Chu dao | Green | `#247a52` | Button chinh, link active, icon chinh, focus brand |
| Chu dao hover | Deep Green | `#1b5e40` | Hover/pressed primary |
| Phu | Slate Blue | `#3f6f86` | Secondary highlight, dashboard chart, thong tin phu |
| Accent | Warm Gold | `#c28a35` | Gia sale, promotion, badge dac biet, diem nhan thuong hieu |
| Nen trang | Green-tinted neutral | `#f4f6f1` | Body background light mode |
| Nen card | White | `#ffffff` | Card, form, modal, dropdown |
| Text manh | Charcoal | `#1f2933` | Heading, gia, text chinh quan trong |
| Text mac dinh | Deep Olive Gray | `#344034` | Body text |

### 7.2 Ti Le Su Dung Mau

| Vai tro | Ti le khuyen nghi | Ghi chu |
| --- | --- | --- |
| Neutral/surface | 65-75% | Nen, card, form, table de san pham noi bat |
| Brand primary | 10-15% | CTA, navigation active, focus, selected state |
| Secondary | 5-10% | Dashboard/chart/info, khong tranh vai tro voi primary |
| Accent | 5-8% | Gia sale, promotion, highlight, khong dung lam nen lon |
| Feedback | 2-5% | Chi dung khi co trang thai nghiep vu |

### 7.3 Quy Tac Mau Theo Nghiep Vu

| Thanh phan | Mau khuyen nghi | Ly do |
| --- | --- | --- |
| Nut mua hang/chinh | `--brand-primary` | Hanh dong tin cay, on dinh |
| Nut xoa/huy nguy hiem | `--danger` | Canh bao dung chuan |
| Gia goc | `--text-subtle` + gach ngang | Phan biet voi gia hien tai |
| Gia khuyen mai | `--sale` hoac mau sale rieng | Noi bat uu dai |
| Rating star | `--rating` | Mau sao quen thuoc voi e-commerce |
| Trang thai thanh cong | `--success`, `--success-bg` | Don hang thanh cong, luu thong tin |
| Trang thai canh bao | `--warning`, `--warning-bg` | Thieu thong tin, sap het hang |
| Trang thai loi | `--danger`, `--danger-bg` | Validate form, loi thanh toan |

### 7.4 Mau Khong Nen Dung Tuy Tien

| Mau | Ly do |
| --- | --- |
| Do tuoi `#ff0000` | Qua gay gat, chi dung neu da map thanh token sale/danger co kiem soat |
| Tim gradient mac dinh | Khong phu hop visual direction dac san/nong san hien tai |
| Xanh neon | Giam cam giac tin cay va kho doc |
| Den tuyet doi cho nen lon | Gay moi mat, nen dung dark surface co sac xanh/xam |

### 7.5 Tuong Phan Mau

Yeu cau toi thieu:

| Loai text | Ti le tuong phan toi thieu | Ghi chu |
| --- | --- | --- |
| Body text nho | 4.5:1 | Ap dung cho text 12-18px thong thuong |
| Heading lon | 3:1 | Neu font >= 24px hoac bold lon |
| Icon co y nghia | 3:1 | Icon button, status icon |
| Border focus | Phai nhin thay ro | Nen co focus ring ngoai border |

---

## 8. Typography

### 8.1 Font Family

Font hien tai trong `Base.css`:

```css
html {
    font-family: 'Google Sans Flex', -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
}
```

Quy chuan:

| Vai tro | Font | Ghi chu |
| --- | --- | --- |
| Sans/body | `Google Sans Flex`, `Segoe UI`, Arial, sans-serif | Dung cho toan bo website |
| Display | `Google Sans Flex`, `Segoe UI`, Arial, sans-serif | Dung heading/banner; co the nang weight thay vi doi font |
| Mono | `JetBrains Mono`, `Consolas`, monospace | Ma don hang, ID, log, admin technical fields |

Neu `Google Sans Flex` khong duoc load tu external font, browser se fallback sang `Segoe UI`. Neu can on dinh visual hon, nen them link font vao `base.jsp` hoac self-host font.

### 8.2 Thang Kich Thuoc Chu

| Token | Size | Dung cho |
| --- | --- | --- |
| `--text-xs` | 12px | Badge, label nho, metadata phu |
| `--text-sm` | 14px | Body nho, table, form label, nav top |
| `--text-md` | 16px | Body mac dinh, product name |
| `--text-lg` | 18px | Card title, section subtitle |
| `--text-xl` | 20px | Section heading nho |
| `--text-2xl` | 24px | Page title, admin heading |
| `--text-3xl` | 32px | Hero title desktop |
| `--text-4xl` | 40px | Landing hero lon neu can |

### 8.3 Type Scale Theo Thanh Phan

| Thanh phan | Desktop | Tablet | Mobile | Weight | Line-height |
| --- | --- | --- | --- | --- | --- |
| Hero title | 40px | 32px | 28px | 700-800 | 1.15 |
| Page title | 32px | 28px | 24px | 700 | 1.2 |
| Section title | 24px | 22px | 20px | 700 | 1.25 |
| Card title | 18px | 18px | 16px | 600-700 | 1.35 |
| Body | 16px | 16px | 15-16px | 400 | 1.6 |
| Body small | 14px | 14px | 14px | 400 | 1.5 |
| Button | 14-15px | 14-15px | 14px | 600 | 1.2 |
| Table | 14px | 14px | 13-14px | 400-600 | 1.45 |
| Price | 17-24px | 16-22px | 16-20px | 700-800 | 1.2 |

### 8.4 Quy Tac Typography

| Ma | Quy tac |
| --- | --- |
| TY-01 | Moi trang chi nen co mot `h1` co y nghia |
| TY-02 | Khong bo qua cap heading vi style; neu can style khac, dung class |
| TY-03 | Body text khong nho hon 14px tren desktop va mobile |
| TY-04 | Line-height body toi thieu 1.5, doan dai nen 1.6-1.7 |
| TY-05 | Text in hoa chi dung cho label ngan, badge, table heading hoac footer heading |
| TY-06 | Khong can giua doan van dai; chi can giua heading ngan hoac empty state |

---

## 9. Khoang Cach, Luoi Va Bo Cuc

### 9.1 Spacing Scale

Su dung scale 4px de dam bao nhat quan:

| Token | Gia tri | Dung cho |
| --- | --- | --- |
| `--space-1` | 4px | Gap icon-text nho, badge internal |
| `--space-2` | 8px | Gap form nho, list item nho |
| `--space-3` | 12px | Padding card compact, product card body |
| `--space-4` | 16px | Padding card co ban, form group margin |
| `--space-5` | 20px | Section header gap, pagination margin |
| `--space-6` | 24px | Grid gap tablet, block spacing |
| `--space-8` | 32px | Section spacing nho, page block |
| `--space-10` | 40px | Container horizontal desktop, section spacing |
| `--space-12` | 48px | Footer/page major spacing |
| `--space-16` | 64px | Hero/landing section spacing |

### 9.2 Container

Container hien tai:

| Class | Max width | Padding desktop | Padding tablet | Padding mobile |
| --- | --- | --- | --- | --- |
| `.main-container` | 1400px | 40px | 20-24px | 16px khuyen nghi |
| `.layout-shell` | 1400px | 40px | 24px | 16px khuyen nghi |

Quy tac:

| Ma | Quy tac |
| --- | --- |
| LA-01 | Trang public va account nen dung `.main-container` hoac `.layout-shell` de can voi header/footer |
| LA-02 | Khong dat container moi max-width khac 1400px neu khong co ly do |
| LA-03 | Page admin co the dung container full-width nhung padding phai theo spacing scale |
| LA-04 | Tren mobile, padding ngang toi thieu 16px de noi dung khong sat canh man hinh |

### 9.3 Grid San Pham

Grid hien tai trong `Base.css`:

| Breakpoint | Cot |
| --- | --- |
| > 992px | 4 cot |
| <= 992px | 3 cot |
| <= 768px | 2 cot |
| <= 600px | 1 cot |

Quy chuan moi khuyen nghi:

| Width | Cot | Gap |
| --- | --- | --- |
| >= 1200px | 4 | 22-24px |
| 992-1199px | 3 | 20-22px |
| 600-991px | 2 | 16-20px |
| < 600px | 1 | 14-16px |

### 9.4 Admin Layout

Admin nen uu tien density cao hon public site nhung khong hy sinh kha dung:

| Thanh phan | Gia tri khuyen nghi |
| --- | --- |
| Sidebar width | 240-280px desktop |
| Topbar height | 56-64px |
| Page padding | 24px desktop, 16px tablet/mobile |
| Card gap | 16-24px |
| Table cell padding | 10-14px |
| Form field height | 40-44px |

### 9.5 Bo Goc Va Shadow

| Token | Gia tri | Dung cho |
| --- | --- | --- |
| `--radius-sm` | 6px | Input, badge, small button |
| `--radius-md` | 8px | Card, dropdown, table wrapper |
| `--radius-lg` | 12px | CTA, major card, auth panel |
| `--shadow-sm` | `0 3px 10px rgba(...)` | Card nhe |
| `--shadow-md` | `0 8px 24px rgba(...)` | Dropdown, hover card |
| `--shadow-lg` | `0 24px 70px rgba(...)` | Mega menu, modal, overlay |

Quy tac shadow:

| Ma | Quy tac |
| --- | --- |
| SH-01 | Shadow khong thay the border; card nen co border nhe de ro tren light/dark |
| SH-02 | Dropdown/modal can shadow lon hon card de the hien elevation |
| SH-03 | Table admin khong nen co shadow qua manh, uu tien border va background |

---

## 10. Thanh Phan Giao Dien

### 10.1 Button

Button hien co trong `Component.css`:

| Class | Vai tro | Token nen dung |
| --- | --- | --- |
| `.btn` | Base button | Font, padding, radius, transition |
| `.btn-primary` | Hanh dong chinh | `--brand-primary`, `--brand-primary-hover` |
| `.btn-outline` | Hanh dong phu | `--border-default`, `--surface-default` |
| `.btn-danger` | Hanh dong nguy hiem | Nen map ve `--danger`, khong dung `--color-sale` cho xoa |
| `.btn-small` | Button nho | Padding 6px 12px, 13px |

Quy chuan kich thuoc:

| Size | Height | Padding | Font |
| --- | --- | --- | --- |
| Small | 32-34px | 6px 12px | 13px |
| Medium | 40-42px | 10px 20px | 14px |
| Large | 46-48px | 12px 24-28px | 15-16px |

State bat buoc:

| State | Yeu cau |
| --- | --- |
| Default | Mau ro, text co tuong phan |
| Hover | Doi mau nen/border nhe, khong chi doi shadow |
| Focus | Co `box-shadow: var(--focus-ring)` hoac outline tuong duong |
| Active | Nen dam hon hoac transform nhe |
| Disabled | Opacity 0.55-0.65, cursor `not-allowed`, khong hover |
| Loading | Giu kich thuoc button, co spinner/text dang xu ly |

### 10.2 Card

| Class | Vai tro |
| --- | --- |
| `.card` | Container noi dung co border/shadow nhe |
| `.card-header` | Vung tieu de |
| `.card-body` | Noi dung chinh |
| `.card-soft` | Card nen nhe |
| `.card-bordered` | Card co border ro hon |

Quy tac:

| Ma | Quy tac |
| --- | --- |
| CA-01 | Product card phai co media ratio dong nhat, khuyen nghi 1:1 |
| CA-02 | Card admin khong nen animate translate manh, tranh lam roi khi thao tac bang table |
| CA-03 | Card co link toan bo phai co `focus-within` state |
| CA-04 | Card khong duoc chua qua nhieu mau nen khac nhau |

### 10.3 Product Card

Thanh phan product card hien co gom media, badge, body, name, price, meta, rating.

Quy chuan:

| Thanh phan | Yeu cau |
| --- | --- |
| Anh | Ratio 1:1, object-fit cover, background fallback `--surface-muted` |
| Ten san pham | 2 dong toi da, line-height 1.4, min-height on dinh |
| Gia hien tai | Dam, ro, khong bi nho hon 16px |
| Gia cu | Mau subdued, gach ngang |
| Badge sale | Noi bat nhung khong che anh qua nhieu |
| Rating | Sao dung `--rating`; review count dung `--text-subtle` |
| Hover | Shadow nhe, translate toi da -2px |

### 10.4 Form

Form hien co trong `Component.css`: `.form-group`, label, input, textarea, select.

Quy chuan:

| Thanh phan | Yeu cau |
| --- | --- |
| Label | 14px, text secondary, dat tren input |
| Input height | 40-44px |
| Padding input | 10px 12px |
| Border | `--border-default`; focus `--brand-primary` + focus ring |
| Error | Border `--danger`, message `--danger`, background error nhe neu can |
| Help text | 13-14px, `--text-subtle` |
| Required mark | Dung `*` mau danger va co giai thich neu form phuc tap |

Validation state:

| State | Mau/bieu hien |
| --- | --- |
| Normal | Border default |
| Focus | Border primary + focus ring |
| Success | Border success, message ngan neu can |
| Error | Border danger, message cu the, khong chi dung mau |
| Disabled | Background muted, text subtle, cursor not-allowed |

### 10.5 Table

Dung cho admin, don hang, roles, customers.

| Thanh phan | Gia tri khuyen nghi |
| --- | --- |
| Font | 14px |
| Header | Nen `--surface-muted`, text secondary/strong, weight 600 |
| Cell padding | 12px desktop, 10px tablet |
| Row hover | `--surface-muted` hoac token hover rieng |
| Border | `--border-subtle` |
| Empty state | Text ro + hanh dong tiep theo neu co |

Responsive table:

| Chieu rong | Cach xu ly |
| --- | --- |
| >= 768px | Table day du |
| < 768px | Cho phep horizontal scroll hoac chuyen thanh card list |
| < 520px | An cot phu, giu cot ten/trang thai/hanh dong chinh |

### 10.6 Header

Header hien tai gom top bar, main header, logo, menu category, search, account, dropdown.

Quy chuan:

| Thanh phan | Yeu cau |
| --- | --- |
| Sticky header | Giu `position: sticky`, z-index theo token |
| Header top | Co the an tren <= 820px nhu hien tai |
| Search | Tren mobile full width va order sau logo/menu |
| Dropdown | Co border, shadow, animation translate nhe |
| Logo | Chieu cao desktop 42px, mobile co the 36-40px |
| Menu target | Button/icon toi thieu 38x38px desktop, 44x44px mobile |

### 10.7 Footer

Footer hien tai co grid 4 cot desktop, 2 cot tablet, 1 cot mobile.

Quy chuan:

| Thanh phan | Yeu cau |
| --- | --- |
| Footer background | `--surface-muted` trong light, dark surface trong dark |
| Heading | 15px, uppercase, weight 800 |
| Link | Text soft, hover primary |
| Social icon | 36x36px toi thieu |
| Payment/transport icon | 52x42px, object-fit contain |

### 10.8 Badge Va Status

| Loai | Mau nen | Text | Dung cho |
| --- | --- | --- | --- |
| Primary | `--brand-primary` | White | Active, selected |
| Muted | `--surface-muted` | `--text-soft` | Metadata, default |
| Success | `--success-bg` | `--success` | Don thanh cong, active user |
| Warning | `--warning-bg` | `--warning` | Cho xu ly, thieu thong tin |
| Danger | `--danger-bg` | `--danger` | Bi khoa, loi, huy |
| Accent | `--brand-accent-soft` | `--brand-accent-hover` | Khuyen mai, best seller |

### 10.9 Modal/Dialog

Neu bo sung modal:

| Hang muc | Yeu cau |
| --- | --- |
| Overlay | Nen den alpha 40-55%, blur nhe neu can |
| Dialog width | 480-640px cho form; 720-960px cho content lon |
| Mobile | Full width tru padding 16px, khong tran man hinh |
| Focus | Trap focus trong modal, Escape dong neu khong nguy hiem |
| Action | Primary ben phai, cancel/secondary ben trai hoac truoc primary |

### 10.10 Toast/Alert

| Loai | Vi tri | Thoi gian | Noi dung |
| --- | --- | --- | --- |
| Success toast | Top-right desktop, bottom mobile | 3-5 giay | Da luu, da them vao gio |
| Error alert | Gan noi phat sinh loi | Den khi user xu ly | Loi form/thanh toan |
| Warning alert | Gan context | Den khi user xu ly | Thieu dia chi, het hang |

---

## 11. Responsive Design

### 11.1 Breakpoint Chuan

Hien tai code dung cac moc 1200, 992, 900, 820, 768, 640, 620, 600, 576, 520. De thong nhat, chuan hoa thanh:

| Token/ten | Width | Thiet bi muc tieu | Ghi chu |
| --- | --- | --- | --- |
| `xs` | < 576px | Mobile nho | 360-575px |
| `sm` | >= 576px | Mobile lon | 576-767px |
| `md` | >= 768px | Tablet doc | 768-991px |
| `lg` | >= 992px | Tablet ngang/laptop nho | 992-1199px |
| `xl` | >= 1200px | Desktop | 1200-1399px |
| `2xl` | >= 1400px | Desktop rong | Container max 1400px |

CSS hien tai theo max-width van chap nhan duoc. Khi viet moi, uu tien:

```css
@media (max-width: 992px) { }
@media (max-width: 768px) { }
@media (max-width: 576px) { }
```

### 11.2 Quy Tac Mobile

| Ma | Quy tac |
| --- | --- |
| RE-01 | Viewport toi thieu can test: 360px |
| RE-02 | Touch target toi thieu 44x44px cho nut, icon action, menu |
| RE-03 | Khong dung hover lam cach duy nhat de xem thong tin quan trong |
| RE-04 | Form checkout tren mobile phai mot cot |
| RE-05 | Header top co the an, search phai van de truy cap |
| RE-06 | Table admin phai horizontal scroll hoac card layout, khong ep chu qua nho |
| RE-07 | Bottom spacing phai du de nut fixed/sticky khong che noi dung |

### 11.3 Quy Tac Tablet

| Khu vuc | Xu ly |
| --- | --- |
| Product grid | 2-3 cot tuy width |
| Header | Search full width neu khong du cho logo/account |
| Checkout | 1 cot hoac 2 cot voi summary sticky neu du width |
| Account | Sidebar co the chuyen len top hoac thanh tab ngang |
| Admin | Sidebar co the collapse, table scroll ngang |

### 11.4 Quy Tac Desktop

| Khu vuc | Xu ly |
| --- | --- |
| Container | Max 1400px, padding 40px |
| Product grid | 4 cot default; co the 5 cot chi khi anh va text van ro |
| Footer | 4 cot |
| Admin | Sidebar + content, dashboard card grid 3-4 cot |
| Detail product | 2 cot: media va thong tin mua hang |

### 11.5 Responsive QA Matrix

Can test cac width sau:

| Width | Muc dich |
| --- | --- |
| 360px | Mobile nho, edge case quan trong |
| 390px | Mobile pho bien |
| 576px | Mobile lon/small tablet |
| 768px | Tablet doc |
| 992px | Breakpoint layout desktop nho |
| 1200px | Desktop chuan |
| 1440px | Desktop rong, kiem tra container max |

---

## 12. Light Mode Va Dark Mode

### 12.1 Nguyen Tac Theme

Theme phai duoc dieu khien qua token. Component khong nen viet:

```css
.card { background: #ffffff; color: #111111; }
```

Nen viet:

```css
.card { background: var(--surface-default); color: var(--text-default); }
```

### 12.2 Light Mode Baseline

Light mode hien co:

| Token | Gia tri |
| --- | --- |
| `--surface-page` | `#f4f6f1` |
| `--surface-default` | `#ffffff` |
| `--surface-muted` | `#f7f9f4` |
| `--text-strong` | `#1f2933` |
| `--text-default` | `#344034` |
| `--border-default` | `#d6ddd2` |
| `--brand-primary` | `#247a52` |
| `--brand-accent` | `#c28a35` |

### 12.3 Dark Mode De Xuat

Dark mode can giu nhan dien xanh la/vang nau nhung nen giam saturation de do choi mau khong qua gat tren nen toi.

```css
:root[data-theme="dark"],
[data-theme="dark"] {
    --brand-primary: #58c98d;
    --brand-primary-hover: #7addaa;
    --brand-primary-soft: rgba(88, 201, 141, 0.14);
    --brand-secondary: #8fb8ca;
    --brand-accent: #e0ad5c;
    --brand-accent-hover: #f0c778;
    --brand-accent-soft: rgba(224, 173, 92, 0.16);

    --surface-page: #101614;
    --surface-default: #18211d;
    --surface-muted: #202b26;
    --surface-subtle: #151c19;
    --surface-inverse: #f4f6f1;

    --text-strong: #f3f7f1;
    --text-default: #d7e0d4;
    --text-soft: #aebcad;
    --text-subtle: #839181;
    --text-on-primary: #08110d;

    --border-subtle: #26342e;
    --border-default: #34463d;
    --border-strong: #4b6256;
    --focus-ring: 0 0 0 3px rgba(88, 201, 141, 0.24);

    --success: #7ae3a7;
    --success-bg: rgba(122, 227, 167, 0.13);
    --success-border: rgba(122, 227, 167, 0.36);
    --warning: #ffc46b;
    --warning-bg: rgba(255, 196, 107, 0.14);
    --warning-border: rgba(255, 196, 107, 0.36);
    --danger: #ff8a80;
    --danger-bg: rgba(255, 138, 128, 0.14);
    --danger-border: rgba(255, 138, 128, 0.36);

    --price: #f3f7f1;
    --sale: #e0ad5c;
    --rating: #ffc857;

    --bg-main: var(--surface-page);
    --bg-white: var(--surface-default);
    --bg-light: var(--surface-muted);
    --text-main: var(--text-default);
    --text-heading: var(--text-strong);
    --text-secondary: var(--text-soft);
    --text-muted: var(--text-subtle);
    --color-primary: var(--brand-primary);
    --color-primary-dark: var(--brand-primary-hover);
    --color-accent: var(--brand-accent);
    --color-price: var(--price);
    --color-sale: var(--sale);
    --border-light: var(--border-subtle);
    --border-normal: var(--border-default);

    --shadow-sm: 0 3px 10px rgba(0, 0, 0, 0.24);
    --shadow-md: 0 8px 24px rgba(0, 0, 0, 0.32);
    --shadow-lg: 0 24px 70px rgba(0, 0, 0, 0.46);
}
```

### 12.4 Cach Bat Theme

Cach khuyen nghi:

```html
<html lang="vi" data-theme="light">
```

Khi user chon dark mode:

```html
<html lang="vi" data-theme="dark">
```

Neu muon theo he dieu hanh khi chua co setting:

```css
@media (prefers-color-scheme: dark) {
    :root:not([data-theme="light"]) {
        /* dark tokens */
    }
}
```

Thu tu uu tien:

1. Lua chon cua user luu trong profile/localStorage.
2. Neu chua co, theo `prefers-color-scheme` cua browser.
3. Neu browser khong ho tro, mac dinh light mode.

### 12.5 Dark Mode Checklist

| Hang muc | Yeu cau |
| --- | --- |
| Body | Khong con nen trang hard-code |
| Card/dropdown/modal | Dung `--surface-default` |
| Text | Tat ca text dung token, khong con `#111`, `#333`, `#777` neu khong can |
| Border | Dung token border, khong qua sang tren nen toi |
| Anh san pham | Anh giu nguyen, container co nen muted |
| Logo | Logo phai doc duoc tren header dark; neu can co phien ban logo dark |
| Shadow | Tang opacity so voi light, nhung khong lam lem |
| Form | Input background, border, placeholder, focus dat tuong phan |

---

## 13. Trang Thai, Tuong Tac Va Chuyen Dong

### 13.1 Interaction State Bat Buoc

| Thanh phan | Default | Hover | Focus | Active | Disabled |
| --- | --- | --- | --- | --- | --- |
| Link | Text token | Primary | Focus ring/underline | Dam hon | Muted |
| Button | Theo variant | Nen/border doi | Focus ring | Pressed | Opacity + not-allowed |
| Input | Border default | Border strong nhe | Border primary + ring | N/A | Muted |
| Card link | Border/shadow nhe | Translate -2px | Focus-within ring | N/A | N/A |
| Menu item | Transparent | Surface muted | Focus background | Selected primary-soft | Muted |

### 13.2 Animation

Quy chuan:

| Loai | Duration | Easing | Dung cho |
| --- | --- | --- | --- |
| Micro interaction | 120-180ms | ease-out | Button hover, input focus |
| Dropdown/menu | 180-250ms | ease | Header dropdown, user menu |
| Card hover | 180-220ms | ease | Product card lift |
| Page reveal | 240-360ms | ease-out | Neu co animation load section |
| Modal | 180-260ms | ease-out | Dialog appear |

Khong nen:

| Ma | Quy tac |
| --- | --- |
| AN-01 | Khong animate width/height lon neu gay layout shift |
| AN-02 | Khong dung transform scale > 1.05 cho button thuong mai |
| AN-03 | Khong animate lien tuc trong form checkout/admin table |
| AN-04 | Ton trong `prefers-reduced-motion` |

CSS khuyen nghi:

```css
@media (prefers-reduced-motion: reduce) {
    *,
    *::before,
    *::after {
        animation-duration: 0.01ms !important;
        animation-iteration-count: 1 !important;
        scroll-behavior: auto !important;
        transition-duration: 0.01ms !important;
    }
}
```

### 13.3 Loading State

| Noi dung | Pattern |
| --- | --- |
| Them vao gio | Button loading ngan, sau do toast success |
| Tim kiem autocomplete | Skeleton/dropdown loading nho |
| Table admin | Skeleton rows hoac spinner trong table body |
| Checkout submit | Disable form action, hien text dang xu ly |
| Upload avatar/banner | Progress hoac spinner + preview |

### 13.4 Empty State

Empty state can co:

| Thanh phan | Yeu cau |
| --- | --- |
| Tieu de | Noi ro khong co du lieu gi |
| Mo ta | Giai thich ngan gon |
| Hanh dong | CTA neu co next step |
| Icon/illustration | Tuy chon, khong thay the text |

Vi du:

```text
Chua co san pham trong gio hang
Hay kham pha cac dac san noi bat va them san pham ban yeu thich.
[Bat dau mua sam]
```

---

## 14. Hinh Anh, Icon Va Media

### 14.1 Anh San Pham

| Hang muc | Yeu cau |
| --- | --- |
| Ratio card | 1:1 |
| Detail gallery | 1:1 hoac 4:5, khong crop mat san pham quan trong |
| Object fit | `cover` cho card, `contain` neu anh co nen trang va san pham can hien day du |
| Placeholder | Nen muted + icon/ten san pham neu anh loi |
| Lazy loading | Dung `loading="lazy"` cho anh ngoai viewport |
| Alt text | Mo ta san pham, khong de rong neu anh co y nghia |

### 14.2 Banner

| Hang muc | Desktop | Mobile |
| --- | --- | --- |
| Hero/banner ratio | 16:5 hoac 3:1 | 4:3 hoac 16:10 |
| Text tren banner | Co overlay dam | Nen tach text ra ngoai neu anh qua phuc tap |
| CTA | Nam trong vung an toan | Khong sat mep, target 44px |

### 14.3 Icon

Du an dang dung Font Awesome. Quy chuan:

| Loai icon | Size | Mau |
| --- | --- | --- |
| Inline icon | 14-16px | Theo text |
| Button icon | 16-18px | Theo button |
| Feature icon | 20-28px | Primary/accent |
| Empty state icon | 40-64px | Muted/accent soft |

Icon co y nghia phai co text hoac `aria-label` tren button.

### 14.4 Logo

| Hang muc | Gia tri |
| --- | --- |
| Header desktop | 42px height hien tai |
| Footer | 48px height hien tai |
| Mobile | 36-40px neu header chat |
| Dark mode | Kiem tra logo tren nen dark; neu logo khong doc duoc, tao logo variant |

---

## 15. Accessibility Va Usability

### 15.1 Yeu Cau Accessibility Toi Thieu

| Ma | Yeu cau | Cach kiem tra |
| --- | --- | --- |
| A11Y-01 | Moi input co label | Inspect DOM, click label focus input |
| A11Y-02 | Focus keyboard thay ro | Tab qua header, form, modal, button |
| A11Y-03 | Link/nut co ten truy cap | Screen reader/name inspection |
| A11Y-04 | Tuong phan text dat 4.5:1 | Dung contrast checker |
| A11Y-05 | Khong chi dung mau de bao loi | Error co text/message/icon |
| A11Y-06 | Heading dung thu tu logic | H1 -> H2 -> H3 khong nhay cap tuy tien |
| A11Y-07 | Target mobile >= 44x44px | Kiem bang devtools |
| A11Y-08 | Co `lang="vi"` | Da co trong `base.jsp` |
| A11Y-09 | Anh co alt phu hop | Anh san pham/banner/icon |
| A11Y-10 | Modal/dropdown keyboard usable | Tab, Escape, Enter |

### 15.2 Keyboard Navigation

Thu tu tab khuyen nghi:

1. Skip link neu bo sung.
2. Header navigation.
3. Search.
4. Account/cart actions.
5. Main content.
6. Form controls/product actions.
7. Footer links.

Nen bo sung skip link:

```html
<a class="skip-link" href="#main-content">Bo qua den noi dung chinh</a>
```

CSS:

```css
.skip-link {
    position: absolute;
    left: 16px;
    top: -48px;
    z-index: var(--z-toast);
    padding: 10px 14px;
    background: var(--brand-primary);
    color: var(--text-on-primary);
    border-radius: var(--radius-sm);
}

.skip-link:focus {
    top: 16px;
}
```

### 15.3 Form Usability

| Hang muc | Quy tac |
| --- | --- |
| Loi validate | Hien gan field, noi ro cach sua |
| Placeholder | Khong thay label |
| Password | Co nut hien/an neu can |
| Checkout | Group thong tin nguoi nhan, dia chi, van chuyen, thanh toan ro rang |
| Dia chi | Select tinh/huyen/xa co loading/error state |
| Submit | Disable khi dang xu ly de tranh double submit |

### 15.4 Usability Cho E-commerce

| Khu vuc | Quy tac |
| --- | --- |
| Product listing | Gia va CTA phai de quet mat |
| Filter/search | Ket qua thay doi phai co feedback |
| Cart | So luong, tong tien, phi ship, khuyen mai ro rang |
| Checkout | Tong tien cuoi cung phai noi bat, khong an phi phu |
| Order status | Trang thai co mau + text, khong chi icon |
| Admin | Hanh dong nguy hiem can confirm va phan biet ro |

---

## 16. Quy Uoc CSS/JSP

### 16.1 Nguyen Tac CSS

| Ma | Quy tac |
| --- | --- |
| CSS-01 | CSS global chi nam trong `theme.css`, `Base.css`, `Component.css`, `layout/*` |
| CSS-02 | Page CSS chi style trong scope cua page de tranh leak |
| CSS-03 | Uu tien class thay vi selector theo tag sau trong page CSS |
| CSS-04 | Khong dung `!important` tru khi override plugin/external hoac legacy bat buoc |
| CSS-05 | Khong duplicate `.btn`, `.card`, `.table` trong nhieu file neu co the mo rong variant |
| CSS-06 | Dung token thay cho hex hard-code |
| CSS-07 | Comment ngan gon cho block CSS phuc tap, khong comment thua |

### 16.2 Naming Convention

Khuyen nghi dung BEM cho component moi:

```css
.product-card {}
.product-card__media {}
.product-card__body {}
.product-card__name {}
.product-card--featured {}
```

Quy tac:

| Loai | Mau ten | Vi du |
| --- | --- | --- |
| Component | `.block` | `.order-summary` |
| Element | `.block__element` | `.order-summary__total` |
| Modifier | `.block--modifier` | `.order-summary--compact` |
| Utility | `.u-*` neu bo sung | `.u-text-center` |
| State | `.is-*`, `.has-*` | `.is-active`, `.has-error` |

### 16.3 JSP Va HTML Semantics

| Hang muc | Quy tac |
| --- | --- |
| Layout | Dung `base.jsp` cho trang moi neu co the |
| Main | Noi dung chinh nam trong `<main id="main-content">` |
| Form | Dung `<label for>` va id input tuong ung |
| Button | Hanh dong JS dung `<button type="button">`, submit form dung `<button type="submit">` |
| Link | Dieu huong dung `<a href>`; khong dung div click |
| Table | Table du lieu dung `<table>`, co `<thead>`, `<tbody>` |
| Currency | Gia hien thi dong nhat theo VND |

### 16.4 Quan Ly Hard-code Hien Tai

Trong code hien tai van co mot so mau hard-code trong `Base.css` product card nhu `#ffffff`, `#e5e7eb`, `#ff1f00`, `#888888`, `#777777`. Ke hoach refactor:

| Hard-code | Token thay the khuyen nghi |
| --- | --- |
| `#ffffff` | `--surface-default` |
| `#e5e7eb` | `--border-subtle` hoac `--border-default` |
| `#ff1f00` | Tao token `--sale-strong` hoac dung `--danger` neu la loi, `--sale` neu la gia sale |
| `#888888`, `#777777` | `--text-subtle` |
| `#111111`, `#1f2933` | `--text-strong` |

Khong can sua toan bo ngay lap tuc, nhung moi code moi phai dung token.

---

## 17. Kiem Thu Giao Dien

### 17.1 Kiem Thu Trinh Duyet

| Trinh duyet | Muc tieu |
| --- | --- |
| Chrome latest | Chinh |
| Edge latest | Chinh |
| Firefox latest | Phu |
| Safari/iOS Safari | Neu co thiet bi, uu tien checkout/mobile |

### 17.2 Kiem Thu Responsive

Moi trang quan trong can test:

| Trang | 360 | 390 | 576 | 768 | 992 | 1200 | 1440 |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Home | Bat buoc | Bat buoc | Nen | Bat buoc | Nen | Bat buoc | Nen |
| Product listing | Bat buoc | Bat buoc | Nen | Bat buoc | Nen | Bat buoc | Nen |
| Product detail | Bat buoc | Bat buoc | Nen | Bat buoc | Nen | Bat buoc | Nen |
| Cart | Bat buoc | Bat buoc | Nen | Bat buoc | Nen | Bat buoc | Nen |
| Checkout | Bat buoc | Bat buoc | Nen | Bat buoc | Nen | Bat buoc | Nen |
| Account profile | Bat buoc | Nen | Nen | Bat buoc | Nen | Bat buoc | Nen |
| Admin dashboard | Nen | Nen | Nen | Bat buoc | Bat buoc | Bat buoc | Nen |
| Admin table pages | Nen | Nen | Nen | Bat buoc | Bat buoc | Bat buoc | Nen |

### 17.3 Kiem Thu Theme

| Test | Yeu cau |
| --- | --- |
| Light mode | Khong loi token, mau dung baseline |
| Dark mode | Tat ca surface/text/border/form doc duoc |
| System preference | Neu chua co user setting, browser dark preference duoc ton trong |
| Toggle theme | Khong reload mat state form neu co JS toggle |
| Logo/image | Khong bi chim tren nen dark |

### 17.4 Kiem Thu Accessibility

| Test | Cach lam |
| --- | --- |
| Keyboard | Dung Tab/Shift+Tab/Enter/Escape khong dung chuot |
| Focus | Moi focus state thay ro |
| Contrast | Kiem mau text, button, badge |
| Screen reader basic | Kiem ten nut icon, label input |
| Reduced motion | Bat setting reduce motion va kiem animation |

### 17.5 Kiem Thu Visual Regression Thu Cong

Truoc khi merge thay doi UI:

1. Chup man hinh trang truoc/sau o desktop va mobile.
2. Kiem tra header/footer khong lech container.
3. Kiem tra product card khong vo line clamp/gia.
4. Kiem tra form focus/error/success.
5. Kiem tra table admin khi data dai.
6. Kiem tra dark mode neu CSS lien quan token.

---

## 18. Quan Ly Thay Doi Design System

### 18.1 Quy Trinh Them Token Moi

1. Xac dinh token co dung lai it nhat 2 noi khong.
2. Dat ten theo vai tro, khong theo mau vat ly neu la semantic token.
3. Them vao `theme.css` light mode.
4. Them gia tri dark mode neu dark mode da kich hoat.
5. Cap nhat tai lieu nay neu token anh huong design system.
6. Kiem tra cac trang chinh.

### 18.2 Quy Trinh Them Component Moi

| Buoc | Noi dung |
| --- | --- |
| 1 | Kiem tra `Component.css` da co pattern tuong tu chua |
| 2 | Dinh nghia vai tro component va states |
| 3 | Viet CSS bang token, dat ten BEM |
| 4 | Test responsive va keyboard |
| 5 | Bo sung vao tai lieu neu component dung lai nhieu trang |

### 18.3 Review Checklist Cho Pull Request UI

| Cau hoi | Dat/Khong |
| --- | --- |
| Co dung token thay vi hard-code mau/spacing khong? | |
| Co hover/focus/disabled/loading state cho action khong? | |
| Co responsive tai 360/768/1200px khong? | |
| Co anh huong header/footer/global class khong? | |
| Co pass contrast va keyboard basic khong? | |
| Co phu hop visual direction xanh la/vang nau/neutral khong? | |
| Co can cap nhat tai lieu design system khong? | |

### 18.4 Phan Cap Quyet Dinh

| Loai thay doi | Ai nen review |
| --- | --- |
| Doi mau brand/token chinh | Tech lead + product/design |
| Doi spacing/container global | Tech lead + frontend owner |
| Them component dung chung | Frontend owner |
| Page-specific CSS nho | Developer + reviewer |
| Dark mode token | Frontend owner + QA |

---

## 19. Checklist Nghiem Thu

### 19.1 Checklist Tong Quan

| Ma | Tieu chi | Dat |
| --- | --- | --- |
| AC-01 | Trang dung font family chuan | |
| AC-02 | Mau chinh/phu/accent dung token | |
| AC-03 | Heading/body text dung type scale | |
| AC-04 | Padding/margin theo spacing scale | |
| AC-05 | Container can voi header/footer | |
| AC-06 | Button co hover/focus/disabled | |
| AC-07 | Form co label, focus, error state | |
| AC-08 | Card/table khong hard-code mau moi | |
| AC-09 | Responsive pass 360/768/1200px | |
| AC-10 | Dark mode khong bi nen trang/text den hard-code | |
| AC-11 | Contrast dat toi thieu | |
| AC-12 | Keyboard navigation dung | |
| AC-13 | Anh co alt va khong vo layout | |
| AC-14 | Loading/empty/error state co noi dung ro | |

### 19.2 Checklist Trang Public

| Khu vuc | Tieu chi |
| --- | --- |
| Header | Sticky, search dung, dropdown khong bi che, mobile dung |
| Home | Banner khong vo, section spacing deu, CTA ro |
| Product listing | Grid dung cot, filter/search usable, pagination ro |
| Product detail | Anh, gia, CTA, so luong, review hierarchy ro |
| Cart | Tong tien, quantity controls, remove action ro |
| Checkout | Form mot cot mobile, tong tien noi bat, submit state an toan |
| Footer | Link/icon can deu, mobile 1 cot, text doc duoc |

### 19.3 Checklist Trang Account/Admin

| Khu vuc | Tieu chi |
| --- | --- |
| Account sidebar | Mobile khong che noi dung, active state ro |
| Profile form | Validate ro, upload avatar co state |
| Orders | Trang thai don hang ro, table/card responsive |
| Admin sidebar/topbar | Navigation active, collapse/mobile dung |
| Admin dashboard | Card metric can deu, chart/table khong tran |
| Admin table | Scroll/card mobile, action nguy hiem co confirm |
| Roles/settings | Form phan quyen ro, checkbox/toggle target du lon |

---

## 20. Phu Luc

### 20.1 Mau `theme.css` Mo Rong Khuyen Nghi

Doan duoi day la baseline co the ap dung trong mot lan refactor rieng. Khong nen paste neu chua test toan bo UI.

```css
:root {
    --space-0: 0;
    --space-1: 4px;
    --space-2: 8px;
    --space-3: 12px;
    --space-4: 16px;
    --space-5: 20px;
    --space-6: 24px;
    --space-8: 32px;
    --space-10: 40px;
    --space-12: 48px;
    --space-16: 64px;

    --font-sans: 'Google Sans Flex', -apple-system, BlinkMacSystemFont, 'Segoe UI', Arial, sans-serif;
    --font-display: var(--font-sans);
    --font-mono: 'JetBrains Mono', 'Consolas', monospace;

    --text-xs: 12px;
    --text-sm: 14px;
    --text-md: 16px;
    --text-lg: 18px;
    --text-xl: 20px;
    --text-2xl: 24px;
    --text-3xl: 32px;
    --text-4xl: 40px;

    --line-tight: 1.2;
    --line-normal: 1.5;
    --line-relaxed: 1.65;

    --container-max: 1400px;
    --container-padding-desktop: 40px;
    --container-padding-tablet: 24px;
    --container-padding-mobile: 16px;

    --z-header: 5100;
    --z-dropdown: 5300;
    --z-modal: 7000;
    --z-toast: 8000;
}
```

### 20.2 Mau CSS Component Theo Token

```css
.btn:focus-visible,
.form-group input:focus-visible,
.form-group textarea:focus-visible,
.form-group select:focus-visible,
.product-card:focus-within {
    outline: none;
    box-shadow: var(--focus-ring);
}

.btn:disabled,
.btn.is-disabled {
    opacity: 0.6;
    cursor: not-allowed;
    pointer-events: none;
}

.badge-success {
    background: var(--success-bg);
    border: 1px solid var(--success-border);
    color: var(--success);
}

.badge-warning {
    background: var(--warning-bg);
    border: 1px solid var(--warning-border);
    color: var(--warning);
}

.badge-danger {
    background: var(--danger-bg);
    border: 1px solid var(--danger-border);
    color: var(--danger);
}
```

### 20.3 Ma Hoa Trang Thai Don Hang

| Trang thai | Token nen dung | Mo ta UI |
| --- | --- | --- |
| Cho xac nhan | `--warning` | Badge vang, text ro |
| Dang xu ly | `--brand-secondary` | Badge info |
| Dang giao | `--brand-primary` | Badge primary |
| Hoan thanh | `--success` | Badge success |
| Da huy | `--danger` | Badge danger |
| Hoan tien | `--brand-accent` | Badge accent |

### 20.4 Mau HTML Cho Form Chuan

```html
<div class="form-group has-error">
    <label for="receiverName">Ho va ten nguoi nhan <span aria-hidden="true">*</span></label>
    <input
        id="receiverName"
        name="receiverName"
        type="text"
        autocomplete="name"
        aria-describedby="receiverName-error"
        aria-invalid="true">
    <p id="receiverName-error" class="form-error">Vui long nhap ho va ten nguoi nhan.</p>
</div>
```

### 20.5 Cac Viec Nen Lam Tiep Theo

| Uu tien | Cong viec | Ly do |
| --- | --- | --- |
| Cao | Bo sung spacing/type/z-index token vao `theme.css` | Giam hard-code, tang consistency |
| Cao | Them dark mode token va co che `data-theme` | Hoan thien yeu cau dark/light mode |
| Cao | Refactor hard-code mau trong product card | Product card la component xuat hien nhieu |
| Trung binh | Them focus-visible va disabled state global | Tang accessibility |
| Trung binh | Chuan hoa breakpoint trong CSS moi | Giam phan manh responsive |
| Trung binh | Them skip link vao `base.jsp` | Cai thien keyboard navigation |
| Thap | Tao visual regression screenshot checklist | Ho tro QA thu cong |

---

## Ket Luan

Tai lieu nay thiet lap baseline design system cho Souvenir E-commerce theo cau truc IEEE-style: co pham vi, muc tieu, quy tac, token, component, responsive, theme, accessibility, testing va governance. Khi phat trien UI moi, developer phai uu tien dung `theme.css`, `Base.css`, `Component.css` va cac quy chuan trong tai lieu nay de dam bao giao dien nhat quan, chuyen nghiep va de bao tri.
