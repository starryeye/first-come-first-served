package dev.practice.api.service;

import dev.practice.api.producer.CouponCreateProducer;
import dev.practice.api.repository.AppliedUserRepository;
import dev.practice.api.repository.CouponCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class LimitedToOneCouponApplyService {

    private final CouponCountRepository couponCountRepository;
    private final CouponCreateProducer couponCreateProducer;
    private final AppliedUserRepository appliedUserRepository;

    public void applyCoupon(Long userId) {
        /**
         * 쿠폰 발급
         * 1. 발급된 쿠폰의 갯수를 1 증가 시키고 갯수를 리턴받는다. (Redis 활용)
         * 2. 발급된 쿠폰의 갯수가 100 개 초과이면 쿠폰을 발급하지 않는다.
         * 3. 100 개 이하이면 쿠폰을 발급한다. (DB 에 적재, 발급 전에 Redis 에서 갯수를 1 증가시키므로 100개 이하면 발급한다.)
         *
         * 발급 조건 추가
         * 0. 1인당 1개의 쿠폰만 발급 가능하도록 한다.
         *
         * 고민.
         * - 1. 쿠폰 종류 컬럼(무슨 이벤트에 대한 쿠폰인지)을 추가하고 유저 아이디와 쿠폰 타입에 DB 에서 유니크 설정을 한다.
         * -> 문제1. 이벤트에 따라 1명당 쿠폰 발급 횟수 제한이 없는 경우가 있을 수 있으므로 적절한 방법이 아니다.
         * - 2. applyCoupon 메서드 전체 범위로 락을 걸고 쿠폰 발급 여부를 먼저 체크하는 로직을 넣는다.
         * -> 문제1. 현재 kafka 를 통해 비동기로 쿠폰 발급 로직을 빼놔서 락의 의미가 없어진다.(한명이 여러개의 쿠폰을 발급 받을 수 있다.)
         * -> 문제2. 문제2. 비동기 처리를 하지 않고 동기로 쿠폰 발급 처리를 한다고 쳐도 락의 범위가 너무 넓다.
         * - 3. redis 의 set data type 을 활용한다.
         * -> userId 를 set 의 value 로 넣어 유니크 체크를 한다.
         * -> key 는 쿠폰의 종류로 한다. 현재 예제에서는 coupon:applied:user
         * -> 해당 방법으로 구현
         */

        Long apply = appliedUserRepository.add(userId);

        if(apply != 1) {
            return; // redis set 은 1을 리턴하면 정상 적재된 것이고, 나머지의 값은 유니크하지 않아서 적재되지 않은 것이다.
            // 즉, 1이면 해당 유저는 해당 쿠폰을 발급하지 않은 것이다.
        }

        long count = couponCountRepository.increment();

        if (count > 100) {
            return;
        }

        couponCreateProducer.sendCouponCreate(userId);
    }
}
