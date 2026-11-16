package com.pedidos360.product.usecase;

import com.pedidos360.product.messaging.dto.CheckoutMessage;
import com.pedidos360.product.messaging.dto.OrderResultItem;
import com.pedidos360.product.messaging.dto.OrderResultMessage;
import com.pedidos360.product.messaging.dto.OrderResultStatus;
import com.pedidos360.product.messaging.producer.PublishOrderResult;
import com.pedidos360.product.usecase.ReduceStock.SoldItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class ResolveOrder {

    private static final Logger log = LoggerFactory.getLogger(ResolveOrder.class);

    private final SaleRecordWriter writer;
    private final PublishOrderResult publish;

    public ResolveOrder(SaleRecordWriter writer, PublishOrderResult publish) {
        this.writer = writer;
        this.publish = publish;
    }

    // Ninguno de los dos métodos lanza: el stock ya se resolvió y no debe afectar el ACK/NACK de Rabbit.

    public void confirm(CheckoutMessage checkout, List<SoldItem> sold) {
        String now = Instant.now().toString();
        try {
            for (SoldItem i : sold) {
                writer.resolve(checkout.cartId(), i.productId(), i.quantity(),
                        i.name(), i.subtotal(), "CONFIRMADA", now);
            }
        } catch (RuntimeException e) {
            log.error("No se pudo marcar CONFIRMADA cartId={}", checkout.cartId(), e);
        }
        List<OrderResultItem> items = sold.stream()
                .map(i -> new OrderResultItem(i.productId(), i.name(), i.quantity(), i.subtotal()))
                .toList();
        publish.execute(new OrderResultMessage(checkout.cartId(), OrderResultStatus.CONFIRMED, null, now, items));
    }

    public void reject(CheckoutMessage checkout, String reason) {
        String now = Instant.now().toString();
        try {
            checkout.items().forEach(i -> writer.resolve(checkout.cartId(), i.productId(), i.quantity(),
                    null, 0, "RECHAZADA", now));
        } catch (RuntimeException e) {
            log.error("No se pudo marcar RECHAZADA cartId={}", checkout.cartId(), e);
        }
        List<OrderResultItem> items = checkout.items().stream()
                .map(i -> new OrderResultItem(i.productId(), null, i.quantity(), 0))
                .toList();
        publish.execute(new OrderResultMessage(checkout.cartId(), OrderResultStatus.REJECTED, reason, now, items));
    }
}