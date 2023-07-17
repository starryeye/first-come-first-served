package dev.practice.api.service;

import dev.practice.api.domain.Coupon;
import dev.practice.api.producer.CouponCreateProducer;
import dev.practice.api.repository.CouponCountRepository;
import dev.practice.api.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApplyServiceWithRedisAndKafka {

    private final CouponCountRepository couponCountRepository;
    private final CouponCreateProducer couponCreateProducer;

    public void applyCoupon(Long userId) {
        /**
         * 쿠폰 발급
         * 1. 발급된 쿠폰의 갯수를 1 증가 시키고 갯수를 리턴받는다. (Redis 활용)
         * 2. 발급된 쿠폰의 갯수가 100 개 초과이면 쿠폰을 발급하지 않는다.
         * 3. 100 개 이하이면 쿠폰을 발급한다. (DB 에 적재, 발급 전에 Redis 에서 갯수를 1 증가시키므로 100개 이하면 발급한다.)
         *
         * 비동기로 ApplyServiceWithRedis 의 문제점을 해결하였다. (사실상 거의 non-blocking I/O)
         * DB 저장 로직을 kafka producer send message 로 변경하였다.
         */

        long count = couponCountRepository.increment();

        if (count > 100) {
            return;
        }

        couponCreateProducer.sendCouponCreate(userId);
    }
}
