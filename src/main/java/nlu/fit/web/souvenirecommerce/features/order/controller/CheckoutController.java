package nlu.fit.web.souvenirecommerce.features.order.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItemEntity;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;
import nlu.fit.web.souvenirecommerce.features.order.exception.CheckoutException;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutRequest;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutResult;
import nlu.fit.web.souvenirecommerce.features.payment.dto.PaymentContext;
import nlu.fit.web.souvenirecommerce.features.order.service.CheckoutService;
import nlu.fit.web.souvenirecommerce.features.payment.model.VnPayUtil;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@WebServlet("/checkout")
public class CheckoutController extends HttpServlet {
    private final CheckoutService checkoutService = new CheckoutService();
    private final CartService cartService = new CartService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        User user = getCurrentUser(session);

        if (user == null) {
            HttpSession newSession = request.getSession();
            newSession.setAttribute("redirectAfterLogin", request.getContextPath() + "/checkout");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        CartEntity cart = cartService.getCartForDisplay(session);
        cartService.storeCart(session, cart);
        CartEntity checkoutCart = buildCheckoutCart(cart, parseSelectedProductIds(request));
        if (checkoutCart.totalQuantity() == 0) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        rememberSelectedProductIds(session, checkoutCart);

        prepareCheckoutHeader(request);
        prepareCheckoutPage(request, user, checkoutCart);
        request.getRequestDispatcher("/WEB-INF/layout/base.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);
        User user = getCurrentUser(session);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        CartEntity cart = cartService.getCartForDisplay(session);
        cartService.storeCart(session, cart);
        Set<Long> selectedProductIds = parseSelectedProductIds(request);
        if (selectedProductIds.isEmpty()) {
            selectedProductIds = getRememberedSelectedProductIds(session);
        }

        CartEntity checkoutCart = buildCheckoutCart(cart, selectedProductIds);
        if (checkoutCart.totalQuantity() == 0) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }

        try {
            CheckoutRequest checkoutRequest = buildCheckoutRequest(request);
            PaymentContext paymentContext = buildPaymentContext(request);
            CheckoutResult result = checkoutService.checkout(
                    user,
                    checkoutCart,
                    buildCheckoutRequest(request),
                    buildPaymentContext(request));
            removeSelectedItems(session, checkoutCart);
            session.removeAttribute("checkoutSelectedProductIds");
            session.setAttribute("lastOrderCode", result.getOrderCode());
            session.setAttribute("lastOrderId", result.getOrder().getId());

            if (result.requiresExternalPayment()) {
                response.sendRedirect(result.getPaymentUrl());
                return;
            }
            response.sendRedirect(request.getContextPath() + "/order-success");
        } catch (CheckoutException e) {
            AuditLogService.failure(
                    CheckoutController.class,
                    user,
                    "ORDER",
                    "CHECKOUT_FAILED",
                    "CHECKOUT",
                    AuditLogService.describe("reason", e.getMessage())
            );
            request.setAttribute("error", localizeCheckoutError(request, e.getMessage()));
            prepareCheckoutHeader(request);
            prepareCheckoutPage(request, user, checkoutCart);
            request.getRequestDispatcher("/WEB-INF/layout/base.jsp").forward(request, response);
        }
    }

    // ── Checkout cart helpers ─────────────────────────────────────────────────

    private CartEntity buildCheckoutCart(CartEntity sourceCart, Set<Long> selectedProductIds) {
        CartEntity checkoutCart = new CartEntity();
        if (sourceCart == null || selectedProductIds == null || selectedProductIds.isEmpty()) {
            return checkoutCart;
        }
        for (Long productId : selectedProductIds) {
            CartItemEntity item = sourceCart.getItem(productId);
            if (item != null && item.getProduct() != null && item.getQuantity() > 0) {
                checkoutCart.addItem(CartItemEntity.builder()
                        .product(item.getProduct())
                        .quantity(item.getQuantity())
                        .build());
            }
        }
        return checkoutCart;
    }

