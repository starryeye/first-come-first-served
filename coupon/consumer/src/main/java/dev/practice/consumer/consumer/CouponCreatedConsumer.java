package dev.practice.consumer.consumer;

import dev.practice.consumer.domain.Coupon;
import dev.practice.consumer.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponCreatedConsumer {

    private final CouponRepository couponRepository;

    @KafkaListener(topics = "coupon-create", groupId = "group-1")
    public void listener(Long userId) {

        couponRepository.save(new Coupon(userId));

        System.out.println("received message, Coupon created userId(" + userId + ")");
    }
}
