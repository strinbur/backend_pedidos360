package com.pedidos360.product.migration;

import com.mongodb.client.MongoDatabase;
import io.flamingock.api.annotations.Apply;
import io.flamingock.api.annotations.Change;
import io.flamingock.api.annotations.Rollback;
import io.flamingock.api.annotations.TargetSystem;
import org.bson.Document;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

@TargetSystem(id = "product-mongo")
@Change(id = "seed-initial-products", author = "pato")
public class _0001__SeedProducts {

    private static final String IMAGE_BASE_PATH = "/images/products";
    private final AtomicInteger imageIndex = new AtomicInteger(1);

    @Apply
    public void execution(MongoDatabase database) {
        List<Document> data = List.of(
            product("PROD-00001", "Torta de chocolate", "Torta de chocolate con relleno de manjar", 15990, 10, "torta"),
            product("PROD-00002", "Torta 3 leches", "Bizcocho húmedo bañado en tres tipos de leche", 14990, 8, "torta"),
            product("PROD-00003", "Torta frutos del bosque", "Torta de vainilla con mix de berries frescos", 16990, 6, "torta"),
            product("PROD-00004", "Torta milhoja nuez manjar", "Capas de hojaldre, manjar y nueces caramelizadas", 17990, 5, "torta"),
            product("PROD-00005", "Torta selva negra", "Bizcocho de chocolate, cerezas y crema chantilly", 17490, 7, "torta"),
            product("PROD-00006", "Pie de limón", "Base crocante con crema de limón y merengue", 12990, 12, "pie"),
            product("PROD-00007", "Cheesecake de frutilla", "Cheesecake horneado con cobertura de frutilla", 15490, 9, "cheesecake"),
            product("PROD-00008", "Cheesecake de maracuyá", "Cheesecake frío con salsa de maracuyá", 15490, 7, "cheesecake"),
            product("PROD-00009", "Kuchen de manzana", "Kuchen alemán con manzanas y canela", 13990, 10, "kuchen"),
            product("PROD-00010", "Kuchen de nuez", "Masa quebrada rellena de crema de nuez", 13990, 8, "kuchen"),
            product("PROD-00011", "Tarta de santiago", "Tarta española de almendras", 14490, 6, "tarta"),
            product("PROD-00012", "Brownie de chocolate", "Brownie húmedo con trozos de chocolate", 3990, 20, "pastel individual"),
            product("PROD-00013", "Muffin de arándanos", "Muffin esponjoso con arándanos frescos", 2490, 25, "pastel individual"),
            product("PROD-00014", "Cupcake red velvet", "Cupcake red velvet con frosting de queso crema", 2990, 18, "pastel individual"),
            product("PROD-00015", "Alfajor de manjar", "Alfajor artesanal bañado en chocolate", 1990, 30, "galleteria"),
            product("PROD-00016", "Torta de zanahoria", "Torta especiada de zanahoria con frosting de queso crema", 15990, 6, "torta"),
            product("PROD-00017", "Torta de vainilla", "Bizcocho de vainilla con crema pastelera", 13990, 8, "torta"),
            product("PROD-00018", "Rollo de canela", "Rollo esponjoso con relleno de canela y glaseado", 2490, 20, "pastel individual"),
            product("PROD-00019", "Tarta de manzana", "Tarta clásica de manzana con canela", 14990, 7, "tarta"),
            product("PROD-00020", "Torta de dulce de leche", "Bizcocho relleno de dulce de leche y nueces", 16490, 5, "torta")
        );

        database.getCollection("products").insertMany(data);
    }

    @Rollback
    public void rollbackExecution(MongoDatabase database) {
        List<String> codes = List.of(
            "PROD-00001", "PROD-00002", "PROD-00003", "PROD-00004", "PROD-00005",
            "PROD-00006", "PROD-00007", "PROD-00008", "PROD-00009", "PROD-00010",
            "PROD-00011", "PROD-00012", "PROD-00013", "PROD-00014", "PROD-00015",
            "PROD-00016", "PROD-00017", "PROD-00018", "PROD-00019", "PROD-00020"
        );
        database.getCollection("products").deleteMany(new Document("code", new Document("$in", codes)));
    }

    private Document product(String code, String name, String description, double price, int stock, String category) {
        String imageUrl = IMAGE_BASE_PATH + "/prod" + imageIndex.getAndIncrement() + ".jpg";
        return new Document()
            .append("code", code)
            .append("name", name)
            .append("description", description)
            .append("price", price)
            .append("stock", stock)
            .append("category", category)
            .append("imageUrl", imageUrl);
    }
}