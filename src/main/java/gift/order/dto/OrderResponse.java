package gift.order.dto;

import java.time.LocalDateTime;

public record OrderResponse(
        Long id,
        Long optionId,
        int quantity,
        LocalDateTime orderDateTime,
        String message
) {
}
