package cart.repository;

import static cart.fixture.Fixture.GOLD_MEMBER;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;

import cart.dao.CartItemDao;
import cart.dao.OrderDao;
import cart.dao.OrderedItemDao;
import cart.dao.ProductDao;
import cart.domain.CartItem;
import cart.domain.Member;
import cart.domain.Order;
import cart.entity.OrderEntity;
import cart.fixture.Fixture;

@JdbcTest
class OrderRepositoryTest {
    @Autowired
    JdbcTemplate jdbcTemplate;
    OrderDao orderDao;
    CartItemDao cartItemDao;
    OrderedItemDao orderedItemDao;
    ProductDao productDao;
    OrderRepository orderRepository;

    @BeforeEach
    void setUp() {
        orderDao = new OrderDao(jdbcTemplate);
        cartItemDao = new CartItemDao(jdbcTemplate);
        orderedItemDao = new OrderedItemDao(jdbcTemplate);
        productDao = new ProductDao(jdbcTemplate);
        orderRepository = new OrderRepository(orderDao, cartItemDao, orderedItemDao, productDao);
    }

    @Test
    @DisplayName("orderId를 인자로 전달받아 Order를 반환한다.")
    void findOrderById() {
        //given
        final List<CartItem> items = List.of(Fixture.CART_ITEM1, Fixture.CART_ITEM2);
        final Long orderId = orderDao.save(new OrderEntity(10000, 1L));
        orderedItemDao.saveAll(items, orderId);

        //when
        final Order result = orderRepository.findOrderByIdAndMember(orderId, GOLD_MEMBER).get();

        //then
        Assertions.assertAll(
                () -> assertThat(result.getId()).isEqualTo(orderId),
                () -> assertThat(result.getPrice()).isEqualTo(10000),
                () -> assertThat(result.getOrderedItems().size()).isEqualTo(2),
                () -> assertThat(result.getOrderedItems().get(0).getQuantity()).isEqualTo(Fixture.CART_ITEM1.getQuantity()),
                () -> assertThat(result.getOrderedItems().get(1).getQuantity()).isEqualTo(Fixture.CART_ITEM2.getQuantity())
        );
    }

    @Test
    @DisplayName("존재하지 않는 orderId를 통해 조회를 하는 경우 Optional.Empty()를 반환한다.")
    void findOrderByIdFailByUnExistedOrderId() {
        final Optional<Order> result = orderRepository.findOrderByIdAndMember(30000L, GOLD_MEMBER);

        assertThat(result).isEqualTo(Optional.empty());
    }

    @Test
    @DisplayName("member를 인자로 전달받아 member의 order를 조회한다.")
    void findOrdersByMember() {
        //given
        Member member = GOLD_MEMBER;
        final List<CartItem> item1 = List.of(Fixture.CART_ITEM1);
        final List<CartItem> item2 = List.of(Fixture.CART_ITEM2);

        final Long orderId1 = orderDao.save(new OrderEntity(10000, member.getId()));
        final Long orderId2 = orderDao.save(new OrderEntity(20000, member.getId()));

        orderedItemDao.saveAll(item1, orderId1);
        orderedItemDao.saveAll(item2, orderId2);

        //when
        final List<Order> results = orderRepository.findAllByMember(GOLD_MEMBER);

        //then
        Assertions.assertAll(
                () -> assertThat(results.size()).isEqualTo(2),
                () -> assertThat(results.get(0).getId()).isEqualTo(orderId1),
                () -> assertThat(results.get(1).getId()).isEqualTo(orderId2),
                () -> assertThat(results.get(0).getPrice()).isEqualTo(10000),
                () -> assertThat(results.get(1).getPrice()).isEqualTo(20000)
        );
    }
}
