package se.jensen.ali.cloudstore.userorderservice.product;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import se.jensen.ali.cloudstore.userorderservice.security.ServiceJwtService;

@Service
public class ProductClient {

    private final RestClient restClient;
    private final ServiceJwtService serviceJwtService;

    public ProductClient(
            @Value("${product-service.base-url:http://localhost:8093}") String productServiceBaseUrl,
            ServiceJwtService serviceJwtService
    ) {
        this.restClient = RestClient.create(productServiceBaseUrl);
        this.serviceJwtService = serviceJwtService;
    }

    public boolean productExists(Long productId) {
        try {
            ProductExistsResponse response = restClient.get()
                    .uri("/api/internal/products/{id}/exists", productId)
                    .header("Authorization", "Bearer " + serviceJwtService.createServiceToken())
                    .retrieve()
                    .body(ProductExistsResponse.class);

            return response != null && response.exists();
        } catch (RestClientException exception) {
            System.out.println("Could not verify product " + productId + ": " + exception.getMessage());
            return true;
        }
    }

    private record ProductExistsResponse(boolean exists) {
    }
}
