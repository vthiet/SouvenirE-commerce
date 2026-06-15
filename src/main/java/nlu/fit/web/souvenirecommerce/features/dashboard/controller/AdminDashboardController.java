package nlu.fit.web.souvenirecommerce.features.dashboard.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.features.dashboard.dto.DashboardMetricsDTO;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.features.dashboard.service.AdminDashboardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@WebServlet(name = "AdminDashboardController", urlPatterns = {"/admin/dashboard"})
public class AdminDashboardController extends HttpServlet {

    private static final Logger log = LoggerFactory.getLogger(AdminDashboardController.class);
    private AdminDashboardService dashboardService;

    @Override
    public void init() {
        dashboardService = new AdminDashboardService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        try {
            log.info("Loading admin dashboard metrics");
            DashboardMetricsDTO metrics = dashboardService.buildDashboardMetrics();

            request.setAttribute("totalProducts", metrics.getTotalProducts());
            request.setAttribute("totalCustomers", metrics.getTotalCustomers());
            request.setAttribute("totalRevenue", metrics.getTotalRevenue());
            request.setAttribute("totalOrders", metrics.getTotalOrders());
            request.setAttribute("topProducts", metrics.getTopProducts());
            request.setAttribute("recentOrders", metrics.getRecentOrders());
            request.setAttribute("monthlyRevenues", metrics.getMonthlyRevenue());
            request.setAttribute("dashboardMetrics", metrics);

            request.getRequestDispatcher("/admin/dashboard.jsp").forward(request, response);
            log.info("Admin dashboard loaded successfully");

        } catch (Exception e) {
            log.error("Failed to load admin dashboard", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error loading dashboard");
        }
    }
}
