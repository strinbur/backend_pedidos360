package com.pedidos360.product.model;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
@Data
@Document(collection = "products")

public class Product {
  @Id
  private String id;
  private String code;
  private String name;
  private String description;
  private double price;
  private int stock;
  private String category;
  private String imageUrl;
}