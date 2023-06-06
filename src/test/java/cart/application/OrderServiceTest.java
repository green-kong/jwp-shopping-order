package cart.application;

import static cart.fixture.Fixture.GOLD_MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import cart.domain.CartItem;
import cart.domain.Member;
import cart.domain.price.DiscountPriceCalculator;
import cart.dto.OrderRequest;
import cart.dto.OrderResponse;
import cart.exception.CartItemException;
import cart.exception.InvalidOrderException;
import cart.fixture.Fixture;

@SpringBootTest
@Sql("/data.sql")
class OrderServiceTest {
    @Autowired
    OrderService orderService;
    @Autowired
    DiscountPriceCalculator discountPriceCalculator;


    @Test
    @DisplayName("주문을 한다.")
    public void order() {
        // given
        List<Long> cartItemIds = Arrays.asList(1L, 2L);
        Member member = GOLD_MEMBER;
        OrderRequest orderRequest = new OrderRequest(cartItemIds);

        // when
        Long result = orderService.add(orderRequest, member);

        // then
        assertThat(result).isEqualTo(1L);
    }

    @Test
    @DisplayName("cartItem의 member와 전달받은 member가 일치하지 않으면 예외를 던진다.")
    public void orderFail() {
        // given
        List<Long> cartItemIds = Arrays.asList(1L, 2L, 3L);
        Member member = GOLD_MEMBER;
        OrderRequest orderRequest = new OrderRequest(cartItemIds);

        // then
        assertThatThrownBy(() -> orderService.add(orderRequest, member))
                .isInstanceOf(CartItemException.IllegalMember.class)
                .hasMessageContaining("Illegal member attempts to cart;");
    }

    @Test
    @DisplayName("orderId와 member를 통해 해당하는 order의 orderResponse를 반환한다.")
    void findOrderById() {
        //given
        Member member = GOLD_MEMBER;
        final CartItem cartItem1 = Fixture.CART_ITEM1;
        final CartItem cartItem2 = Fixture.CART_ITEM2;
        final int price = cartItem1.calculateTotalPrice() + cartItem2.calculateTotalPrice();
        final int expectedPrice = discountPriceCalculator.calculateFinalPrice(price, member);

        List<Long> cartItemIds = Arrays.asList(cartItem1.getId(), cartItem2.getId());
        OrderRequest orderRequest = new OrderRequest(cartItemIds);
        Long orderId = orderService.add(orderRequest, member);

        //when
        final OrderResponse result = orderService.findByIdAndMember(orderId, member);

        //then
        Assertions.assertAll(
                () -> assertThat(result.getId()).isEqualTo(orderId),
                () -> assertThat(result.getTotalPrice()).isEqualTo(expectedPrice),
                () -> assertThat(result.getCartItems().size()).isEqualTo(2),
                () -> assertThat(result.getCartItems().get(0).getQuantity()).isEqualTo(cartItem1.getQuantity()),
                () -> assertThat(result.getCartItems().get(1).getQuantity()).isEqualTo(cartItem2.getQuantity())
        );
    }

    @Test
    @DisplayName("orderId와 member를 통해 해당하는 order의 orderResponse를 반환한다.")
    void findOrderByIdFailByUnExistedOrderId() {
        assertThatThrownBy(() -> orderService.findByIdAndMember(30000L, GOLD_MEMBER))
                .isInstanceOf(InvalidOrderException.class)
                .hasMessageContaining("OrderId is not existed;");
    }

    @Test
    @DisplayName("member를 통해 해당 member의 orderResponse를 반환한다.")
    void findOrdersByMember() {
        //given
        Member member = GOLD_MEMBER;
        final Long id1 = addOrder(member, Fixture.CART_ITEM1);
        final Long id2 = addOrder(member, Fixture.CART_ITEM2);

        final int expectedPrice1 = discountPriceCalculator.calculateFinalPrice(Fixture.CART_ITEM1.calculateTotalPrice(),
                member);
        final int expectedPrice2 = discountPriceCalculator.calculateFinalPrice(Fixture.CART_ITEM2.calculateTotalPrice(),
                member);

        //when
        final List<OrderResponse> result = orderService.findAllByMember(member);
        final List<Long> idCollection = result.stream()
                .map(OrderResponse::getId)
                .collect(Collectors.toUnmodifiableList());
        final List<Integer> priceCollect = result.stream()
                .map(OrderResponse::getTotalPrice)
                .collect(Collectors.toUnmodifiableList());

        //then
        Assertions.assertAll(
                () -> assertThat(result.size()).isEqualTo(2),
                () -> assertThat(idCollection).containsExactlyInAnyOrder(id1, id2),
                () -> assertThat(priceCollect).containsExactlyInAnyOrder(expectedPrice1, expectedPrice2)
        );
    }

    private Long addOrder(Member member, CartItem cartItem) {
        List<Long> cartItemId = List.of(cartItem.getId());
        OrderRequest orderRequest1 = new OrderRequest(cartItemId);
        return orderService.add(orderRequest1, member);
    }
}
