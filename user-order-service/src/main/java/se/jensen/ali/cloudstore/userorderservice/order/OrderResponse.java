package se.jensen.ali.cloudstore.userorderservice.order;

import java.time.Instant;
import java.util.List;

public record OrderResponse(
        Long id,
        String username,
        Instant createdAt,
        List<OrderItemResponse> items
) {
    public static OrderResponse from(CustomerOrder order) {
        return new OrderResponse(
                order.getId(),
                order.getUser().getUsername(),
                order.getCreatedAt(),
                order.getItems().stream().map(OrderItemResponse::from).toList()
        );
    }
}
