package se.jensen.ali.cloudstore.productservice.service;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import se.jensen.ali.cloudstore.productservice.model.Product;

import java.util.Arrays;
import java.util.List;

@Service
public class ProductService {

    private final RestClient restClient;

    public ProductService() {
        this.restClient = RestClient.create("https://fakestoreapi.com");
    }

    public List<Product> getAllProducts() {
        Product[] products = restClient.get()
                .uri("/products")
                .retrieve()
                .body(Product[].class);

        if (products == null) {
            return List.of();
        }

        return Arrays.asList(products);
    }

    public Product getProductById(Long id) {
        return restClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(Product.class);
    }
}
