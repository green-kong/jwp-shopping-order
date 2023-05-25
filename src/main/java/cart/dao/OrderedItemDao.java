package cart.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import cart.domain.CartItem;

@Repository
public class OrderedItemDao {
    private final JdbcTemplate jdbcTemplate;

    public OrderedItemDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int[] saveAll(List<CartItem> cartItems, Long orderId) {
        final String sql = "INSERT INTO ordered_item (order_id, product_id, quantity) VALUES (?, ?, ?)";
        return jdbcTemplate.batchUpdate(
                sql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        final CartItem cartItem = cartItems.get(i);
                        ps.setLong(1, orderId);
                        ps.setLong(2, cartItem.getProduct().getId());
                        ps.setInt(3, cartItem.getQuantity());
                    }

                    @Override
                    public int getBatchSize() {
                        return cartItems.size();
                    }
                }
        );
    }
}