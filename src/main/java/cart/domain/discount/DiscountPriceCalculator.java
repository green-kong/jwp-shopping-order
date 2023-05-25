package cart.domain.discount;

import cart.domain.Member;

public class DiscountPriceCalculator implements PriceCalculator{
    private final DiscountPolicy discountPolicy;

    public DiscountPriceCalculator(DiscountPolicy discountPolicy) {
        this.discountPolicy = discountPolicy;
    }

    @Override
    public Integer calculateFinalPrice(Integer price, Member member) {
        return price - discountPolicy.calculateDiscountPrice(price, member);
    }
}