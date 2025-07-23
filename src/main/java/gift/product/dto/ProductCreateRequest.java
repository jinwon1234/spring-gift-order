package gift.product.dto;

import gift.global.annotation.ImageURLConstraint;
import gift.option.dto.OptionCreateRequest;
import gift.product.annotation.ProductNameConstraint;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

import static gift.util.PatternUtil.PRODUCT_NAME_PATTERN;

public class ProductCreateRequest {

    @Size(min = 1, max = 15, message = "상품이름은 1글자 이상 15글자 이하여야합니다.")
    @Pattern(regexp = PRODUCT_NAME_PATTERN,
    message = "( ), [ ], +, -, &, /, _ 외의 특수문자는 사용할 수 없습니다.")
    @ProductNameConstraint
    @NotBlank
    private String name;

    @Min(value = 1, message = "0원 이하는 가격으로 설정할 수 없습니다.")
    private int price;

    @ImageURLConstraint
    private String imageURL;

    @Size(min = 1, message = "옵션은 최소 1개 이상 입력하셔야합니다.")
    private List<OptionCreateRequest> options;

    protected ProductCreateRequest() {}

    public ProductCreateRequest(String name, int price, String imageURL, List<OptionCreateRequest> options) {
        this.name = name;
        this.price = price;
        this.imageURL = imageURL;
        this.options = options;
    }

    public String getName() {
        return name;
    }

    public int getPrice() {
        return price;
    }

    public String getImageURL() {
        return imageURL;
    }

    public List<OptionCreateRequest> getOptions() {
        return options;
    }
}
