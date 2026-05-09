package nlu.fit.web.souvenirecommerce.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewSummary {

    private int totalReviews;

    private double avgRating;
}