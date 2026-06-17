package nlu.fit.web.souvenirecommerce.features.dashboard.dto;

import nlu.fit.web.souvenirecommerce.model.entity.Order;
import nlu.fit.web.souvenirecommerce.model.entity.Product;

import java.util.List;
import java.util.Map;

public class DashboardMetricsDTO {

    private int totalProducts;
    private int totalCustomers;
    private double totalRevenue;
    private int totalOrders;
    private List<Double> monthlyRevenue;
    private List<Integer> monthlyOrders;
    private Map<String, Integer> orderStatusCounts;
    private List<Product> topProducts;
    private List<Order> recentOrders;
    private List<LowStockProductDTO> lowStockProducts;
    private int newCustomersLast7Days;
    private List<CategorySalesDTO> topCategoriesBySales;

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getTotalCustomers() {
        return totalCustomers;
    }

    public void setTotalCustomers(int totalCustomers) {
        this.totalCustomers = totalCustomers;
    }

    public double getTotalRevenue() {
        return totalRevenue;
    }

    public void setTotalRevenue(double totalRevenue) {
        this.totalRevenue = totalRevenue;
    }

    public int getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(int totalOrders) {
        this.totalOrders = totalOrders;
    }

    public List<Double> getMonthlyRevenue() {
        return monthlyRevenue;
    }

    public void setMonthlyRevenue(List<Double> monthlyRevenue) {
        this.monthlyRevenue = monthlyRevenue;
    }

    public List<Integer> getMonthlyOrders() {
        return monthlyOrders;
    }

    public void setMonthlyOrders(List<Integer> monthlyOrders) {
        this.monthlyOrders = monthlyOrders;
    }

    public Map<String, Integer> getOrderStatusCounts() {
        return orderStatusCounts;
    }

    public void setOrderStatusCounts(Map<String, Integer> orderStatusCounts) {
        this.orderStatusCounts = orderStatusCounts;
    }

    public List<Product> getTopProducts() {
        return topProducts;
    }

    public void setTopProducts(List<Product> topProducts) {
        this.topProducts = topProducts;
    }

    public List<Order> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<Order> recentOrders) {
        this.recentOrders = recentOrders;
    }

    public List<LowStockProductDTO> getLowStockProducts() {
        return lowStockProducts;
    }

    public void setLowStockProducts(List<LowStockProductDTO> lowStockProducts) {
        this.lowStockProducts = lowStockProducts;
    }

    public int getNewCustomersLast7Days() {
        return newCustomersLast7Days;
    }

    public void setNewCustomersLast7Days(int newCustomersLast7Days) {
        this.newCustomersLast7Days = newCustomersLast7Days;
    }

    public List<CategorySalesDTO> getTopCategoriesBySales() {
        return topCategoriesBySales;
    }

    public void setTopCategoriesBySales(List<CategorySalesDTO> topCategoriesBySales) {
        this.topCategoriesBySales = topCategoriesBySales;
    }
}
