package dev.practice.api;

import dev.practice.api.repository.AppliedUserRepository;
import dev.practice.api.repository.CouponCountRepository;
import dev.practice.api.repository.CouponRepository;
import dev.practice.api.service.ApplyServiceWithRedisAndKafka;
import dev.practice.api.service.LimitedToOneCouponApplyService;
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
public class LimitedToOneCouponSystemTest {

    @Autowired
    private LimitedToOneCouponApplyService limitedToOneCouponApplyService;

    @Autowired
    private CouponCountRepository couponCountRepository;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private AppliedUserRepository appliedUserRepository;

    private Long userId = 1L;

    @BeforeEach
    void setUp() {
        couponCountRepository.reset();
        couponRepository.deleteAll();
        appliedUserRepository.delete(userId);
    }

    @AfterEach
    void tearDown() {
        couponCountRepository.reset();
        couponRepository.deleteAll();
        appliedUserRepository.delete(userId);
    }

    /**
     * Consumer application 을 실행 시킨 상태에서 Test 해야함.
     * - 쿠폰이 100 개만 생성되는지에 대한 동시성 테스트
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
                    limitedToOneCouponApplyService.applyCoupon(userId);
                }finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        Thread.sleep(10000); // consumer 가 모든 메시지를 소비할 때 까지 시간을 기다려준다. 넉넉하게 10초
        // 기다려 주지 않으면 consumer 가 메시지를 모두 소비하기 전에 Test 가 종료되면서 테스트는 실패한다.

        long count = couponRepository.count();

        Assertions.assertThat(count).isEqualTo(100L);
    }

    /**
     * Consumer application 을 실행 시킨 상태에서 Test 해야함.
     * - 1인당 1개의 쿠폰만 발급되는지에 대한 동시성 테스트
     */
    @Test
    void oneCouponConcurrencyTest() throws InterruptedException {

        int threadCount = 1000;

        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch countDownLatch = new CountDownLatch(threadCount);

        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    limitedToOneCouponApplyService.applyCoupon(userId);
                }finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        Thread.sleep(10000); // consumer 가 모든 메시지를 소비할 때 까지 시간을 기다려준다. 넉넉하게 10초
        // 기다려 주지 않으면 consumer 가 메시지를 모두 소비하기 전에 Test 가 종료되면서 테스트는 실패한다.

        long count = couponRepository.count();

        Assertions.assertThat(count).isEqualTo(1L);
    }
}
