package com.pedidos360.kafkaadmin.dto;

import java.util.List;
import java.util.Map;

public record TopicResponse(
        String name,
        int partitions,
        int replicationFactor,
        List<PartitionResponse> partitionDetails,
        Map<String, String> configs
) {}