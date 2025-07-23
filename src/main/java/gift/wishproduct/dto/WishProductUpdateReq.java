package gift.wishproduct.dto;

import jakarta.validation.constraints.Min;

public class WishProductUpdateReq {

    @Min(value = 1, message = "1개 이상 주문이 가능합니다.")
    private int quantity;

    public WishProductUpdateReq(int quantity) {
        this.quantity = quantity;
    }

    protected WishProductUpdateReq() {}

    public int getQuantity() {
        return quantity;
    }
}
