package nlu.fit.web.souvenirecommerce.features.product.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;
import nlu.fit.web.souvenirecommerce.features.product.dto.ProductTypeDTO;
import nlu.fit.web.souvenirecommerce.features.product.service.ProductTypeService;
import nlu.fit.web.souvenirecommerce.model.enums.ProductSort;

import java.io.IOException;

@WebServlet("/search")
public class SearchController extends HttpServlet {

    private ProductTypeService productTypeService;

    @Override
    public void init() {
        productTypeService = new ProductTypeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String keyword = request.getParameter("keyword");
        if (keyword == null || keyword.trim().isEmpty()) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        PriceBounds priceBounds = resolvePriceBounds(request);

        ProductTypeDTO dto = productTypeService.getSearchProductType(
                keyword,
                priceBounds.minPrice(),
                priceBounds.maxPrice(),
                parseInteger(request.getParameter("rating")),
                parseSort(request.getParameter("sort")),
                parseInteger(request.getParameter("page"), 1)
        );

        if (dto == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        request.setAttribute("headerMode", "BREADCRUMB");
        request.setAttribute("breadcrumbLabel", I18nUtil.message(request, "productType.search_breadcrumb"));
        request.setAttribute("data", dto);

        if (isAjaxRequest(request)) {
            response.setContentType("text/html;charset=UTF-8");
            request.getRequestDispatcher("/WEB-INF/views/product/product-type-results.jsp")
                    .forward(request, response);
            return;
        }

        request.setAttribute("pageTitle", dto.getCategory().getCategoryName());
        request.setAttribute("contentPage", "/productType.jsp");
        request.setAttribute("pageCss", "PTypeMain.css");
        request.setAttribute("pageJs", "ProductType.js");

        request.getRequestDispatcher("WEB-INF/layout/base.jsp")
                .forward(request, response);
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private int parseInteger(String value, int defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private ProductSort parseSort(String value) {
        if (value == null || value.isBlank()) {
            return ProductSort.POPULAR;
        }

        try {
            return ProductSort.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return ProductSort.POPULAR;
        }
    }

    private PriceBounds resolvePriceBounds(HttpServletRequest request) {
        Integer minPrice = parseInteger(request.getParameter("minPrice"));
        Integer maxPrice = parseInteger(request.getParameter("maxPrice"));

        if (minPrice != null || maxPrice != null) {
            return new PriceBounds(minPrice, maxPrice);
        }

        return switch (String.valueOf(request.getParameter("priceRange"))) {
            case "under-100" -> new PriceBounds(null, 100000);
            case "100-300" -> new PriceBounds(100000, 300000);
            case "300-500" -> new PriceBounds(300000, 500000);
            case "over-500" -> new PriceBounds(500000, null);
            default -> new PriceBounds(null, null);
        };
    }

    private boolean isAjaxRequest(HttpServletRequest request) {
        return "true".equalsIgnoreCase(request.getParameter("ajax"))
                || "XMLHttpRequest".equalsIgnoreCase(request.getHeader("X-Requested-With"));
    }

    private record PriceBounds(Integer minPrice, Integer maxPrice) {
    }
}
