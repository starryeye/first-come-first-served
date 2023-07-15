package dev.practice.api.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class CouponCountRepository {

    private final StringRedisTemplate stringRedisTemplate;

    /**
     * Redis INCR 명령어를 활용하여 쿠폰 발급 갯수를 1 증가 시킨다.
     * Redis 는 단일 쓰레드로 동작하기 때문에 동시성 문제가 발생하지 않는다.
     */
    public Long increment() {
        return stringRedisTemplate
                .opsForValue()
                .increment("coupon:count");
    }

    public void reset() {
        stringRedisTemplate.delete("coupon:count");
    }
}
