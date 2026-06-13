package nlu.fit.web.souvenirecommerce.features.product.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.features.product.dto.ProductTypeDTO;
import nlu.fit.web.souvenirecommerce.model.enums.ProductSort;
import nlu.fit.web.souvenirecommerce.features.product.service.ProductTypeService;

import java.io.IOException;

@WebServlet("/category")
public class ProductTypeController extends HttpServlet {

    private ProductTypeService productTypeService;

    @Override
    public void init() {
        productTypeService = new ProductTypeService();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        Long categoryId;

        try {
            categoryId = Long.parseLong(request.getParameter("id"));
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        Integer minPrice = parseInteger(request.getParameter("minPrice"));
        Integer maxPrice = parseInteger(request.getParameter("maxPrice"));
        Integer rating = parseInteger(request.getParameter("rating"));
        ProductSort sort = parseSort(request.getParameter("sort"));
        int page = parseInteger(request.getParameter("page"), 1);

        ProductTypeDTO dto = productTypeService.getProductType(
                categoryId,
                minPrice,
                maxPrice,
                rating,
                sort,
                page
        );

        if (dto == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        request.setAttribute("headerMode", "BREADCRUMB");
        request.setAttribute("breadcrumbCategory", dto.getCategory());

        request.setAttribute("data", dto);

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
}