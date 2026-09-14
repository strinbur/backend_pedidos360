package com.pedidos360.cart.client;

import com.pedidos360.cart.dto.ProductDTO;
import com.pedidos360.cart.exception.ProductNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private final RestClient productRestClient;

    public ProductClient(RestClient productRestClient) {
        this.productRestClient = productRestClient;
    }

    public ProductDTO getProductById(String productId) {
        try {
            return productRestClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .body(ProductDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ProductNotFoundException(productId);
        }
    }
}