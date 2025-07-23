package gift.wishproduct.dto;

import gift.domain.WishProduct;

public class WishProductResponse {

    private Long id;
    private String productName;
    private String optionName;
    private int price;
    private int quantity;
    private String imageUrl;
  
    public WishProductResponse(WishProduct wishProduct) {
        this.id = wishProduct.getId();
        this.productName = wishProduct.getProduct().getName();
        this.optionName = wishProduct.getOption().getName();
        this.price = wishProduct.getProduct().getPrice();
        this.quantity = wishProduct.getQuantity();
        this.imageUrl = wishProduct.getProduct().getImageUrl();
    }

    protected WishProductResponse() {}

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public int getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getOptionName() {
        return optionName;
    }
}
