package dev.practice.api.service;

import dev.practice.api.repository.CouponRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class ApplyServiceTest {

    @Autowired
    private ApplyService applyService;

    @Autowired
    private CouponRepository couponRepository;

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
    }

    @AfterEach
    void tearDown() {
        couponRepository.deleteAll();
    }

    // 기본 기능 Test, Local 테스트
    @Test
    void applyCoupon() {
        //given
        long userId = 1L;

        //when
        applyService.applyCoupon(userId);

        //then
        long count = couponRepository.count();
        Assertions.assertThat(count).isEqualTo(1L);
    }

    /**
     * count 에 대한 race condition test 이다.
     *
     * 동일한 userId 에 대한 race condition 은 해당 프로젝트에서 다루지 않는다.
     * -> LimitedToOneCouponApplyService.java 에서 다룬다.
     */
    @Test
    void concurrencyTest() throws InterruptedException {

        int threadCount = 1000;

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++) {
            long userId = i;
            executorService.submit(() -> {
                try {
                    applyService.applyCoupon(userId);
                }finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        long count = couponRepository.count();

        Assertions.assertThatThrownBy(
                () -> Assertions.assertThat(count).isEqualTo(100L)
        ).isInstanceOf(AssertionError.class); // 100 개를 초과하는 쿠폰이 발급된다.
    }
}