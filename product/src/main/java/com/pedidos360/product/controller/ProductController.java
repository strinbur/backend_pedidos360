package com.pedidos360.product.controller;

import com.pedidos360.product.dto.ProductRequestDTO;
import com.pedidos360.product.dto.ProductResponseDTO;
import com.pedidos360.product.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin(origins = "http://localhost:4200","https://52.22.2.226")
@RestController
@RequestMapping("/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@RequestBody ProductRequestDTO product) {
        return ResponseEntity.ok(productService.create(product));
    }

    @GetMapping
    public ResponseEntity<List<ProductResponseDTO>> findAll() {
        return ResponseEntity.ok(productService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> findById(@PathVariable String id) {
        return ResponseEntity.ok(productService.findById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(@PathVariable String id, @RequestBody ProductRequestDTO product) {
        return ResponseEntity.ok(productService.update(id, product));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable String id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ProductResponseDTO> findByCode(@PathVariable String code) {
        return ResponseEntity.ok(productService.findByCode(code));
    }
}
