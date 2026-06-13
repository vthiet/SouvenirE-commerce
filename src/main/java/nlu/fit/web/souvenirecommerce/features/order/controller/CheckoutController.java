package nlu.fit.web.souvenirecommerce.features.order.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.cart.model.Cart;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItem;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartPriceService;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartPersistenceService;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutException;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutRequest;
import nlu.fit.web.souvenirecommerce.features.order.dto.CheckoutResult;
import nlu.fit.web.souvenirecommerce.features.order.dto.PaymentContext;
import nlu.fit.web.souvenirecommerce.features.order.service.CheckoutService;
import nlu.fit.web.souvenirecommerce.features.payment.VnPayUtil;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.model.enums.PaymentMethod;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

@WebServlet("/checkout")
public class CheckoutController extends HttpServlet {
    private final CheckoutService checkoutService = new CheckoutService();
    private final CartPersistenceService cartPersistenceService = new CartPersistenceService();
    private final CartPriceService cartPriceService = new CartPriceService();

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

        Cart cart = getCart(session);
        Cart checkoutCart = buildCheckoutCart(cart, parseSelectedProductIds(request));
        if (checkoutCart.totalQuantity() == 0) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        refreshCartPrices(checkoutCart);
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

        Cart cart = getCart(session);
        Set<Long> selectedProductIds = parseSelectedProductIds(request);
        if (selectedProductIds.isEmpty()) {
            selectedProductIds = getRememberedSelectedProductIds(session);
        }

        Cart checkoutCart = buildCheckoutCart(cart, selectedProductIds);
        if (checkoutCart.totalQuantity() == 0) {
            response.sendRedirect(request.getContextPath() + "/cart");
            return;
        }
        refreshCartPrices(checkoutCart);

