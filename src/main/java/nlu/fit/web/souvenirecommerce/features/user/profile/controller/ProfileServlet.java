package nlu.fit.web.souvenirecommerce.features.user.profile.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import nlu.fit.web.souvenirecommerce.model.enums.Gender;
import nlu.fit.web.souvenirecommerce.model.entity.User;
import nlu.fit.web.souvenirecommerce.features.user.profile.service.ProfileService;
import nlu.fit.web.souvenirecommerce.common.utils.CloudinaryUtil;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * ProfileServlet handles HTTP requests related to displaying the user profile page.
 * This servlet supports both GET and POST methods.
 *
 * Annotations:
 * - {@code @MultipartConfig}: Configures file upload settings for POST requests.
 * - {@code @WebServlet}: Maps this servlet to the "/user/profile" endpoint.
 *
 * Key Features:
 * - If the user session is invalid or the user is not authenticated, the servlet redirects
 *   the user to the login page.
 * - Configures attributes required to render the profile page, such as CSS and JavaScript
 *   resources, and forwards the request to the base layout JSP.
 * - The POST method is available but not implemented in this servlet.
 *
 * Configuration Details:
 * - {@code fileSizeThreshold}: Maximum size (in bytes) of file data kept in memory before being written to disk.
 * - {@code maxFileSize}: Maximum size (in bytes) of an uploaded file.
 * - {@code maxRequestSize}: Maximum size (in bytes) of a complete multipart request.
 */
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024,
        maxFileSize = 5 * 1024 * 1024,
        maxRequestSize = 10 * 1024 * 1024
)
@WebServlet("/user/profile")
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

         req.setAttribute("pageTitle", "Hồ sơ");
         req.setAttribute("pageCss", "account/account-layout.css");
         req.setAttribute("contentCss", "account/profile-form.css");
         req.setAttribute("pageJs", "account/profile.js");
         req.setAttribute("contentPage", "/WEB-INF/views/account/account_layout.jsp");

        req.getRequestDispatcher("/WEB-INF/layout/base.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        User currentUser = (User) session.getAttribute("user");
        String action = request.getParameter("action");

        try {
            if ("update_profile".equals(action)) {
                updateUserProfile(request, response, currentUser, session);
            } else if ("change_avatar".equals(action)) {
                changeUserAvatar(request, response, currentUser, session);
            } else {
                response.sendRedirect(request.getContextPath() + "/user/profile");
            }
        } catch (Exception e) {
            Logger.getLogger(ProfileServlet.class.getName()).log(Level.SEVERE, "Error updating profile", e);
            session.setAttribute("profileMessage", "Có lỗi xảy ra khi cập nhật hồ sơ: " + e.getMessage());
            session.setAttribute("profileMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/user/profile");
        }
    }

    private void updateUserProfile(HttpServletRequest request, HttpServletResponse response,
                                   User currentUser, HttpSession session) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String phone = request.getParameter("phone");
        String gender = request.getParameter("gender");
        String dob = request.getParameter("dob");

        // Validation
        if (firstName == null || firstName.trim().isEmpty()) {
            session.setAttribute("profileMessage", "Tên không được để trống");
            session.setAttribute("profileMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/user/profile");
            return;
        }

        if (lastName == null || lastName.trim().isEmpty()) {
            session.setAttribute("profileMessage", "Họ không được để trống");
            session.setAttribute("profileMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/user/profile");
            return;
        }

        if (phone == null || phone.trim().isEmpty() || !phone.matches("^[0-9]{10,20}$")) {
            session.setAttribute("profileMessage", "Số điện thoại không hợp lệ (10-20 chữ số)");
            session.setAttribute("profileMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/user/profile");
            return;
        }

        // Update user fields
        currentUser.setFirstName(firstName.trim());
        currentUser.setLastName(lastName.trim());
        currentUser.setPhone(phone.trim());

        // Update gender if provided
        if (gender != null && !gender.trim().isEmpty()) {
            try {
                Gender genderValue = mapGender(gender);
                if (genderValue != null) {
                    currentUser.setGender(genderValue);
                }
            } catch (IllegalArgumentException e) {
                // Keep existing gender if invalid value provided
            }
        }

        // Update date of birth if provided
        if (dob != null && !dob.trim().isEmpty()) {
            try {
                currentUser.setDateOfBirth(LocalDate.parse(dob, DateTimeFormatter.ISO_LOCAL_DATE));
            } catch (Exception e) {
                // Keep existing date of birth if invalid
            }
        }

        // Save to database
        ProfileService profileService = new ProfileService();
        currentUser = profileService.update(currentUser).get();

        // Update session with all attribute names (consistent with LoginServlet)
        session.setAttribute("currentUser", currentUser);
        session.setAttribute("userInSession", currentUser);
        session.setAttribute("user", currentUser);
        session.setAttribute("authUser", currentUser);

        session.setAttribute("profileMessage", "Cập nhật hồ sơ thành công!");
        session.setAttribute("profileMessageType", "success");
         response.sendRedirect(request.getContextPath() + "/user/profile");
     }

     private Gender mapGender(String vietnameseGender) {
        if (vietnameseGender == null || vietnameseGender.trim().isEmpty()) {
            return null;
        }

        switch (vietnameseGender.trim()) {
            case "Nam":
                return Gender.MALE;
            case "Nữ":
                return Gender.FEMALE;
            case "Khác":
                return Gender.OTHER;
            default:
                return null;
        }
    }

    private void changeUserAvatar(HttpServletRequest request, HttpServletResponse response,
                                  User currentUser, HttpSession session) throws IOException, ServletException {
        // Check if Cloudinary is configured
        if (!CloudinaryUtil.isConfigured()) {
            session.setAttribute("profileMessage", "Hệ thống chưa được cấu hình để tải hình ảnh");
            session.setAttribute("profileMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/user/profile");
            return;
        }

        // Get the uploaded file
        Part filePart = request.getPart("avatarFile");
        if (filePart == null || filePart.getSize() == 0) {
            session.setAttribute("profileMessage", "Vui lòng chọn một tệp hình ảnh");
            session.setAttribute("profileMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/user/profile");
            return;
        }

        // Validate file type
        String contentType = filePart.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            session.setAttribute("profileMessage", "Tệp phải là một hình ảnh");
            session.setAttribute("profileMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/user/profile");
            return;
        }

        // Validate file size (max 5MB)
        if (filePart.getSize() > 5 * 1024 * 1024) {
            session.setAttribute("profileMessage", "Hình ảnh không được vượt quá 5MB");
            session.setAttribute("profileMessageType", "error");
            response.sendRedirect(request.getContextPath() + "/user/profile");
            return;
        }

        try {
            // Delete old avatar from Cloudinary if exists
            if (currentUser.getAvatarPublicId() != null && !currentUser.getAvatarPublicId().isEmpty()) {
                CloudinaryUtil.deleteImage(currentUser.getAvatarPublicId());
            }

            // Generate unique public ID for the avatar
            String publicId = "avatar_user_" + currentUser.getId() + "_" + System.currentTimeMillis();

            // Upload image to Cloudinary
             Map<String, Object> uploadResult = CloudinaryUtil.uploadImage(
                 filePart.getInputStream(),
                 filePart.getSubmittedFileName(),
                 "avatars",
                 publicId
             );

             // Log the upload result for debugging
             Logger.getLogger(ProfileServlet.class.getName()).info("Cloudinary upload result: " + uploadResult);

             // Update user avatar information
             String avatarUrl = (String) uploadResult.get("secure_url");
             if (avatarUrl == null || avatarUrl.isEmpty()) {
                 avatarUrl = (String) uploadResult.get("url");
             }
             currentUser.setAvatarUrl(avatarUrl);
             currentUser.setAvatarPublicId((String) uploadResult.get("public_id"));

            // Save to database
            ProfileService profileService = new ProfileService();
            profileService.update(currentUser);

            // Update session
            session.setAttribute("currentUser", currentUser);
            session.setAttribute("userInSession", currentUser);
            session.setAttribute("user", currentUser);
            session.setAttribute("authUser", currentUser);

            session.setAttribute("profileMessage", "Cập nhật ảnh đại diện thành công!");
            session.setAttribute("profileMessageType", "success");

            Logger.getLogger(ProfileServlet.class.getName()).info("Avatar updated successfully for user: " + currentUser.getId());
        } catch (IOException e) {
            Logger.getLogger(ProfileServlet.class.getName()).log(Level.SEVERE, "Error uploading avatar", e);
            session.setAttribute("profileMessage", "Lỗi tải lên hình ảnh: " + e.getMessage());
            session.setAttribute("profileMessageType", "error");
        }

        response.sendRedirect(request.getContextPath() + "/user/profile");
    }

}
