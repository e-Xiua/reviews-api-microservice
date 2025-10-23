package com.iwellness.reviews.event;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewDeletedEvent implements Serializable {
    private Long reviewId;
    private Long serviceId;
    private Long userId;
    private LocalDateTime deletedAt;
}
