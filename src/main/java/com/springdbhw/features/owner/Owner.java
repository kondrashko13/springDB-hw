package com.springdbhw.features.owner;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "owners")
public class Owner {
    @Id
    private String id;

    @NotBlank
    @Indexed
    private String fullName;

    @Indexed(unique = true)
    private String email;
}