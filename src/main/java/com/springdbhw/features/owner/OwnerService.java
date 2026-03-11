package com.springdbhw.features.owner;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class OwnerService {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    private static final String PROFILE_PREFIX = "profile:owner:";
    private static final String RATE_LIMIT_PREFIX = "rate-limit:";

    // 1. CRUD для JSON ------------------------------------------------------------------------------------------------
    public void saveOwner(Owner owner) {
        String json = objectMapper.writeValueAsString(owner);
        stringRedisTemplate.opsForValue().set(PROFILE_PREFIX + owner.getId(), json);
    }

    public Owner getOwnerById(String id) {
        String json = stringRedisTemplate.opsForValue().get(PROFILE_PREFIX + id);
        return json != null ? objectMapper.readValue(json, Owner.class) : null;
    }

    public void deleteOwner(String id) {
        stringRedisTemplate.delete(PROFILE_PREFIX + id);
    }

    // 2. Rate Limiter -------------------------------------------------------------------------------------------------
    public boolean allowRequest(String userId, int maxRequests, int durationSeconds) {
        String key = RATE_LIMIT_PREFIX + userId;
        Long currentCount = stringRedisTemplate.opsForValue().increment(key);

        if (currentCount != null && currentCount == 1) {
            stringRedisTemplate.expire(key, Duration.ofSeconds(durationSeconds));
        }

        return currentCount != null && currentCount <= maxRequests;
    }
}