        try {
            CheckoutResult result = checkoutService.checkout(
                    user,
                    checkoutCart,
                    buildCheckoutRequest(request),
                    buildPaymentContext(request));
            removeSelectedItems(cart, checkoutCart);
            session.setAttribute("cart", cart);
            session.setAttribute("cartItemCount", cart.totalQuantity());
            cartPersistenceService.saveCart(user, cart);
            session.removeAttribute("checkoutSelectedProductIds");
            session.setAttribute("lastOrderCode", result.getOrderCode());
            session.setAttribute("lastOrderId", result.getOrder().getId());

            if (result.requiresExternalPayment()) {
                response.sendRedirect(result.getPaymentUrl());
                return;
            }
            response.sendRedirect(request.getContextPath() + "/order-success");
        } catch (CheckoutException e) {
            request.setAttribute("error", e.getMessage());
            prepareCheckoutHeader(request);
            prepareCheckoutPage(request, user, checkoutCart);
            request.getRequestDispatcher("/WEB-INF/layout/base.jsp").forward(request, response);
        }
    }

    private void prepareCheckoutHeader(HttpServletRequest request) {
        request.setAttribute("headerMode", "CHECKOUT_FLOW");
        request.setAttribute("checkoutStep", "CHECKOUT");
        request.setAttribute("pageTitle", "Thanh toán - INOLA");
        request.setAttribute("pageCss", "Payment.css");
        request.setAttribute("pageJs", "payment.js");
        request.setAttribute("enableSelect2", true);
        request.setAttribute("contentPage", "/checkout.jsp");
    }

    private void prepareCheckoutPage(HttpServletRequest request, User user, Cart checkoutCart) {
        request.setAttribute("currentUser", user);
        request.setAttribute("authUser", user);
        request.setAttribute("cart", checkoutCart);
        request.setAttribute("checkoutProductIds", productIdsOf(checkoutCart));
        request.setAttribute("savedAddresses", checkoutService.getUserAddresses(user.getId()));
        request.setAttribute("provinceOptions", checkoutService.getProvinces());
        request.setAttribute("vnpayAvailable", checkoutService.isPaymentMethodAvailable(PaymentMethod.VNPAY_QR));
    }

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
                .note(request.getParameter("note"))
                .paymentMethod(parsePaymentMethod(request.getParameter("paymentMethod")))
                .build();
    }

    private PaymentMethod parsePaymentMethod(String value) {
        if (value == null || value.isBlank()) {
            return PaymentMethod.COD;
        }
        try {
            return PaymentMethod.valueOf(value);
        } catch (IllegalArgumentException e) {
            return PaymentMethod.COD;
        }
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Cart getCart(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object cart = session.getAttribute("cart");
        return cart instanceof Cart ? (Cart) cart : null;
    }

    private Cart buildCheckoutCart(Cart sourceCart, Set<Long> selectedProductIds) {
        Cart checkoutCart = new Cart();
        if (sourceCart == null || selectedProductIds == null || selectedProductIds.isEmpty()) {
            return checkoutCart;
        }

        for (Long productId : selectedProductIds) {
            CartItem item = sourceCart.getItem(productId);
            if (item != null && item.getProduct() != null && item.getQuantity() > 0) {
                checkoutCart.addItem(item.getProduct(), item.getQuantity(), item.getPrice());
            }
        }

        return checkoutCart;
    }

    private Set<Long> parseSelectedProductIds(HttpServletRequest request) {
        Set<Long> productIds = new LinkedHashSet<>();
        addProductIds(productIds, request.getParameter("items"));
        String[] selectedProductIdValues = request.getParameterValues("selectedProductId");
        if (selectedProductIdValues != null) {
            for (String value : selectedProductIdValues) {
                addProductIds(productIds, value);
            }
        }
        return productIds;
    }

    private void addProductIds(Set<Long> productIds, String rawValue) {
        if (rawValue == null || rawValue.isBlank()) {
            return;
        }

        for (String token : rawValue.split(",")) {
            try {
                productIds.add(Long.valueOf(token.trim()));
            } catch (NumberFormatException ignored) {
                // Ignore invalid ids from the browser and keep the checkout scoped to valid cart items.
            }
        }
    }

    private Set<Long> getRememberedSelectedProductIds(HttpSession session) {
        Set<Long> productIds = new LinkedHashSet<>();
        if (session == null) {
            return productIds;
        }

        Object value = session.getAttribute("checkoutSelectedProductIds");
        if (value instanceof Set<?> rememberedIds) {
            for (Object rememberedId : rememberedIds) {
                if (rememberedId instanceof Long productId) {
                    productIds.add(productId);
                }
            }
        }
        return productIds;
    }

    private void rememberSelectedProductIds(HttpSession session, Cart checkoutCart) {
        if (session != null) {
            session.setAttribute("checkoutSelectedProductIds", productIdsOf(checkoutCart));
        }
    }

    private Set<Long> productIdsOf(Cart cart) {
        Set<Long> productIds = new LinkedHashSet<>();
        if (cart == null) {
            return productIds;
        }

        for (CartItem item : cart.getItems()) {
            if (item.getProduct() != null && item.getProduct().getId() != null) {
                productIds.add(item.getProduct().getId());
            }
        }
        return productIds;
    }

    private void removeSelectedItems(Cart cart, Cart checkoutCart) {
        for (Long productId : productIdsOf(checkoutCart)) {
            cart.removeItem(productId);
        }
    }

    private User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("userInSession");
        if (user instanceof User) {
            return (User) user;
        }
        user = session.getAttribute("user");
        if (user instanceof User) {
            return (User) user;
        }
        user = session.getAttribute("currentUser");
        if (user instanceof User) {
            return (User) user;
        }
        user = session.getAttribute("authUser");
        return user instanceof User ? (User) user : null;
    }

    private void refreshCartPrices(Cart cart) {
        for (CartItem item : cart.getItems()) {
            item.setPrice(cartPriceService.getCurrentPrice(item.getProduct()));
        }
    }
}
