package com.springdbhw.features.vet_visit;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Document(collection = "vet_visits")
public class VetVisit {

    @Id
    private String id;

    @NotBlank
    @Indexed
    private String catId;

    @NotNull
    @PastOrPresent
    private LocalDate visitDate;

    @NotBlank
    @Builder.Default
    private String diagnosis = "Healthy";

    @Builder.Default
    private String prescribedTreatment = "";

    @Builder.Default
    private double cost = 0.0;
}
