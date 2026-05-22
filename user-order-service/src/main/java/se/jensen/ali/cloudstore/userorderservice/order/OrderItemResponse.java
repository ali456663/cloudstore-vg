package se.jensen.ali.cloudstore.userorderservice.order;

public record OrderItemResponse(
        Long productId,
        String productTitle,
        String selectedColor,
        String selectedSize,
        Integer quantity
) {
    public static OrderItemResponse from(OrderItem item) {
        return new OrderItemResponse(
                item.getProductId(),
                item.getProductTitle(),
                item.getSelectedColor(),
                item.getSelectedSize(),
                item.getQuantity()
        );
    }
}
