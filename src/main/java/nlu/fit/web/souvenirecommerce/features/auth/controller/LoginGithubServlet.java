package nlu.fit.web.souvenirecommerce.features.auth.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import nlu.fit.web.souvenirecommerce.common.utils.HibernateUtil;
import nlu.fit.web.souvenirecommerce.features.auth.service.AuthService;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartEntity;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartService;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import org.hibernate.Transaction;

import java.io.IOException;

@WebServlet("/login-github")
@Slf4j
public class LoginGithubServlet extends HttpServlet {
    private AuthService authService;
    private CartService cartService;

    @Override
    public void init() throws ServletException {
        authService = new AuthService();
        cartService = new CartService();
    }

    @Override
    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String code = req.getParameter("code");

        try {
            User user = authService.loginWithGithub(code);
            HttpSession session = req.getSession(false);
            Object redirectAfterLogin = session == null ? null : session.getAttribute("redirectAfterLogin");
            Object cartAttribute = session == null ? null : session.getAttribute("cart");
            CartEntity preLoginCart = cartAttribute instanceof CartEntity cart ? cart : null;
            if (session != null) {
                session.invalidate();
            }
            session = req.getSession(true);
            session.setAttribute("userDto", user);
            session.setAttribute("currentUser", user);
            session.setAttribute("userInSession", user);
            session.setAttribute("user", user);
            session.setAttribute("authUser", user);
            cartService.prepareGuestCartMerge(session, preLoginCart);
            CartEntity cart = cartService.getCartForDisplay(session);
            cartService.storeCart(session, cart);
            if (redirectAfterLogin instanceof String redirect && !redirect.isBlank()) {
                resp.sendRedirect(redirect);
                return;
            }
            resp.sendRedirect(req.getContextPath() + "/home");
        } catch (Exception e) {
            rollbackCurrentTransaction();
            log.warn("Đăng nhập GitHub thất bại", e);
            req.getSession(true).setAttribute("error", "Đăng nhập GitHub thất bại.");
            resp.sendRedirect(req.getContextPath() + "/login");
        }
    }

    private void rollbackCurrentTransaction() {
        try {
            Transaction transaction = HibernateUtil.getSessionFactory().getCurrentSession().getTransaction();
            if (transaction != null && transaction.isActive()) {
                transaction.rollback();
            }
        } catch (RuntimeException rollbackError) {
            log.warn("Không thể rollback transaction đăng nhập GitHub", rollbackError);
        }
    }
}
