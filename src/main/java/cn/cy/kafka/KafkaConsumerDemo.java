package cn.cy.kafka;

import java.time.Duration;
import java.util.Collection;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRebalanceListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.TopicPartition;

import com.google.common.collect.Lists;

/**
 *
 */
public class KafkaConsumerDemo {

    public static KafkaConsumer<String, String> buildKafkaConsumer(String groupId, String topic) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("group.id", groupId);
        props.put("key.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer",
                "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("max.poll.records", 10);

        KafkaConsumer<String, String> kafkaConsumer = new KafkaConsumer<String, String>(props);
        kafkaConsumer.subscribe(Lists.newArrayList(topic), new ConsumerRebalanceListener() {
            @Override
            public void onPartitionsRevoked(Collection<TopicPartition> partitions) {
                
            }

            @Override
            public void onPartitionsAssigned(Collection<TopicPartition> partitions) {
                System.out.println(kafkaConsumer.endOffsets(partitions));
            }
        });

        return kafkaConsumer;
    }

    public static void main(String[] args) {
        KafkaConsumer<String, String> con = buildKafkaConsumer("test-c", "test");

        for (int i = 0; i < 1000; i++) {
            ConsumerRecords<String, String> records = con.poll(Duration.ofMillis(3000L));

            for (ConsumerRecord<String, String> record : records) {
                System.out.println(String.format("record key %s, record value %s", record.key(), record.value()));
            }
        }

    }
}
