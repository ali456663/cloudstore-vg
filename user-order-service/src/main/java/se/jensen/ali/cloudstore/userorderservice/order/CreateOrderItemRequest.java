package se.jensen.ali.cloudstore.userorderservice.order;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrderItemRequest(
        @NotNull
        Long productId,

        @NotBlank
        String productTitle,

        @NotBlank
        String selectedColor,

        @NotBlank
        String selectedSize,

        @NotNull
        @Min(1)
        Integer quantity
) {
}
