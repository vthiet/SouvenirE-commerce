package nlu.fit.web.souvenirecommerce.core.filters;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.cart.model.Cart;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartPersistenceService;
import nlu.fit.web.souvenirecommerce.features.product.service.ICategoryService;
import nlu.fit.web.souvenirecommerce.features.product.service.impl.CategoryServiceImpl;
import nlu.fit.web.souvenirecommerce.legacy.dao.SettingsDAO;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;
import java.util.Collections;

public class HeaderFilter implements Filter {

    private ICategoryService categoryService;
    private SettingsDAO settingsDAO;
    private CartPersistenceService cartPersistenceService;

    @Override
    public void init(FilterConfig filterConfig) {
        this.categoryService = new CategoryServiceImpl();
        this.settingsDAO = new SettingsDAO();
        this.cartPersistenceService = new CartPersistenceService();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession(false);

        if (shouldSkip(req)) {
            chain.doFilter(request, response);
            return;
        }

        setAuthUser(request, session);
        setHeaderCategories(request);
        setCartItemCount(request, session);
        setSiteSettings(request);

        chain.doFilter(request, response);
    }

    private boolean shouldSkip(HttpServletRequest request) {
        String uri = request.getRequestURI();

        return uri.contains("/assets/") || uri.endsWith(".css") || uri.endsWith(".js") || uri.endsWith(".png") || uri.endsWith(".jpg") || uri.endsWith(".jpeg") || uri.endsWith(".webp") || uri.endsWith(".svg") || uri.endsWith(".ico") || uri.endsWith(".woff") || uri.endsWith(".woff2") || uri.endsWith(".ttf");
    }

    private void setAuthUser(ServletRequest request, HttpSession session) {
        if (session == null) {
            return;
        }

        Object authUser = session.getAttribute("authUser");

        if (authUser == null) {
            authUser = session.getAttribute("userInSession");
        }

        if (authUser != null) {
            request.setAttribute("authUser", authUser);
        }
    }

    private void setHeaderCategories(ServletRequest request) {
        try {
            request.setAttribute("headerCategories", categoryService.getHeaderCategories());

            request.setAttribute("headerTopCategories", categoryService.getTopSellingHeaderCategories(5));
        } catch (RuntimeException e) {
            e.printStackTrace();

            request.setAttribute("headerCategories", Collections.emptyList());

            request.setAttribute("headerTopCategories", Collections.emptyList());
        }
    }

    private void setCartItemCount(ServletRequest request, HttpSession session) {
        if (session == null) {
            request.setAttribute("cartItemCount", 0);
            return;
        }

        Object cartItemCount = session.getAttribute("cartItemCount");

        if (cartItemCount != null) {
            request.setAttribute("cartItemCount", cartItemCount);
            return;
        }

        Cart cart = (Cart) session.getAttribute("cart");

        if (cart != null) {
            request.setAttribute("cartItemCount", cart.totalQuantity());
            return;
        }

        User user = getCurrentUser(session);

        if (user != null) {
            cart = cartPersistenceService.loadCart(user);
            session.setAttribute("cart", cart);
            session.setAttribute("cartItemCount", cart.totalQuantity());
            request.setAttribute("cartItemCount", cart.totalQuantity());
            return;
        }

        request.setAttribute("cartItemCount", 0);
    }

    private User getCurrentUser(HttpSession session) {
        Object user = session.getAttribute("userInSession");

        if (user instanceof User currentUser) {
            return currentUser;
        }

        user = session.getAttribute("authUser");

        return user instanceof User currentUser ? currentUser : null;
    }

    private void setSiteSettings(ServletRequest request) {
        try {
            request.setAttribute("settings", settingsDAO.getAllSettings());
        } catch (Exception e) {
            e.printStackTrace();
            request.setAttribute("settings", Collections.emptyMap());
        }
    }

    @Override
    public void destroy() {
    }
}
