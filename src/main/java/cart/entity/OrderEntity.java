package cart.entity;

public class OrderEntity {
    private final Long id;
    private final Integer price;
    private final Long memberId;

    public OrderEntity(Long id, Integer price, Long memberId) {
        this.id = id;
        this.price = price;
        this.memberId = memberId;
    }

    public OrderEntity(Integer price, Long memberId) {
        this(null, price, memberId);
    }

    public Long getId() {
        return id;
    }

    public Integer getPrice() {
        return price;
    }

    public Long getMemberId() {
        return memberId;
    }
}
