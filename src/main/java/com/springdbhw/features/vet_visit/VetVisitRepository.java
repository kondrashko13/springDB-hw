package com.springdbhw.features.vet_visit;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface VetVisitRepository extends MongoRepository<VetVisit, String> {
    // 5.3: Derived Query ----------------------------------------------------------------------------------------------
    List<VetVisit> findByCatIdAndVisitDateAfter(String catId, LocalDate date);

    // 6.3: @Query -----------------------------------------------------------------------------------------------------
    @Query(value = "{ 'cost' : 0.0 }", delete = true)
    long deleteFreeVisits();
}
