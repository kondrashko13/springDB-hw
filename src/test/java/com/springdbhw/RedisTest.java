package com.springdbhw;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.springdbhw.features.cat.Cat;
import com.springdbhw.features.cat.CatRepository;
import com.springdbhw.features.cat.CatService;
import com.springdbhw.features.owner.Owner;
import com.springdbhw.features.owner.OwnerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class RedisTest {

    @Container
    @SuppressWarnings("resource")
    private static final GenericContainer<?> REDIS_CONTAINER =
            new GenericContainer<>(DockerImageName.parse("redis:latest")).withExposedPorts(6379);

    @DynamicPropertySource
    static void redisProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", REDIS_CONTAINER::getFirstMappedPort);
    }

    @Autowired private OwnerService ownerService;
    @Autowired private CatRepository catRepository;
    @Autowired private CatService catService;
    @Autowired private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void cleanUp() {
        stringRedisTemplate.execute((RedisCallback<Void>) connection -> {
            connection.serverCommands().flushDb();
            return null;
        });
    }

    // 1. --------------------------------------------------------------------------------------------------------------
    @Test
    void testJsonCrudProfile() throws JsonProcessingException {
        Owner owner = new Owner("user1", "John", "john@example.com");
        ownerService.saveOwner(owner);

        Owner fetched = ownerService.getOwnerById("user1");
        assertThat(fetched).isNotNull();
        assertThat(fetched.getName()).isEqualTo("John");

        ownerService.deleteOwner("user1");
        assertThat(ownerService.getOwnerById("user1")).isNull();
    }

    // 2. --------------------------------------------------------------------------------------------------------------
    @Test
    void testRateLimiter() throws InterruptedException {
        String userId = "testUser";

        assertThat(ownerService.allowRequest(userId, 2, 2)).isTrue();
        assertThat(ownerService.allowRequest(userId, 2, 2)).isTrue();
        assertThat(ownerService.allowRequest(userId, 2, 2)).isFalse();

        Thread.sleep(2100);

        assertThat(ownerService.allowRequest(userId, 2, 2)).isTrue();
    }

    // 3. 4. 6. --------------------------------------------------------------------------------------------------------
    @Test
    void testHashCrudAndPartialUpdate() {
        Cat cat = new Cat("cat1", "Tom", 3);
        catRepository.save(cat);

        Optional<Cat> fetched = catRepository.findById("cat1");
        assertThat(fetched).isPresent();
        assertThat(fetched.get().getName()).isEqualTo("Tom");

        Map<String, String> updates = Map.of(
                "name", "Thomas",
                "age", "4"
        );
        catService.patchCat("cat1", updates);

        Cat updatedCat = catRepository.findById("cat1").get();
        assertThat(updatedCat.getName()).isEqualTo("Thomas");
        assertThat(updatedCat.getAge()).isEqualTo(4);

        catRepository.deleteById("cat1");
        assertThat(catRepository.findById("cat1")).isEmpty();
    }
}
