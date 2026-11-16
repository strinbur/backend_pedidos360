package com.pedidos360.product.usecase;

import com.pedidos360.product.messaging.dto.OrderPlacedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RecordSale {

    private static final Logger log = LoggerFactory.getLogger(RecordSale.class);
    private final SaleRecordWriter writer;

    public RecordSale(SaleRecordWriter writer) {
        this.writer = writer;
    }

    public void execute(OrderPlacedMessage order) {
        order.items().forEach(i -> writer.recordPending(
                order.cartId(), i.productId(), i.name(), i.quantity(), i.subtotal(), order.occurredAt()));
        log.info("Pedido recibido (PENDIENTE) cartId={} ítems={} total={}",
                order.cartId(), order.items().size(), order.total());
    }
}