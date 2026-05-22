package se.jensen.ali.cloudstore.userorderservice.order;

import org.springframework.stereotype.Service;
import se.jensen.ali.cloudstore.userorderservice.product.ProductClient;
import se.jensen.ali.cloudstore.userorderservice.user.AppUser;
import se.jensen.ali.cloudstore.userorderservice.user.AppUserRepository;

@Service
public class OrderService {

    private final AppUserRepository appUserRepository;
    private final CustomerOrderRepository customerOrderRepository;
    private final ProductClient productClient;

    public OrderService(
            AppUserRepository appUserRepository,
            CustomerOrderRepository customerOrderRepository,
            ProductClient productClient
    ) {
        this.appUserRepository = appUserRepository;
        this.customerOrderRepository = customerOrderRepository;
        this.productClient = productClient;
    }

    public OrderResponse createOrder(String username, CreateOrderRequest request) {
        AppUser user = appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        CustomerOrder order = new CustomerOrder(user);

        for (CreateOrderItemRequest itemRequest : request.items()) {
            if (!productClient.productExists(itemRequest.productId())) {
                throw new IllegalArgumentException("Product does not exist: " + itemRequest.productId());
            }

            order.addItem(new OrderItem(itemRequest.productId(), itemRequest.quantity()));
        }

        CustomerOrder savedOrder = customerOrderRepository.save(order);
        return OrderResponse.from(savedOrder);
    }
}
