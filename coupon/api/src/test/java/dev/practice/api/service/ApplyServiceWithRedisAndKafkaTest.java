package dev.practice.api.service;

import dev.practice.api.repository.CouponCountRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SpringBootTest
class ApplyServiceWithRedisAndKafkaTest {

    @Autowired
    private ApplyServiceWithRedisAndKafka applyServiceWithRedisAndKafka;

    @Autowired
    private CouponCountRepository couponCountRepository;

    @BeforeEach
    void setUp() {
        couponCountRepository.reset();
    }

    @AfterEach
    void tearDown() {
        couponCountRepository.reset();
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
                    applyServiceWithRedisAndKafka.applyCoupon(userId);
                }finally {
                    countDownLatch.countDown();
                }
            });
        }

        countDownLatch.await();

        //터미널에서 consumer 키고 확인..
        //TODO, kafka consumer 코드로 확인
    }
}