package dev.practice.api.service;

import dev.practice.api.domain.Coupon;
import dev.practice.api.repository.CouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class ApplyService {

    private final CouponRepository couponRepository;

    public void applyCoupon(Long userId) {
        /**
         * 쿠폰 발급
         * 1. 발급된 쿠폰의 갯수를 조회한다.
         * 2. 발급된 쿠폰의 갯수가 100 개 이상이면 쿠폰을 발급하지 않는다.
         * 3. 100 개 미만이면 쿠폰을 발급한다. (DB 에 적재)
         *
         * 문제점
         * 1. 동일한 userId 에 대한 쿠폰 발급이 동시에 일어날 수 있다.
         * -> 해당 프로젝트에서는 다루지 않을 것이다.
         * 2. 동시에 쿠폰 발급이 대규모로 요청되면 100 개를 초과하는 쿠폰이 발급될 수 있다.
         * -> 해결해보자
         */

        long count = couponRepository.count();

        if (count >= 100) {
            return;
        }

        couponRepository.save(new Coupon(userId));
    }
}
