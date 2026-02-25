package com.springdbhw.features.vet_visit;

import com.mongodb.client.result.DeleteResult;
import com.springdbhw.features.cat.Cat;
import com.springdbhw.features.owner.Owner;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VetVisitService {

    private final MongoTemplate mongoTemplate;

    // 7.1: Додавання --------------------------------------------------------------------------------------------------
    public Cat addNewCat(Cat cat) {
        return mongoTemplate.insert(cat);
    }

    // 7.2: Видалення --------------------------------------------------------------------------------------------------
    public long deleteVisitsByDiagnosis(String diagnosis) {
        Query query = new Query(Criteria.where("diagnosis").is(diagnosis));
        DeleteResult result = mongoTemplate.remove(query, VetVisit.class);
        return result.getDeletedCount();
    }

    // 7.3: Пошук ------------------------------------------------------------------------------------------------------
    public List<Owner> findOwnersByEmailDomain(String domain) {
        Query query = new Query(Criteria.where("email").regex("@" + domain + "$", "i"));
        return mongoTemplate.find(query, Owner.class);
    }
}
