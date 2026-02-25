package com.springdbhw.features.owner;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OwnerRepository extends MongoRepository<Owner, String> {
    // 5.1: Derived ----------------------------------------------------------------------------------------------------
    Optional<Owner> findByEmail(String email);

    // 6.1: @Query -----------------------------------------------------------------------------------------------------
    @Query("{ 'fullName': { $regex: ?0, $options: 'i' } }")
    List<Owner> findOwnersByFullNameRegex(String regex);
}
