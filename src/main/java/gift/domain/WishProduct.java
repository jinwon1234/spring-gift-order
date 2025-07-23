package gift.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "wish_product")
public class WishProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private Option option;


    public WishProduct(int quantity, Member owner, Product product, Option option) {
        this.quantity = quantity;
        this.owner = owner;
        this.product = product;
        this.option = option;
    }

    public WishProduct(Long id, int quantity, Member owner, Product product, Option option) {
        this.id = id;
        this.quantity = quantity;
        this.owner = owner;
        this.product = product;
        this.option = option;
    }

    protected WishProduct() {}

    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public Member getOwner() {
        return owner;
    }

    public Product getProduct() {
        return product;
    }

    public Option getOption() {
        return option;
    }

    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }
}
