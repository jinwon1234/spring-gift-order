package gift.domain;


import jakarta.persistence.*;

@Entity
@Table(name = "orders")
public class Order extends BaseTimeEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private String message;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "option_id", nullable = false)
    private Option option;


    protected Order() {}

    public Order(int quantity, String message, Option option) {
        this.quantity = quantity;
        this.message = message;
        this.option = option;
    }

    public Long getId() {
        return id;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getMessage() {
        return message;
    }

    public Option getOption() {
        return option;
    }
}
