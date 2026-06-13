package nlu.fit.web.souvenirecommerce.features.product.dto;

import lombok.*;
import nlu.fit.web.souvenirecommerce.model.enums.ProductSort;
import nlu.fit.web.souvenirecommerce.model.entity.Category;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductTypeDTO {

    private Category category;
    private List<ProductCardDTO> products;

    private int currentPage;
    private int totalPages;
    private int totalProducts;
    private int totalReviews;

    private Integer minPrice;
    private Integer maxPrice;
    private Integer rating;
    private ProductSort sort;
    private String sortParam;
    private boolean searchMode;
    private String searchKeyword;
    private String listingAction;
}
