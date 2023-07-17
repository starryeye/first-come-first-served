package dev.practice.consumer.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class CouponCreatedConsumer {

    @KafkaListener(topics = "coupon-create", groupId = "group-1")
    public void listener(Long userId) {
        System.out.println("received message, userId = " + userId);
    }
}
