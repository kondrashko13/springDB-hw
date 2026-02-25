package com.springdbhw;

import org.springframework.dao.DuplicateKeyException;
import com.springdbhw.features.owner.Owner;
import com.springdbhw.features.owner.OwnerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.IndexInfo;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class MongoIndexTest {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest");

    @Autowired
    private OwnerRepository ownerRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @BeforeEach
    void setUp() {
        ownerRepository.deleteAll();
    }

    @Test
    void shouldThrowExceptionWhenSavingDuplicateEmail() {
        Owner o1 = Owner.builder().email("test@mail.com").fullName("Owner 1").build();
        Owner o2 = Owner.builder().email("test@mail.com").fullName("Owner 2").build();

        ownerRepository.save(o1);

        assertThrows(DuplicateKeyException.class, () -> ownerRepository.save(o2));
    }

    @Test
    void shouldVerifyIndexExistsOnFullNameField() {
        List<IndexInfo> indexInfoList = mongoTemplate.indexOps(Owner.class).getIndexInfo();

        boolean emailIndexExists = indexInfoList.stream()
                .anyMatch(info -> info.getIndexFields().stream()
                        .anyMatch(field -> field.getKey().equals("fullName")));

        assertThat(emailIndexExists).isTrue();
    }
}
