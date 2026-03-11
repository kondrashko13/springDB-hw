package com.springdbhw.features.owner;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class Owner {
    private String id;
    private String name;
    private String email;
}