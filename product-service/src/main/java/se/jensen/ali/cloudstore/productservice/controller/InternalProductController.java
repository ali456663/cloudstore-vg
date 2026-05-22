package se.jensen.ali.cloudstore.productservice.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import se.jensen.ali.cloudstore.productservice.model.Product;
import se.jensen.ali.cloudstore.productservice.model.ProductExistsResponse;
import se.jensen.ali.cloudstore.productservice.service.ProductService;

@RestController
@RequestMapping("/api/internal/products")
public class InternalProductController {

    private final ProductService productService;

    public InternalProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{id}/exists")
    public ProductExistsResponse productExists(@PathVariable Long id) {
        Product product = productService.getProductById(id);
        return new ProductExistsResponse(product != null && product.id() != null);
    }
}
