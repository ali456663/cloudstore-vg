package se.jensen.ali.cloudstore.userorderservice.order;

public record OrderItemResponse(
        Long productId,
        Integer quantity
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(item.getProductId(), item.getQuantity());
    }
}
