package nlu.fit.web.souvenirecommerce.features.page.home.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import nlu.fit.web.souvenirecommerce.features.product.dto.HomePageDTO;
import nlu.fit.web.souvenirecommerce.features.product.service.HomeService;

import java.io.IOException;

@WebServlet("/home")
public class HomeController extends HttpServlet {

    private HomeService homeService;

    @Override
    public void init() {
        homeService = new HomeService();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HomePageDTO dto = homeService.getHomePageData();

        request.setAttribute("data", dto);
        request.setAttribute("headerMode", "MENU_BAR");
        request.setAttribute("pageTitle", "Trang chủ");
        request.setAttribute("pageCss", "HomePageMain.css");
        request.setAttribute("pageJs", "HomePage.js");
        request.setAttribute("contentPage", "/WEB-INF/views/home/home.jsp");

        request.getRequestDispatcher("WEB-INF/layout/base.jsp").forward(request, response);
    }
}
