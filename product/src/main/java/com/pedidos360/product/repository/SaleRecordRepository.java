package com.pedidos360.product.repository;

import com.pedidos360.product.model.SaleRecord;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRecordRepository extends MongoRepository<SaleRecord, String> {
}