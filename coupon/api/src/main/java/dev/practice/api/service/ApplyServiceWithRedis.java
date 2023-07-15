package dev.practice.api.service;

import dev.practice.api.domain.Coupon;
import dev.practice.api.repository.CouponCountRepository;
import dev.practice.api.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApplyServiceWithRedis {

    private final CouponRepository couponRepository;
    private final CouponCountRepository couponCountRepository;

    public void applyCoupon(Long userId) {
        /**
         * 쿠폰 발급
         * 1. 발급된 쿠폰의 갯수를 1 증가 시키고 갯수를 리턴받는다. (Redis 활용)
         * 2. 발급된 쿠폰의 갯수가 100 개 초과이면 쿠폰을 발급하지 않는다.
         * 3. 100 개 이하이면 쿠폰을 발급한다. (DB 에 적재, 발급 전에 Redis 에서 갯수를 1 증가시키므로 100개 이하면 발급한다.)
         *
         * ApplyService 와 비교하여 달라진점.
         * - 쿠폰 발급 갯수를 Redis 에서 관리한다.
         * - Redis 에서 관리하여 DB 의 totalCount 쿼리에 대한 부담이 없어졌다. (메모리 DB 라 빠르기도함)
         * - Redis 의 INCR 명령어는 단일 쓰레드로 동작하기 때문에 count 에 대한 동시성 문제가 발생하지 않는다.
         * - 결론적으로, count 에 대해 동시성 문제와 totalCount 쿼리 두가지를 동시에 해결한 것이다.
         */

        long count = couponCountRepository.increment();

        if (count > 100) {
            return;
        }

        couponRepository.save(new Coupon(userId));
    }
}
