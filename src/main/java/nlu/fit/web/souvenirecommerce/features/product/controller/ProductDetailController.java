package nlu.fit.web.souvenirecommerce.features.product.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.order.repository.OrderRepository;
import nlu.fit.web.souvenirecommerce.features.product.dto.ProductDetailDTO;
import nlu.fit.web.souvenirecommerce.features.product.service.ProductService;
import nlu.fit.web.souvenirecommerce.features.product.service.ReviewService;
import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProvider;
import nlu.fit.web.souvenirecommerce.features.shipping.ShippingProviderRegistry;
import nlu.fit.web.souvenirecommerce.features.user.address.AddressService;
import nlu.fit.web.souvenirecommerce.model.entity.Address;

import java.io.IOException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Optional;

@WebServlet("/product")
public class ProductDetailController extends HttpServlet {

    private ProductService productService;
    private ReviewService reviewService;
    private AddressService addressService;
    private OrderRepository orderRepository;

    @Override
    public void init() {
        productService = new ProductService();
        reviewService = new ReviewService();
        addressService = new AddressService();
        orderRepository = new OrderRepository();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        Long productId;

        try {
            productId = Long.parseLong(request.getParameter("id"));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        ProductDetailDTO dto = productService.getProductDetail(productId);

        if (dto == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        request.setAttribute("data", dto);

        Integer currentUserId = getCurrentUserId(request);

        boolean isLoggedIn = currentUserId != null;
        boolean canReview = false;

        if (isLoggedIn) {
            canReview = reviewService.canReview(currentUserId, productId);
        }

        request.setAttribute("isLoggedIn", isLoggedIn);
        request.setAttribute("canReview", canReview);
        prepareShippingEstimate(request, currentUserId);

        request.setAttribute("headerMode", "BREADCRUMB");
        request.setAttribute("breadcrumbCategory", dto.getCategory());
        request.setAttribute("breadcrumbProduct", dto.getProduct());

        request.setAttribute("pageTitle", dto.getProduct().getName());
        request.setAttribute("contentPage", "/WEB-INF/views/product/detail.jsp");
        request.setAttribute("pageCss", "ProductDetail.css");
        request.setAttribute("pageJs", "ProductDetail.js");

        request.getRequestDispatcher("/WEB-INF/layout/base.jsp").forward(request, response);
    }

    private Integer getCurrentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        String[] commonKeys = {"user", "currentUser", "authUser", "userInSession", "loggedInUser", "account"};

        for (String key : commonKeys) {
            Integer userId = extractUserId(session.getAttribute(key));

            if (userId != null) {
                return userId;
            }
        }

        Enumeration<String> names = session.getAttributeNames();

        while (names.hasMoreElements()) {
            String name = names.nextElement();
            Integer userId = extractUserId(session.getAttribute(name));

            if (userId != null) {
                return userId;
            }
        }

        return null;
    }

    private Integer extractUserId(Object userObj) {
        if (userObj == null) {
            return null;
        }

        try {
            Method getIdMethod = userObj.getClass().getMethod("getId");
            Object id = getIdMethod.invoke(userObj);

            if (id instanceof Number) {
                return ((Number) id).intValue();
            }

            if (id instanceof String) {
                return Integer.parseInt((String) id);
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private void prepareShippingEstimate(HttpServletRequest request, Integer currentUserId) {
        if (currentUserId == null) {
            return;
        }

        try {
            Address address = resolvePreferredShippingAddress(currentUserId.longValue()).orElse(null);
            if (address == null) {
                return;
            }

            request.setAttribute("shippingAddressText", formatAddress(address));

            Integer districtId = address.getEffectiveGhnDistrictId();
            String wardCode = address.getEffectiveGhnWardCode();
            if (districtId == null || wardCode == null || wardCode.isBlank()) {
                return;
            }

            BigDecimal shippingFee = ghnService.calculateFee(districtId, wardCode);
            request.setAttribute("shippingFeeAmount", shippingFee);
        } catch (Exception ignored) {
            request.setAttribute("shippingFeeUnavailable", true);
        }
    }

    private Optional<Address> resolvePreferredShippingAddress(Long userId) {
        Optional<Address> latestOrderAddress = orderRepository.findLatestShippingAddressByUserId(userId);
        if (latestOrderAddress.isPresent()) {
            return latestOrderAddress;
        }

        List<Address> addresses = addressService.getUserAddresses(userId);
        if (addresses == null || addresses.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(addresses.get(0));
    }

    private String formatAddress(Address address) {
        List<String> parts = new ArrayList<>();
        addPart(parts, address.getAddressDetail());
        addPart(parts, address.getEffectiveWardName());
        addPart(parts, address.getEffectiveDistrictName());
        addPart(parts, address.getEffectiveProvinceName());
        return String.join(", ", parts);
    }

    private void addPart(List<String> parts, String value) {
        if (value != null && !value.isBlank()) {
            parts.add(value.trim());
        }
    }
}
