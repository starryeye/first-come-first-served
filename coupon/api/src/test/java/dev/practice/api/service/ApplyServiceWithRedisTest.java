package dev.practice.api.service;

import dev.practice.api.repository.CouponCountRepository;
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

@SpringBootTest
class ApplyServiceWithRedisTest {

    @Autowired
    private ApplyServiceWithRedis applyServiceWithRedis;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private CouponCountRepository couponCountRepository;

    @BeforeEach
    void setUp() {
        couponRepository.deleteAll();
        couponCountRepository.reset();
    }

    @AfterEach
    void tearDown() {
        couponRepository.deleteAll();
        couponCountRepository.reset();
    }

    // 기본 기능 Test, Local 테스트
    @Test
    void applyCoupon() {
        //given
        long userId = 1L;

        //when
        applyServiceWithRedis.applyCoupon(userId);

        //then
        long count = couponRepository.count();
        Assertions.assertThat(count).isEqualTo(1L);
    }

    /**
     * count 에 대한 race condition 해결
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
                    applyServiceWithRedis.applyCoupon(userId);
                }finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        long count = couponRepository.count();

        Assertions.assertThat(count).isEqualTo(100L);
    }
}