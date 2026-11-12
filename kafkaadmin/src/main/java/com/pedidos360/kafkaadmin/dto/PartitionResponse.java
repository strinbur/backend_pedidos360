package com.pedidos360.kafkaadmin.dto;

import java.util.List;

public record PartitionResponse(int partition, int leader, List<Integer> replicas, List<Integer> isr) {}