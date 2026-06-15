package nlu.fit.web.souvenirecommerce.features.product.service;

import nlu.fit.web.souvenirecommerce.legacy.dao.CategoryDAO;
import nlu.fit.web.souvenirecommerce.legacy.dao.ProductDAO;
import nlu.fit.web.souvenirecommerce.legacy.dao.PromotionDAO;
import nlu.fit.web.souvenirecommerce.features.product.dto.ProductCardDTO;
import nlu.fit.web.souvenirecommerce.features.product.dto.ProductTypeDTO;
import nlu.fit.web.souvenirecommerce.model.enums.ProductSort;
import nlu.fit.web.souvenirecommerce.legacy.model.Promotion;
import nlu.fit.web.souvenirecommerce.model.entity.Category;
import nlu.fit.web.souvenirecommerce.model.entity.Product;
import nlu.fit.web.souvenirecommerce.common.utils.ProductCardMapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProductTypeService {

    private static final int PAGE_SIZE = 12;

    private final ProductDAO productDAO = new ProductDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();
    private final PromotionDAO promotionDAO = new PromotionDAO();

    public ProductTypeDTO getProductType(Long categoryId, Integer minPrice, Integer maxPrice, Integer rating, ProductSort sort, int page) {
        Category category = categoryDAO.getCategoryById(categoryId);

        if (category == null) {
            return null;
        }

        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;

        List<Product> products = productDAO.getProductsByCategoryWithFilter(categoryId, minPrice, maxPrice, rating, sort, offset, PAGE_SIZE);

        int totalProducts = productDAO.countProductsByCategoryWithFilter(categoryId, minPrice, maxPrice, rating);

        int totalPages = (int) Math.ceil((double) totalProducts / PAGE_SIZE);

        ProductTypeDTO dto = new ProductTypeDTO();
        dto.setCategory(category);
        dto.setProducts(mapToProductCardDTOs(products));
        dto.setCurrentPage(safePage);
        dto.setTotalPages(totalPages);
        dto.setTotalProducts(totalProducts);
        dto.setMinPrice(minPrice);
        dto.setMaxPrice(maxPrice);
        dto.setRating(rating);
        dto.setSort(sort);
        dto.setSortParam(sort != null ? sort.name().toLowerCase() : "popular");
        dto.setListingAction("/category");

        return dto;
    }

    public ProductTypeDTO getSearchProductType(String keyword, Integer minPrice, Integer maxPrice, Integer rating, ProductSort sort, int page) {
        String safeKeyword = keyword == null ? "" : keyword.trim();

        if (safeKeyword.isEmpty()) {
            return null;
        }

        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;

        List<Product> products = productDAO.searchProductsWithFilter(
                safeKeyword,
                minPrice,
                maxPrice,
                rating,
                sort,
                offset,
                PAGE_SIZE
        );

        int totalProducts = productDAO.countSearchProductsWithFilter(
                safeKeyword,
                minPrice,
                maxPrice,
                rating
        );

        int totalPages = Math.max(1, (int) Math.ceil((double) totalProducts / PAGE_SIZE));

        Category temporaryCategory = Category.builder()
                .id(0L)
                .categoryName("Kết quả tìm kiếm: " + safeKeyword)
                .image("")
                .build();

        ProductTypeDTO dto = new ProductTypeDTO();
        dto.setCategory(temporaryCategory);
        dto.setProducts(mapToProductCardDTOs(products));
        dto.setCurrentPage(safePage);
        dto.setTotalPages(totalPages);
        dto.setTotalProducts(totalProducts);
        dto.setMinPrice(minPrice);
        dto.setMaxPrice(maxPrice);
        dto.setRating(rating);
        dto.setSort(sort);
        dto.setSortParam(sort != null ? sort.name().toLowerCase() : "popular");
        dto.setSearchMode(true);
        dto.setSearchKeyword(safeKeyword);
        dto.setListingAction("/search");

        return dto;
    }

    public ProductTypeDTO getPanelProductType(String panelSlug, Integer minPrice, Integer maxPrice, Integer rating, ProductSort sort, int page) {
        PanelDefinition panel = PanelDefinition.from(panelSlug);
        if (panel == null) {
            return null;
        }

        int safePage = Math.max(page, 1);
        int offset = (safePage - 1) * PAGE_SIZE;

        List<Product> products = productDAO.getProductsByCategoryIdsWithFilter(
                panel.categoryIds(),
                minPrice,
                maxPrice,
                rating,
                sort,
                offset,
                PAGE_SIZE
        );

        int totalProducts = productDAO.countProductsByCategoryIdsWithFilter(
                panel.categoryIds(),
                minPrice,
                maxPrice,
                rating
        );

        int totalPages = Math.max(1, (int) Math.ceil((double) totalProducts / PAGE_SIZE));

        Category temporaryCategory = Category.builder()
                .id(0L)
                .categoryName(panel.title())
                .image("")
                .build();

        ProductTypeDTO dto = new ProductTypeDTO();
        dto.setCategory(temporaryCategory);
        dto.setProducts(mapToProductCardDTOs(products));
        dto.setCurrentPage(safePage);
        dto.setTotalPages(totalPages);
        dto.setTotalProducts(totalProducts);
        dto.setMinPrice(minPrice);
        dto.setMaxPrice(maxPrice);
        dto.setRating(rating);
        dto.setSort(sort);
        dto.setSortParam(sort != null ? sort.name().toLowerCase() : "popular");
        dto.setPanelMode(true);
        dto.setPanelSlug(panel.slug());
        dto.setListingAction("/products");

        return dto;
    }

    private List<ProductCardDTO> mapToProductCardDTOs(List<Product> products) {
        List<ProductCardDTO> cards = new ArrayList<>();

        if (products == null || products.isEmpty()) {
            return cards;
        }

        List<Long> productIds = products.stream().map(Product::getId).toList();

        Map<Long, Promotion> promotionMap = promotionDAO.getActivePromotionsByProductIds(productIds);

        for (Product product : products) {
            cards.add(ProductCardMapper.from(product, promotionMap.get(product.getId())));
        }

        return cards;
    }

    private record PanelDefinition(String slug, String title, List<Long> categoryIds) {
        private static PanelDefinition from(String slug) {
            if (slug == null || slug.isBlank()) {
                return null;
            }

            return switch (slug.trim()) {
                case "vi-que-binh-dinh" -> new PanelDefinition(
                        "vi-que-binh-dinh",
                        "Vị Quê Bình Định",
                        List.of(1L, 2L)
                );
                case "qua-tu-bien" -> new PanelDefinition(
                        "qua-tu-bien",
                        "Quà Từ Biển",
                        List.of(4L)
                );
                case "huong-men-dat-vo" -> new PanelDefinition(
                        "huong-men-dat-vo",
                        "Hương Men Đất Võ",
                        List.of(3L)
                );
                case "net-viet-lam-qua" -> new PanelDefinition(
                        "net-viet-lam-qua",
                        "Nét Việt Làm Quà",
                        List.of(5L, 6L)
                );
                default -> null;
            };
        }
    }
}
