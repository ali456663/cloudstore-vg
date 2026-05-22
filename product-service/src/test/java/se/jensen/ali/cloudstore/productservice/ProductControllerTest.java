package se.jensen.ali.cloudstore.productservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ProductService productService;

    @Test
    void healthReturnsMessage() throws Exception {
        mockMvc.perform(get("/api/products/health"))
                .andExpect(status().isOk())
                .andExpect(content().string("product-service is running"));
    }

    @Test
    void getAllProductsReturnsProducts() throws Exception {
        Product product = new Product(
                1L,
                "Test Backpack",
                BigDecimal.valueOf(109.95),
                "A test product",
                "bags",
                "https://example.com/backpack.png"
        );

        when(productService.getAllProducts()).thenReturn(List.of(product));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Backpack"))
                .andExpect(jsonPath("$[0].price").value(109.95));
    }

    @Test
    void getProductByIdReturnsProduct() throws Exception {
        Product product = new Product(
                1L,
                "Test Backpack",
                BigDecimal.valueOf(109.95),
                "A test product",
                "bags",
                "https://example.com/backpack.png"
        );

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Backpack"))
                .andExpect(jsonPath("$.category").value("bags"));
    }
}
