package gift.wishproduct.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class WishProductCreateReq {

    @NotNull
    private Long productId;
    @NotNull
    private Long optionId;

    @Min(value = 1, message = "1개 이상 주문이 가능합니다.")
    private int quantity;

    public WishProductCreateReq(Long productId, Long optionId, int quantity) {
        this.productId = productId;
        this.optionId = optionId;
        this.quantity = quantity;
    }

    protected WishProductCreateReq() {
    }

    public Long getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public Long getOptionId() {
        return optionId;
    }
}