    private Set<Long> productIdsOf(CartEntity cart) {
        Set<Long> ids = new LinkedHashSet<>();
        if (cart == null) return ids;
        for (CartItemEntity item : cart.getItems()) {
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                ids.add(item.getProduct().getId());
            }
        }
        return ids;
    }

    private void rememberSelectedProductIds(HttpSession session, CartEntity checkoutCart) {
        if (session != null) {
            session.setAttribute("checkoutSelectedProductIds", productIdsOf(checkoutCart));
        }
    }

    private void removeSelectedItems(HttpSession session, CartEntity checkoutCart) {
        for (Long productId : productIdsOf(checkoutCart)) {
            cartService.removeItem(session, productId);
        }
        CartEntity cart = cartService.getCartForDisplay(session);
        cartService.storeCart(session, cart);
    }

    // ── Request parsing ───────────────────────────────────────────────────────

    private Set<Long> parseSelectedProductIds(HttpServletRequest request) {
        Set<Long> productIds = new LinkedHashSet<>();
        addProductIds(productIds, request.getParameter("items"));
        String[] values = request.getParameterValues("selectedProductId");
        if (values != null) {
            for (String v : values) addProductIds(productIds, v);
        }
        return productIds;
    }

    private void addProductIds(Set<Long> ids, String raw) {
        if (raw == null || raw.isBlank()) return;
        for (String token : raw.split(",")) {
            try { ids.add(Long.valueOf(token.trim())); } catch (NumberFormatException ignored) {
                // Ignore invalid ids from the browser and keep the checkout scoped to valid cart items.
            }
        }
    }

    private Set<Long> getRememberedSelectedProductIds(HttpSession session) {
        Set<Long> ids = new LinkedHashSet<>();
        if (session == null) return ids;
        Object value = session.getAttribute("checkoutSelectedProductIds");
        if (value instanceof Set<?> remembered) {
            for (Object id : remembered) {
                if (id instanceof Long productId) ids.add(productId);
            }
        }
        return ids;
    }

    // ── Page preparation ──────────────────────────────────────────────────────

    private void prepareCheckoutHeader(HttpServletRequest request) {
        request.setAttribute("headerMode", "CHECKOUT_FLOW");
        request.setAttribute("checkoutStep", "CHECKOUT");
        request.setAttribute("pageTitleKey", "checkout.page.title");
        request.setAttribute("pageTitle", I18nUtil.message(request, "checkout.page.title"));
        request.setAttribute("pageCss", "Payment.css");
        request.setAttribute("pageJs", "payment.js");
        request.setAttribute("enableSelect2", true);
        request.setAttribute("contentPage", "/checkout.jsp");
    }

    private void prepareCheckoutPage(HttpServletRequest request, User user, CartEntity checkoutCart) {
        request.setAttribute("currentUser", user);
        request.setAttribute("authUser", user);
        request.setAttribute("cart", checkoutCart);
        request.setAttribute("checkoutProductIds", productIdsOf(checkoutCart));
        request.setAttribute("savedAddresses", checkoutService.getUserAddresses(user.getId()));
        request.setAttribute("provinceOptions", checkoutService.getProvinces());
        request.setAttribute("vnpayAvailable", checkoutService.isPaymentMethodAvailable(PaymentMethod.VNPAY_QR));
        request.setAttribute("shippingProviders", nlu.fit.web.souvenirecommerce.features.shipping.ShippingProviderRegistry.all());
    }

    // ── Payment context ───────────────────────────────────────────────────────

    private PaymentContext buildPaymentContext(HttpServletRequest request) {
        return PaymentContext.builder()
                .clientIp(VnPayUtil.getClientIp(request))
                .returnUrl(buildReturnUrl(request))
                .build();
    }

    private String buildReturnUrl(HttpServletRequest request) {
        boolean defaultPort = ("http".equalsIgnoreCase(request.getScheme()) && request.getServerPort() == 80)
                || ("https".equalsIgnoreCase(request.getScheme()) && request.getServerPort() == 443);
        return request.getScheme() + "://" + request.getServerName()
                + (defaultPort ? "" : ":" + request.getServerPort())
                + request.getContextPath() + "/payment/vnpay-return";
    }

    private CheckoutRequest buildCheckoutRequest(HttpServletRequest request) {
        return CheckoutRequest.builder()
                .savedAddressId(parseLong(request.getParameter("savedAddressId")))
                .receiverName(request.getParameter("receiverName"))
                .receiverPhone(request.getParameter("receiverPhone"))
                .addressDetail(request.getParameter("addressDetail"))
                .provinceCode(parseInteger(request.getParameter("provinceCode")))
                .wardCode(parseInteger(request.getParameter("wardCode")))
                .carrierProvinceId(parseInteger(request.getParameter("carrierProvinceId")))
                .carrierDistrictId(parseInteger(request.getParameter("carrierDistrictId")))
                .carrierWardCode(request.getParameter("carrierWardCode"))
                .provinceName(request.getParameter("provinceName"))
                .districtName(request.getParameter("districtName"))
                .wardName(request.getParameter("wardName"))
                .shippingFee(parseDouble(request.getParameter("shippingFee")))
                .note(request.getParameter("note"))
                .paymentMethod(parsePaymentMethod(request.getParameter("paymentMethod")))
                .preferredCarrierCode(request.getParameter("preferredCarrierCode"))
                .build();
    }

    // ── Type parsing utilities ────────────────────────────────────────────────

    private PaymentMethod parsePaymentMethod(String value) {
        if (value == null || value.isBlank()) return PaymentMethod.COD;
        try { return PaymentMethod.valueOf(value); } catch (IllegalArgumentException e) { return PaymentMethod.COD; }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Integer.valueOf(value); } catch (NumberFormatException e) { return null; }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Long.valueOf(value); } catch (NumberFormatException e) { return null; }
    }

    private Double parseDouble(String value) {
        if (value == null || value.isBlank()) return null;
        try { return Double.valueOf(value); } catch (NumberFormatException e) { return null; }
    }

    private User getCurrentUser(HttpSession session) {
        if (session == null) return null;
        for (String key : new String[]{"userInSession", "user", "currentUser", "authUser"}) {
            Object u = session.getAttribute(key);
            if (u instanceof User user) return user;
        }
        return null;
    }

    private String localizeCheckoutError(HttpServletRequest request, String message) {
        if (message == null || message.isBlank()) {
            return I18nUtil.message(request, "checkout.error.generic");
        }

        return switch (message) {
            case "Phương thức thanh toán đã chọn hiện không khả dụng." ->
                    I18nUtil.message(request, "checkout.error.payment_unavailable");
            case "Sản phẩm không tồn tại" ->
                    I18nUtil.message(request, "checkout.error.product_missing");
            case "Không thể tạo đơn hàng" ->
                    I18nUtil.message(request, "checkout.error.order_create_failed");
            case "Địa chỉ giao hàng không hợp lệ" ->
                    I18nUtil.message(request, "checkout.error.address_invalid");
            case "Vui lòng nhập đầy đủ họ tên, số điện thoại và địa chỉ giao hàng" ->
                    I18nUtil.message(request, "checkout.error.address_required");
            case "Vui lòng chọn đầy đủ tỉnh/thành phố, quận/huyện, phường/xã và nhập địa chỉ chi tiết" ->
                    I18nUtil.message(request, "checkout.error.address_required_full");
            case "Vui lòng chọn đầy đủ tỉnh/thành phố, phường/xã và nhập địa chỉ chi tiết" ->
                    I18nUtil.message(request, "checkout.error.address_required_basic");
            case "Không thể tạo trạng thái đơn hàng" ->
                    I18nUtil.message(request, "checkout.error.status_create_failed");
            case "Vui lòng đăng nhập để đặt hàng" ->
                    I18nUtil.message(request, "checkout.error.login_required");
            case "Giỏ hàng đang trống" ->
                    I18nUtil.message(request, "checkout.error.cart_empty");
            case "Số lượng sản phẩm không hợp lệ" ->
                    I18nUtil.message(request, "checkout.error.quantity_invalid");
            case String stockMessage when stockMessage.startsWith("Sản phẩm ") && stockMessage.endsWith(" không đủ tồn kho") ->
                    I18nUtil.message(request, "checkout.error.stock_insufficient",
                            stockMessage.substring("Sản phẩm ".length(), stockMessage.length() - " không đủ tồn kho".length()));
            case "Phương thức thanh toán này chưa được hỗ trợ" ->
                    I18nUtil.message(request, "checkout.error.payment_gateway_unavailable");
            case "VNPay chưa được cấu hình. Vui lòng chọn COD hoặc liên hệ quản trị viên." ->
                    I18nUtil.message(request, "checkout.error.vnpay_unconfigured");
            case "Không thể tạo giao dịch VNPay cho đơn hàng chưa được lưu." ->
                    I18nUtil.message(request, "checkout.error.vnpay_unavailable");
            case "Không thể tạo phiên thanh toán VNPay. Vui lòng thử lại." ->
                    I18nUtil.message(request, "checkout.error.vnpay_session_failed");
            default -> I18nUtil.message(request, "checkout.error.generic");
        };
    }
}
