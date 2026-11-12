package com.pedidos360.product.usecase;

import com.pedidos360.product.messaging.dto.OrderPlacedMessage;
import com.pedidos360.product.model.SaleRecord;
import com.pedidos360.product.repository.SaleRecordRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RecordSale {

    private static final Logger log = LoggerFactory.getLogger(RecordSale.class);
    private final SaleRecordRepository repository;

    public RecordSale(SaleRecordRepository repository) {
        this.repository = repository;
    }

    public void execute(OrderPlacedMessage order) {
        List<SaleRecord> records = order.items().stream().map(i -> {
            SaleRecord r = new SaleRecord();
            r.setId(order.cartId() + ":" + i.productId());
            r.setCartId(order.cartId());
            r.setProductId(i.productId());
            r.setProductName(i.name());
            r.setQuantity(i.quantity());
            r.setSubtotal(i.subtotal());
            r.setOccurredAt(order.occurredAt());
            return r;
        }).toList();

        repository.saveAll(records);
        log.info("Venta registrada cartId={} ítems={} total={}", order.cartId(), records.size(), order.total());
    }
}