package com.pedidos360.kafkaadmin.broker;

import com.pedidos360.kafkaadmin.dto.TopicResponse;

import java.util.List;
import java.util.Map;

public interface BrokerAdminService {

    List<String> listTopics();

    TopicResponse describeTopic(String name);

    void createTopic(String name, int partitions, short replicationFactor, Map<String, String> configs);

    void deleteTopic(String name);

    void increasePartitions(String name, int totalPartitions);

    void setConfigs(String name, Map<String, String> configs);

    void resetConfig(String name, String key);
}