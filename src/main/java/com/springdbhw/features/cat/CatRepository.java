package com.springdbhw.features.cat;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CatRepository extends MongoRepository<Cat, String> {
    // 5.2: Derived Query ----------------------------------------------------------------------------------------------
    long deleteByOwnerId(String ownerId);

    // 6.2: @Query -----------------------------------------------------------------------------------------------------
    @Query("{ 'age' : { $gt: ?0 } }")
    List<Cat> findCatsOlderThan(int age);
}