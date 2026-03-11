package com.springdbhw.features.cat;

import org.springframework.stereotype.Repository;
import org.springframework.data.repository.CrudRepository;

@Repository
public interface CatRepository extends CrudRepository<Cat, String> {}