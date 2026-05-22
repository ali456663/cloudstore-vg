package se.jensen.ali.cloudstore.userorderservice;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import se.jensen.ali.cloudstore.userorderservice.auth.AuthService;
import se.jensen.ali.cloudstore.userorderservice.auth.RegisterRequest;
import se.jensen.ali.cloudstore.userorderservice.notification.OrderNotificationClient;
import se.jensen.ali.cloudstore.userorderservice.order.CreateOrderItemRequest;
import se.jensen.ali.cloudstore.userorderservice.order.CreateOrderRequest;
import se.jensen.ali.cloudstore.userorderservice.order.CustomerOrder;
import se.jensen.ali.cloudstore.userorderservice.order.CustomerOrderRepository;
import se.jensen.ali.cloudstore.userorderservice.order.OrderResponse;
import se.jensen.ali.cloudstore.userorderservice.order.OrderService;
import se.jensen.ali.cloudstore.userorderservice.product.ProductClient;
import se.jensen.ali.cloudstore.userorderservice.user.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
class OrderServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private CustomerOrderRepository customerOrderRepository;

    @MockitoBean
    private ProductClient productClient;

    @MockitoBean
    private OrderNotificationClient orderNotificationClient;

    @BeforeEach
    void setUp() {
        customerOrderRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void createOrderSavesOrderForUser() {
        authService.register(new RegisterRequest("Ali", "Hassan", "ali@test.com", "0701234567", "password123"));
        when(productClient.productExists(1L)).thenReturn(true);
        when(productClient.productExists(9L)).thenReturn(true);
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(1L, "Test jacket", "Black", "M", 2),
                new CreateOrderItemRequest(9L, "Test shoes", "White", "42", 1)
        ));

        OrderResponse response = orderService.createOrder("ali@test.com", request);

        assertThat(response.id()).isNotNull();
        assertThat(response.username()).isEqualTo("ali@test.com");
        assertThat(response.customerName()).isEqualTo("Ali Hassan");
        assertThat(response.customerEmail()).isEqualTo("ali@test.com");
        assertThat(response.items()).hasSize(2);
        assertThat(response.items().getFirst().productId()).isEqualTo(1L);
        assertThat(response.items().getFirst().productTitle()).isEqualTo("Test jacket");
        assertThat(response.items().getFirst().selectedColor()).isEqualTo("Black");
        assertThat(response.items().getFirst().selectedSize()).isEqualTo("M");
        assertThat(response.items().getFirst().quantity()).isEqualTo(2);
        assertThat(customerOrderRepository.count()).isEqualTo(1);
        verify(orderNotificationClient).sendOrderEmail(org.mockito.ArgumentMatchers.any(CustomerOrder.class));
    }
}
