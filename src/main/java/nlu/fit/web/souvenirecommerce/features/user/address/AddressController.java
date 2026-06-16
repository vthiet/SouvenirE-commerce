package nlu.fit.web.souvenirecommerce.features.user.address;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import nlu.fit.web.souvenirecommerce.core.logging.AuditLogService;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.model.entity.Ward;

import java.io.IOException;
import java.util.List;

@WebServlet("/user/address/*")
public class AddressController extends HttpServlet {

    private final AddressService addressService = new AddressService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        User user = getCurrentUser(session);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getPathInfo();
        if (path == null || "/".equals(path)) {
            showAddressPage(request, response, user, false);
            return;
        }

        try {
            switch (path) {
                case "/add" -> {
                    showAddressPage(request, response, user, true);
                    return;
                }

                case "/wards" -> {
                    writeWardsJson(request, response);
                    return;
                }

                case "/default" -> {
                    Long id = parseLong(request.getParameter("id"));
                    boolean updated = addressService.setDefaultAddress(user.getId(), id);
                    if (updated) {
                        AuditLogService.success(
                                AddressController.class,
                                user,
                                "ADDRESS",
                                "ADDRESS_DEFAULT_CHANGED",
                                "ADDRESS",
                                AuditLogService.describe("addressId", id)
                        );
                    } else {
                        AuditLogService.failure(
                                AddressController.class,
                                user,
                                "ADDRESS",
                                "ADDRESS_DEFAULT_CHANGED",
                                "ADDRESS",
                                AuditLogService.describe("addressId", id, "reason", "update_failed")
                        );
                    }
                }

                case "/delete" -> {
                    Long id = parseLong(request.getParameter("id"));
                    boolean deleted = addressService.deleteAddress(user.getId(), id);
                    if (deleted) {
                        AuditLogService.success(
                                AddressController.class,
                                user,
                                "ADDRESS",
                                "ADDRESS_DELETED",
                                "ADDRESS",
                                AuditLogService.describe("addressId", id)
                        );
                    } else {
                        AuditLogService.failure(
                                AddressController.class,
                                user,
                                "ADDRESS",
                                "ADDRESS_DELETED",
                                "ADDRESS",
                                AuditLogService.describe("addressId", id, "reason", "delete_failed")
                        );
                    }
                }

                default -> {
                    // không làm gì
                }
            }
        } catch (NumberFormatException e) {
            // user sửa URL → bỏ qua
        }

        response.sendRedirect(request.getContextPath() + "/user/address");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        HttpSession session = request.getSession(false);
        User user = getCurrentUser(session);

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String path = request.getPathInfo();
        if (path == null) {
            response.sendRedirect(request.getContextPath() + "/user/address");
            return;
        }

        try {
            switch (path) {

                case "/add" -> {
                    if (user.getId() != null) {
                        String detail = request.getParameter("addressDetail");
                        Integer provinceCode = parseInteger(request.getParameter("provinceCode"));
                        Integer wardCode = parseInteger(request.getParameter("wardCode"));

                        boolean added = addressService.addAddress(user, detail, provinceCode, wardCode);
                        if (!added) {
                            AuditLogService.failure(
                                    AddressController.class,
                                    user,
                                    "ADDRESS",
                                    "ADDRESS_ADDED",
                                    "ADDRESS",
                                    AuditLogService.describe("reason", "invalid_address_data")
                            );
                            session.setAttribute("profileMessage", "Vui lòng chọn đầy đủ tỉnh/thành phố, phường/xã và nhập địa chỉ chi tiết");
                            session.setAttribute("profileMessageType", "error");
                            response.sendRedirect(request.getContextPath() + "/user/address/add");
                            return;
                        }
                        AuditLogService.success(
                                AddressController.class,
                                user,
                                "ADDRESS",
                                "ADDRESS_ADDED",
                                "ADDRESS",
                                AuditLogService.describe("provinceCode", provinceCode, "wardCode", wardCode)
                        );
                        session.setAttribute("profileMessage", "Thêm địa chỉ thành công!");
                        session.setAttribute("profileMessageType", "success");
                    }
                }

                case "/edit" -> {
                    response.sendRedirect(request.getContextPath() + "/user/address");
                    return;
                }

                default -> {
                    // không làm gì
                }
            }
        } catch (NumberFormatException e) {
            // user sửa URL → bỏ qua
        }

        response.sendRedirect(request.getContextPath() + "/user/address");
    }

    private void showAddressPage(HttpServletRequest request, HttpServletResponse response, User user, boolean addMode)
            throws ServletException, IOException {
        if (user.getId() != null) {
            request.setAttribute("listAddr", addressService.getUserAddresses(user.getId()));
        }
        request.setAttribute("addressMode", addMode ? "add" : "list");
        request.setAttribute("provinceOptions", addressService.getProvinces());
        request.setAttribute("pageTitle", "Địa chỉ");
        request.setAttribute("pageCss", "account/account-layout.css");
        request.setAttribute("contentCss", "account/profile-form.css");
        request.setAttribute("pageJs", "account/profile.js");
        request.setAttribute("enableSelect2", true);
        request.setAttribute("pageContent", "addresses.jsp");
        request.setAttribute("contentPage", "/WEB-INF/views/account/account_layout.jsp");

        request.getRequestDispatcher("/WEB-INF/layout/base.jsp").forward(request, response);
    }

    private void writeWardsJson(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Integer provinceCode = parseInteger(request.getParameter("provinceCode"));
        String keyword = request.getParameter("q");
        List<Ward> wards = addressService.getWardsByProvinceCode(provinceCode);

        response.setContentType("application/json;charset=UTF-8");
        StringBuilder json = new StringBuilder("{\"results\":[");
        int resultCount = 0;
        for (Ward ward : wards) {
            String text = ward.getFullName() == null || ward.getFullName().isBlank()
                    ? ward.getName()
                    : ward.getFullName();
            if (!matchesKeyword(text, keyword)) {
                continue;
            }
            if (resultCount > 0) {
                json.append(',');
            }
            resultCount++;
            json.append("{\"id\":\"")
                    .append(ward.getCode())
                    .append("\",\"text\":\"")
                    .append(escapeJson(text))
                    .append("\"}");
        }
        json.append("]}");
        response.getWriter().write(json.toString());
    }

    private boolean matchesKeyword(String text, String keyword) {
        return keyword == null
                || keyword.isBlank()
                || (text != null && text.toLowerCase().contains(keyword.trim().toLowerCase()));
    }

    private Integer parseInteger(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(value);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String escapeJson(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }

    private User getCurrentUser(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object user = session.getAttribute("userInSession");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("user");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("currentUser");
        if (user instanceof User) {
            return (User) user;
        }

        user = session.getAttribute("authUser");
        return user instanceof User ? (User) user : null;
    }
}
