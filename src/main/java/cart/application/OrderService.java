package cart.application;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import cart.dao.CartItemDao;
import cart.domain.CartItem;
import cart.domain.Member;
import cart.domain.Order;
import cart.domain.price.PriceCalculator;
import cart.dto.OrderRequest;
import cart.dto.OrderResponse;
import cart.exception.CartItemException.UnExistedCartItem;
import cart.exception.InvalidOrderException;
import cart.repository.OrderRepository;

@Service
@Transactional(readOnly = true)
public class OrderService {
    private final CartItemDao cartItemDao;
    private final PriceCalculator priceCalculator;
    private final OrderRepository orderRepository;

    public OrderService(
            CartItemDao cartItemDao,
            PriceCalculator priceCalculator,
            OrderRepository orderRepository
    ) {
        this.cartItemDao = cartItemDao;
        this.priceCalculator = priceCalculator;
        this.orderRepository = orderRepository;
    }

    @Transactional
    public Long add(OrderRequest orderRequest, Member member) {
        final List<Long> cartItemIds = orderRequest.getCartItemIds();
        final List<CartItem> cartItems = cartItemDao.findAllByIds(cartItemIds);
        validateExistentCartItems(cartItemIds, cartItems);
        Order order = Order.of(cartItems, member, priceCalculator);
        return orderRepository.save(order);
    }

    private void validateExistentCartItems(List<Long> cartItemIds, List<CartItem> cartItems) {
        if (cartItemIds.size() != cartItems.size()) {
            throw new UnExistedCartItem("존재하지 않는 cartItemId가 포함되어 있습니다.");
        }
    }

    public OrderResponse findByIdAndMember(Long orderId, Member member) {
        final Order order = orderRepository.findOrderByIdAndMember(orderId, member)
                .orElseThrow(() -> new InvalidOrderException("OrderId is not existed; orderId = " + orderId));
        return OrderResponse.from(order);
    }

    public List<OrderResponse> findAllByMember(Member member) {
        final List<Order> orders = orderRepository.findAllByMember(member);
        return orders.stream()
                .map(OrderResponse::from)
                .collect(Collectors.toUnmodifiableList());
    }
}
