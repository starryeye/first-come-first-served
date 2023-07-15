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
         *
         * 문제점
         * - 일반적으로 DB 는 coupon 발급에 해당하는 데이터만을 다루진 않는다.
         * - 예를 들어 쿠폰 발급 저장이 1분이 걸린다고 가정하면..
         * - DB 커넥션이 10 개이고 쿠폰 발급 요청이 1000 개가 들어온다면,
         * - 약 100분에 가까운 시간이 소요된다.. (쿠폰 발급에 대한 timeout 발생도 가능하다.. redis 에서 걸리는 시간은 O(1) 이므로 무시)
         * - 그리고.. 쿠폰 발급외의 다른 비즈니스 로직은 수행되지 못하고 대기하거나 timeout 이 발생하게 된다.
         * - 결론적으로, tomcat 스레드 풀 또는 DB 커넥션 풀이 모두 고갈되어 서비스가 불가능한 상태가 된다.
         */

        long count = couponCountRepository.increment();

        if (count > 100) {
            return;
        }

        couponRepository.save(new Coupon(userId));
    }
}
