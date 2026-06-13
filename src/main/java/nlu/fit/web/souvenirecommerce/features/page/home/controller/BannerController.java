package nlu.fit.web.souvenirecommerce.features.page.home.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import nlu.fit.web.souvenirecommerce.features.page.home.service.BannerService;
import nlu.fit.web.souvenirecommerce.model.entity.Banner;

import java.io.File;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/banner")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, // 2MB
        maxFileSize = 1024 * 1024 * 10,      // 10MB
        maxRequestSize = 1024 * 1024 * 50    // 50MB
)
public class BannerController extends HttpServlet {
    // Thư mục lưu ảnh
    private static final String UPLOAD_DIR = "assets/images/home_banner";
    private BannerService bannerService;

    @Override
    public void init() throws ServletException {
        bannerService = new BannerService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<Banner> banners = bannerService.findAll();
        req.setAttribute("banners", banners);
        req.getRequestDispatcher("/admin/banners.jsp").forward(req, resp);
    }
//
//    @Override
//    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
//        req.setCharacterEncoding("UTF-8");
//        String action = req.getParameter("action");
//
//        if ("add".equals(action)) {
//            String title = req.getParameter("title");
//            int position = 1;
//            try { position = Integer.parseInt(req.getParameter("position")); } catch (Exception e) {}
//            boolean status = Boolean.parseBoolean(req.getParameter("status"));
//
//            // Upload ảnh
//            String imageUrl = saveFileUpload(req, "imageFile");
//            if (imageUrl == null) imageUrl = "assets/images/home_banner/default.jpg"; // Ảnh mặc định nếu lỗi
//
//            Banner newBanner = new Banner(0, imageUrl, title, position, status);
//            bannerService.save(newBanner);
//
//        } else if ("update".equals(action)) {
//            long id = Long.parseLong(req.getParameter("id"));
//            Banner banner = bannerService.findById(id);
//
//            b.setTitle(req.getParameter("title"));
//            try { b.setPosition(Integer.parseInt(req.getParameter("position"))); } catch (Exception e) {}
//            b.setStatus(Boolean.parseBoolean(req.getParameter("status")));
//
//            // Chỉ cập nhật ảnh nếu người dùng chọn file mới
//            String newImage = saveFileUpload(req, "imageFile");
//            if (newImage != null) {
//                b.setImageUrl(newImage);
//            }
//            dao.update(b);
//
//        } else if ("delete".equals(action)) {
//            int id = Integer.parseInt(req.getParameter("id"));
//            dao.delete(id);
//
//        } else if ("toggle".equals(action)) {
//            int id = Integer.parseInt(req.getParameter("id"));
//            Banner b = dao.getById(id);
//            if (b != null) {
//                b.setStatus(!b.isStatus());
//                dao.update(b);
//            }
//        }
//
//        resp.sendRedirect(req.getContextPath() + "/admin/banner");
//    }
//
//    public void addBanner(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
//        String title = req.getParameter("title");
//        int position = 1;
//        try { position = Integer.parseInt(req.getParameter("position")); } catch (Exception e) {}
//        boolean status = Boolean.parseBoolean(req.getParameter("status"));
//
//        String imageUrl = saveFileUpload(req, "imageFile");
//        if (imageUrl == null) imageUrl = "assets/images/home_banner/default.jpg";
//
//        Banner newBanner = new Banner(0, imageUrl, title, position, status);
//        bannerService.save(newBanner);
//
//    }
//
//
//
//    private String saveFileUpload(HttpServletRequest req, String partName) {
//        try {
//            Part filePart = req.getPart(partName);
//            if (filePart != null && filePart.getSize() > 0 && filePart.getSubmittedFileName() != null && !filePart.getSubmittedFileName().isEmpty()) {
//                String fileName = System.currentTimeMillis() + "_" + filePart.getSubmittedFileName(); // Thêm time để tránh trùng tên
//
//                // Lưu vào thư mục thật của project trên server
//                String applicationPath = req.getServletContext().getRealPath("");
//                String uploadFilePath = applicationPath + File.separator + UPLOAD_DIR;
//
//                File uploadDir = new File(uploadFilePath);
//                if (!uploadDir.exists()) uploadDir.mkdirs();
//
//                String savePath = uploadFilePath + File.separator + fileName;
//                filePart.write(savePath);
//
//                return UPLOAD_DIR + "/" + fileName; // Trả về đường dẫn để lưu DB
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}
