package gift.order.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record CartOrderCreateRequest(
        @NotNull(message = "위시 상품 아이디는 필수 값입니다.")
        Long wishProductId,
        @Range(min = 1, message = "주문 수량은 최소 1개입니다.")
        int quantity,
        String message
) {

    public CartOrderCreateRequest {
        if (message == null || message.isBlank()) {
            message = "주문이 정상적으로 완료되었습니다.";
        }
    }

}
