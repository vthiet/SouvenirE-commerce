package nlu.fit.web.souvenirecommerce.model.entity;

import jakarta.persistence.*;
import nlu.fit.web.souvenirecommerce.common.utils.I18nUtil;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "products")
@SQLDelete(sql = "UPDATE products SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(name = "name_en")
    private String nameEn;

    @Lob
    private String description;

    @Lob
    @Column(name = "description_en")
    private String descriptionEn;

    @Column(name = "short_description")
    private String shortDescription;

    @Lob
    @Column(name = "short_description_en")
    private String shortDescriptionEn;

    @Column(name = "original_price")
    private double originalPrice;

    @Column(name = "discount_percent")
    private Integer discountPercent;

    @Column(name = "sale_price")
    private Double salePrice;

    @Column(name = "image_url")
    private String image;

    @Column(name = "stock_quantity")
    private int stockQuantity;

    @Column(name = "total_sold")
    private int totalSold;

    @Column(name = "avg_rating")
    private double avgRating;

    @Column(name = "review_count")
    private int reviewCount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    public Long getCategoryId(){
        return this.category != null ? this.category.getId() : null;
    }

    public String getImageUrl() {
        return image;
    }

    public void setImageUrl(String imageUrl) {
        this.image = imageUrl;
    }

    public String getName() {
        return localizedValue(name, nameEn);
    }

    public String getLocalizedName() {
        return getName();
    }

    public String getRawName() {
        return name;
    }

    public String getDescription() {
        return localizedValue(description, descriptionEn);
    }

    public String getLocalizedDescription() {
        return getDescription();
    }

    public String getRawDescription() {
        return description;
    }

    public String getShortDescription() {
        return localizedValue(shortDescription, shortDescriptionEn);
    }

    public String getLocalizedShortDescription() {
        return getShortDescription();
    }

    public String getRawShortDescription() {
        return shortDescription;
    }

    private String localizedValue(String primaryValue, String localizedValue) {
        if (I18nUtil.isEnglishLocale()) {
            return firstNonBlank(localizedValue, primaryValue);
        }
        return firstNonBlank(primaryValue, localizedValue);
    }

    private String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first;
        }
        if (second != null && !second.isBlank()) {
            return second;
        }
        return first;
    }
}
