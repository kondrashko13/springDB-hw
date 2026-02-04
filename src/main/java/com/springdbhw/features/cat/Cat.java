package com.springdbhw.features.cat;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@NamedQuery(
        name = "Cat.deleteDead",
        query = "DELETE FROM Cat c WHERE c.alive = false"
)
@NamedQuery(
        name = "Cat.upAgeIfAlive",
        query = "UPDATE Cat c SET c.age = c.age + 1 WHERE c.alive = true"
)
@NamedQuery(
        name = "Cat.findOlderThanAverage",
        query = """
                    SELECT c
                    FROM Cat c
                    WHERE c.age > (
                        SELECT AVG(c2.age)
                        FROM Cat c2
                    )
                """
)
@NamedQuery(
        name = "Cat.countByBreed",
        query = """
                    SELECT c.age, COUNT(c)
                    FROM Cat c
                    GROUP BY c.breed
                    ORDER BY c.breed
                """
)
@NamedQuery(
        name = "Cat.findByOwnerName",
        query = """
                    SELECT c
                    FROM Cat c
                    JOIN c.owner o
                    WHERE o.name LIKE :ownerName
                """
)
@Table(name = "cats")
public class Cat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotNull
    @Min(0)
    @Builder.Default
    private int age = 0;

    @NotNull
    @Builder.Default
    private boolean alive = true;

    @NotNull
    @Builder.Default
    private Breed breed = Breed.CUTE;

    @ManyToOne
    private Owner owner;
}

