package com.springdbhw.features.cat;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class CatService {
    private final StringRedisTemplate stringRedisTemplate;
    private final CatRepository catRepository;

    // 4. Universal partial update -------------------------------------------------------------------------------------
    public void patchCat(String catId, Map<String, String> patch) {
        String key = "cats:" + catId;
        stringRedisTemplate.opsForHash().putAll(key, patch);
    }
}
