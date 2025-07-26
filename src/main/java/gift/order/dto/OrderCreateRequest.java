package gift.order.dto;

public record OrderCreateRequest(
        Long wishProductId,
        int quantity,
        String message
) {
}
