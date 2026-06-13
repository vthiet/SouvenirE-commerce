package nlu.fit.web.souvenirecommerce.features.product.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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

        ProductTypeDTO dto = productTypeService.getSearchProductType(
                keyword,
                parseInteger(request.getParameter("minPrice")),
                parseInteger(request.getParameter("maxPrice")),
                parseInteger(request.getParameter("rating")),
                parseSort(request.getParameter("sort")),
                parseInteger(request.getParameter("page"), 1)
        );

        if (dto == null) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }

        request.setAttribute("headerMode", "BREADCRUMB");
        request.setAttribute("breadcrumbLabel", "Tìm kiếm");
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
