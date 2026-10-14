package com.pedidos360.cart.client;

import com.pedidos360.cart.cache.CachedProduct;
import com.pedidos360.cart.cache.ProductCache;
import com.pedidos360.cart.dto.ProductDTO;
import com.pedidos360.cart.exception.ProductNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

@Component
public class ProductClient {

    private static final Logger log = LoggerFactory.getLogger(ProductClient.class);

    private final RestClient productRestClient;
    private final ProductCache cache;

    public ProductClient(RestClient productRestClient, ProductCache cache) {
        this.productRestClient = productRestClient;
        this.cache = cache;
    }

    public ProductDTO getProductById(String productId) {
        try {
            ProductDTO product = productRestClient.get()
                    .uri("/products/{id}", productId)
                    .retrieve()
                    .body(ProductDTO.class);
            if (product != null) {
                cache.put(new CachedProduct(product.id(), product.code(), product.name(),
                        product.price(), product.imageUrl()));
            }
            return product;
        } catch (HttpClientErrorException.NotFound ex) {
            cache.remove(productId);
            throw new ProductNotFoundException(productId);
        } catch (ResourceAccessException ex) {
            // product no responde: usa el último dato conocido
            log.warn("Product no disponible, usando caché para productId={}", productId);
            return cache.find(productId)
                    .map(c -> new ProductDTO(c.id(), c.code(), c.name(), null, c.price(),
                            Integer.MAX_VALUE,   // stock desconocido: lo valida product al consumir
                            null, c.imageUrl()))
                    .orElseThrow(() -> ex);
        }
    }
}