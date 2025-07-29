package gift.order.dto;

import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Range;

public record DirectOrderCreateRequest(
        @NotNull(message = "옵션 아이디는 필수 값입니다.")
        Long optionId,
        @Range(min = 1, message = "주문 수량은 최소 1개입니다.")
        int quantity,
        String message
) {

    public DirectOrderCreateRequest {
        if (message == null || message.isBlank()) {
            message = "주문이 정상적으로 완료되었습니다.";
        }
    }
}
