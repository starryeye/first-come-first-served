package dev.practice.api.producer;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponCreateProducer {

    private final KafkaTemplate<String, Long> kafkaTemplate;

    public void sendCouponCreate(Long userId) {
        kafkaTemplate.send("coupon-create", userId);
    }
}
