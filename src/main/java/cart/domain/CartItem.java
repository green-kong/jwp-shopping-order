package cart.domain;

import java.util.Objects;

import cart.exception.CartItemException;

public class CartItem {
    private final Long id;
    private Integer quantity;
    private final Product product;
    private final Member member;

    public CartItem(Long id, int quantity, Product product, Member member) {
        this.id = id;
        this.quantity = quantity;
        this.product = product;
        this.member = member;
    }

    public CartItem(Product product, Member member) {
        this(null, 1, product, member);
    }

    public CartItem(Integer quantity, Product product, Member member) {
        this(null, quantity, product, member);
    }

    public Long getId() {
        return id;
    }

    public Member getMember() {
        return member;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void checkOwner(Member member) {
        if (!this.member.equals(member)) {
            throw new CartItemException.IllegalMember(this, member);
        }
    }

    public void changeQuantity(int quantity) {
        this.quantity = quantity;
    }

    public Integer calculateTotalPrice() {
        return product.getPrice() * quantity;
    }

    public boolean isSameProduct(Long productId) {
        return this.product.getId().equals(productId);
    }

    public Long getProductId() {
        return this.product.getId();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CartItem cartItem = (CartItem) o;
        return Objects.equals(id, cartItem.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
