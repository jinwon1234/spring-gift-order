package gift.order.dto;

public record KakaoOrderMessageTemplate(
        String productName,
        String optionName,
        int price,
        int quantity,
        String message,
        int totalPrice
) {
}
