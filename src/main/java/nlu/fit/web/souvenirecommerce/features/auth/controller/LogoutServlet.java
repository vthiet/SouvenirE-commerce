package nlu.fit.web.souvenirecommerce.features.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;

@Slf4j
@WebServlet(urlPatterns = {"/logout"})
public class LogoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Object currentUser = session.getAttribute("currentUser");
            if (currentUser instanceof User user) {
                log.info("Đăng xuất: userId={}, email={}", user.getId(), user.getEmail());
                AuditLogService.success(
                        LogoutServlet.class,
                        user,
                        "AUTH",
                        "LOGOUT",
                        "SESSION",
                        AuditLogService.describe("sessionId", session.getId())
                );
            } else {
                log.info("Đăng xuất: sessionId={}", session.getId());
            }
            session.invalidate();
        } else {
            log.info("Đăng xuất: không có session");
        }

        resp.sendRedirect(req.getContextPath() + "/home");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doGet(req, resp);
    }
}
