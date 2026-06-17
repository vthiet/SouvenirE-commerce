package nlu.fit.web.souvenirecommerce.features.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.google.gson.JsonObject;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;
import nlu.fit.web.souvenirecommerce.features.auth.service.AuthService;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/api/check-email", "/api/signup/check-email", "/api/login/check-email"})
public class CheckEmailServlet extends HttpServlet {
    private AuthService authService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        req.setCharacterEncoding("UTF-8");

        String email = normalizeEmail(req.getParameter("email"));
        PrintWriter out = resp.getWriter();
        JsonObject jsonResponse = new JsonObject();

        if (email == null || email.trim().isEmpty()) {
            writeJson(out, jsonResponse, "error", I18nUtil.message(req, "auth.server.check_email.empty"));
            return;
        }

        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            writeJson(out, jsonResponse, "error", I18nUtil.message(req, "auth.server.check_email.invalid"));
            return;
        }

        boolean exists = authService.hasEmailExist(email);

        if (exists) {
            writeJson(out, jsonResponse, "error", I18nUtil.message(req, "auth.server.check_email.exists"));
        } else {
            jsonResponse.addProperty("status", "success");
            jsonResponse.addProperty("message", I18nUtil.message(req, "auth.server.check_email.available"));
            jsonResponse.addProperty("email", email);
            out.print(jsonResponse.toString());
        }
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }

    private void writeJson(PrintWriter out, JsonObject jsonResponse, String status, String message) {
        jsonResponse.addProperty("status", status);
        jsonResponse.addProperty("message", message);
        out.print(jsonResponse.toString());
    }
}
