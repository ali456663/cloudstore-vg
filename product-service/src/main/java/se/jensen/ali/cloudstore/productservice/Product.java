package se.jensen.ali.cloudstore.productservice;

import java.math.BigDecimal;

public record Product(
        Long id,
        String title,
        BigDecimal price,
        String description,
        String category,
        String image
) {
}
