package cart.dao;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import cart.domain.CartItem;
import cart.entity.OrderedItemEntity;

@Repository
public class OrderedItemDao {
    private final JdbcTemplate jdbcTemplate;
    private final RowMapper<OrderedItemEntity> rowMapper = (rs, rowNum) -> {
        Long id = rs.getLong("id");
        Long productId = rs.getLong("product_id");
        Long orderId = rs.getLong("order_id");
        int quantity = rs.getInt("quantity");
        return new OrderedItemEntity(id, orderId, productId, quantity);
    };

    public OrderedItemDao(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public int[] saveAll(List<CartItem> cartItems, Long id) {
        final String sql = "INSERT INTO ordered_item (order_id, product_id, quantity) VALUES (?, ?, ?)";
        return jdbcTemplate.batchUpdate(
                sql,
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        final CartItem cartItem = cartItems.get(i);
                        ps.setLong(1, id);
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

    public List<OrderedItemEntity> findAllByOrderId(Long id) {
        final String sql = "SELECT * FROM ordered_item WHERE order_id = ?";
        return jdbcTemplate.query(sql, rowMapper, id);
    }
}
