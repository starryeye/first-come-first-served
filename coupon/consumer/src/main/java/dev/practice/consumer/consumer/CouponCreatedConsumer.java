package dev.practice.consumer.consumer;

import dev.practice.consumer.domain.Coupon;
import dev.practice.consumer.domain.FailedEvent;
import dev.practice.consumer.repository.CouponRepository;
import dev.practice.consumer.repository.FailedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CouponCreatedConsumer {

    private final CouponRepository couponRepository;
    private final FailedEventRepository failedEventRepository;

    /**
     * Kafka consumer 로, 쿠폰 생성 이벤트를 소비하여 DB 에 저장한다.(쿠폰 발급)
     *
     * 쿠폰 발급 처리를 비동기로 처리하고 있기 때문에
     * consumer 가 쿠폰 발급 처리중 장애가 발생해도 producer 는 알지 못하고 발급 성공했다고 처리한다..
     * -> 따라서, 쿠폰 발급에 실패하면 FailedEvent 를 저장한다. 이후, 배치를 통해 발급 재시도를 한다.
     * TODO: batch retry
     * TODO: 쿠폰 발급 이벤트 publish 때, producer 와 broker 간의 장애가 나면 producer 에서 completableFuture 를 사용하여 retry 를 하자.
     */
    @KafkaListener(topics = "coupon-create", groupId = "group-1")
    public void listener(Long userId) {

        try {
            couponRepository.save(new Coupon(userId));
            System.out.println("received message, Coupon created userId(" + userId + ")");
        }catch (Exception e) {
            System.out.println("received message, Coupon created fail userId(" + userId + ")" + " Exception :" + e.getMessage());
            failedEventRepository.save(new FailedEvent(userId));
        }
    }
}
