package cart.dao;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import cart.domain.CartItem;
import cart.domain.Member;
import cart.domain.Product;

@Repository
public class CartItemDao {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<CartItem> memberProductCartItemRowMapper = (rs, rowNum) -> {
        Long memberId = rs.getLong("member_id");
        String email = rs.getString("email");
        Long productId = rs.getLong("product.id");
        String name = rs.getString("name");
        int price = rs.getInt("price");
        String imageUrl = rs.getString("image_url");
        Long cartItemId = rs.getLong("cart_item.id");
        int quantity = rs.getInt("cart_item.quantity");
        int grade = rs.getInt("grade.grade");
        Member member = new Member(memberId, email, null, grade);
        Product product = new Product(productId, name, price, imageUrl);
        return new CartItem(cartItemId, quantity, product, member);
    };

    public CartItemDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<CartItem> findAllByMemberId(Long memberId) {
        String sql =
                "SELECT cart_item.id, cart_item.member_id, member.email, product.id, product.name, product.price, product.image_url, cart_item.quantity, grade.grade "
                        +
                        "FROM cart_item " +
                        "INNER JOIN member ON cart_item.member_id = member.id " +
                        "INNER JOIN product ON cart_item.product_id = product.id " +
                        "INNER JOIN grade ON member.grade_id = grade.id " +
                        "WHERE cart_item.member_id = ?";
        return jdbcTemplate.query(sql, memberProductCartItemRowMapper, memberId);
    }

    public Long save(CartItem cartItem) {
        KeyHolder keyHolder = new GeneratedKeyHolder();

        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO cart_item (member_id, product_id, quantity) VALUES (?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS
            );

            ps.setLong(1, cartItem.getMember().getId());
            ps.setLong(2, cartItem.getProduct().getId());
            ps.setInt(3, cartItem.getQuantity());

            return ps;
        }, keyHolder);

        return Objects.requireNonNull(keyHolder.getKey()).longValue();
    }

    public CartItem findById(Long id) {
        String sql =
                "SELECT cart_item.id, cart_item.member_id, member.email, product.id, product.name, product.price, product.image_url, cart_item.quantity, grade.grade "
                        +
                        "FROM cart_item " +
                        "INNER JOIN member ON cart_item.member_id = member.id " +
                        "INNER JOIN product ON cart_item.product_id = product.id "
                        + "INNER JOIN grade ON member.grade_id = grade.id " +
                        "WHERE cart_item.id = ?";
        List<CartItem> cartItems = jdbcTemplate.query(sql, memberProductCartItemRowMapper, id);
        return cartItems.isEmpty() ? null : cartItems.get(0);
    }


    public void deleteById(Long id) {
        String sql = "DELETE FROM cart_item WHERE id = ?";
        jdbcTemplate.update(sql, id);
    }

    public void updateQuantity(CartItem cartItem) {
        String sql = "UPDATE cart_item SET quantity = ? WHERE id = ?";
        jdbcTemplate.update(sql, cartItem.getQuantity(), cartItem.getId());
    }

    public int deleteByIds(List<Long> ids) {
        String inClause = String.join(",", Collections.nCopies(ids.size(), "?"));
        String sql = "DELETE FROM cart_item WHERE id IN (" + inClause + ")";
        return jdbcTemplate.update(sql, ids.toArray());
    }

    public List<CartItem> findAllByIds(List<Long> ids) {
        String inClause = String.join(",", Collections.nCopies(ids.size(), "?"));
        final String sql =
                "SELECT cart_item.id, cart_item.member_id, member.email, product.id, product.name, product.price, product.image_url, cart_item.quantity, grade.grade "
                        + "FROM cart_item "
                        + "INNER JOIN member ON cart_item.member_id = member.id "
                        + "INNER JOIN product ON cart_item.product_id = product.id "
                        + "INNER JOIN grade ON grade.id = member.grade_id "
                        + "WHERE cart_item.id IN (" + inClause + ")";
        return jdbcTemplate.query(sql, memberProductCartItemRowMapper, ids.toArray());
    }
}
