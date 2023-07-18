package dev.practice.api.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class AppliedUserRepository {

    private final StringRedisTemplate stringRedisTemplate;

    public Long add(Long userId) {
        return stringRedisTemplate.opsForSet()
                .add("coupon:applied:user", String.valueOf(userId));
    }

    public void delete(Long userId) {
        stringRedisTemplate.opsForSet()
                .remove("coupon:applied:user", String.valueOf(userId));
    }
}
