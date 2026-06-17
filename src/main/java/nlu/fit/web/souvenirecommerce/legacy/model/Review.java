package nlu.fit.web.souvenirecommerce.legacy.model;

import lombok.*;

import java.sql.Timestamp;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    private int id;
    private Long productId;
    private String productName;
    private String productImage;
    private int userId;
    private String userName;
    private String userEmail;
    private int rating;
    private String comment;
    private Timestamp createdAt;
}
