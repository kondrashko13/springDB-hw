package com.springdbhw.features.cat;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@RedisHash("cats")
public class Cat {
    @Id
    private String id;

    private String name;

    @Builder.Default
    private int age = 0;
}

