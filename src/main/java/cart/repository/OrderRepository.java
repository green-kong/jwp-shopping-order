package cart.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Repository;

import cart.dao.CartItemDao;
import cart.dao.OrderDao;
import cart.dao.OrderedItemDao;
import cart.dao.ProductDao;
import cart.domain.CartItem;
import cart.domain.CartItems;
import cart.domain.Member;
import cart.domain.Order;
import cart.domain.Product;
import cart.entity.OrderEntity;
import cart.entity.OrderedItemEntity;

@Repository
public class OrderRepository {
    private final OrderDao orderDao;
    private final CartItemDao cartItemDao;
    private final OrderedItemDao orderedItemDao;
    private final ProductDao productDao;

    public OrderRepository(
            OrderDao orderDao,
            CartItemDao cartItemDao,
            OrderedItemDao orderedItemDao,
            ProductDao productDao
    ) {
        this.orderDao = orderDao;
        this.cartItemDao = cartItemDao;
        this.orderedItemDao = orderedItemDao;
        this.productDao = productDao;
    }

    public Long save(Order order) {
        final OrderEntity orderEntity = new OrderEntity(order.getPrice(), order.getMember().getId());
        final Long orderId = orderDao.save(orderEntity);
        orderedItemDao.saveAll(order.getOrderedItems(), orderId);
        cartItemDao.deleteByIds(order.getOrderedItemIds());
        return orderId;
    }

    public Optional<Order> findOrderByIdAndMember(Long orderId, Member member) {
        final Optional<OrderEntity> savedOrder = orderDao.findById(orderId);
        if (savedOrder.isEmpty()) {
            return Optional.empty();
        }
        final OrderEntity orderEntity = savedOrder.get();
        List<OrderedItemEntity> orderedItems = orderedItemDao.findAllByOrderId(orderId);
        final CartItems cartItems = orderedItemsToCartItems(member, orderedItems);
        return Optional.of(new Order(orderEntity.getId(), orderEntity.getPrice(), member, cartItems));
    }

    private CartItems orderedItemsToCartItems(Member member, List<OrderedItemEntity> orderedItems) {
        final List<Long> productIds = collectIds(orderedItems);
        List<Product> products = productDao.findAllByIds(productIds);
        final Map<Long, Product> idToProduct = createProductMappingTable(products);

        final List<CartItem> cartItems = orderedItems.stream()
                .map(orderedItemEntity -> new CartItem(
                        orderedItemEntity.getQuantity(),
                        idToProduct.get(orderedItemEntity.getProductId()),
                        member
                ))
                .collect(Collectors.toUnmodifiableList());
        return CartItems.of(cartItems, member);
    }

    private Map<Long, Product> createProductMappingTable(List<Product> products) {
        return products.stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));
    }

    private List<Long> collectIds(List<OrderedItemEntity> orderedItems) {
        return orderedItems.stream()
                .map(OrderedItemEntity::getProductId)
                .collect(Collectors.toUnmodifiableList());
    }

    public List<Order> findAllByMember(Member member) {
        final List<OrderEntity> orders = orderDao.findAllByMember(member);
        List<Order> result = new ArrayList<>();
        for (OrderEntity order : orders) {
            final List<OrderedItemEntity> items = orderedItemDao.findAllByOrderId(order.getId());
            final CartItems cartItems = orderedItemsToCartItems(member, items);
            result.add(new Order(order.getId(), order.getPrice(), member, cartItems));
        }
        return result;
    }
}
