package nlu.fit.web.souvenirecommerce.features.cart.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.features.cart.model.Cart;
import nlu.fit.web.souvenirecommerce.features.cart.model.CartItem;
import nlu.fit.web.souvenirecommerce.features.cart.service.CartPersistenceService;
import nlu.fit.web.souvenirecommerce.model.entity.User;

import java.io.IOException;

@WebServlet("/cart/update")
public class UpdateCartController extends HttpServlet {
    private final CartPersistenceService cartPersistenceService = new CartPersistenceService();
    
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        try {
            Long productId = Long.parseLong(request.getParameter("productId"));
            int quantity  = Integer.parseInt(request.getParameter("quantity"));

            HttpSession session = request.getSession();
            Cart cart = (Cart) session.getAttribute("cart");
            
            if (cart == null) {
                response.getWriter().write("{\"success\":false}");
                return;
            }

            if (quantity <= 0) {
                cart.removeItem(productId);
            } else {
                cart.updateItem(productId, quantity);
            }
            
            session.setAttribute("cart", cart);
            session.setAttribute("cartItemCount", cart.totalQuantity());
            cartPersistenceService.saveCart(getCurrentUser(session), cart);

            CartItem item = cart.getItem(productId);
            double itemSubtotal = (item != null) ? item.getSubTotal() : 0;

            String json = """
            {
              "success": true,
              "totalQuantity": %d,
              "total": %.0f,
              "itemSubtotal": %.0f
            }
            """.formatted(
                    cart.totalQuantity(),
                    cart.total(),
                    itemSubtotal
            );

            response.getWriter().write(json);

        } catch (Exception e) {
            e.printStackTrace();
            response.getWriter().write("{\"success\":false}");
        }
    }
     
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    private User getCurrentUser(HttpSession session) {
        Object user = session.getAttribute("userInSession");

        if (user instanceof User currentUser) {
            return currentUser;
        }

        user = session.getAttribute("authUser");

        return user instanceof User currentUser ? currentUser : null;
    }
}
