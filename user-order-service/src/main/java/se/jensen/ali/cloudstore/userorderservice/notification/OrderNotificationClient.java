package se.jensen.ali.cloudstore.userorderservice.notification;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.jensen.ali.cloudstore.userorderservice.order.CustomerOrder;
import se.jensen.ali.cloudstore.userorderservice.order.OrderItem;
import se.jensen.ali.cloudstore.userorderservice.user.AppUser;

import java.util.List;

@Service
public class OrderNotificationClient {

    private final String googleScriptUrl;
    private final String sellerEmail;
    private final RestClient restClient;

    public OrderNotificationClient(
            @Value("${notification.google-script-url:}") String googleScriptUrl,
            @Value("${notification.seller-email:ali.wafa17943@gmail.com}") String sellerEmail
    ) {
        this.googleScriptUrl = googleScriptUrl;
        this.sellerEmail = sellerEmail;
        this.restClient = RestClient.create();
    }

    public void sendOrderEmail(CustomerOrder order) {
        if (googleScriptUrl == null || googleScriptUrl.isBlank()) {
            return;
        }

        AppUser user = order.getUser();
        OrderEmailRequest request = new OrderEmailRequest(
                sellerEmail,
                order.getId(),
                user.getFullName(),
                user.getEmail(),
                user.getPhoneNumber(),
                order.getItems().stream().map(OrderEmailItem::from).toList()
        );

        restClient.post()
                .uri(googleScriptUrl)
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }

    private record OrderEmailRequest(
            String sellerEmail,
            Long orderId,
            String customerName,
            String customerEmail,
            String customerPhoneNumber,
            List<OrderEmailItem> items
    ) {
    }

    private record OrderEmailItem(
            Long productId,
            String productTitle,
            String selectedColor,
            String selectedSize,
            Integer quantity
    ) {
        private static OrderEmailItem from(OrderItem item) {
            return new OrderEmailItem(
                    item.getProductId(),
                    item.getProductTitle(),
                    item.getSelectedColor(),
                    item.getSelectedSize(),
                    item.getQuantity()
            );
        }
    }
}
