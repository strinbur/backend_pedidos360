package com.pedidos360.product.config;

import com.mongodb.client.MongoClient;
import io.flamingock.api.annotations.EnableFlamingock;
import io.flamingock.api.annotations.Stage;
import io.flamingock.store.mongodb.sync.MongoDBSyncAuditStore;
import io.flamingock.targetsystem.mongodb.sync.MongoDBSyncTargetSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableFlamingock(
    stages = @Stage(location = "com.pedidos360.product.migration")
)
@Configuration
public class FlamingockConfig {

    @Value("${spring.mongodb.database:product_db}")
    private String databaseName;

    @Bean
    public MongoDBSyncTargetSystem mongoTargetSystem(MongoClient mongoClient) {
        return new MongoDBSyncTargetSystem("product-mongo", mongoClient, databaseName);
    }

    @Bean
    public MongoDBSyncAuditStore mongoAuditStore(MongoDBSyncTargetSystem mongoTargetSystem) {
        return MongoDBSyncAuditStore.from(mongoTargetSystem);
    }
}