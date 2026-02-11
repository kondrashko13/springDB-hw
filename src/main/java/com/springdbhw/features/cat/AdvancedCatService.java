package com.springdbhw.features.cat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Path;
import jakarta.transaction.Transactional;

import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

import static org.jooq.impl.DSL.*;

@Component
@Transactional
public class AdvancedCatService {

    @PersistenceContext
    private EntityManager em;

    private static final Table<?> CATS = table("cats");
    private static final Table<?> OWNERS = table("owners");

    private static final Field<Boolean> ALIVE = field("alive", Boolean.class);
    private static final Field<Integer> AGE = field("age", Integer.class);
    private static final Field<Integer> BREED = field("breed", Integer.class);
    private static final Field<String> NAME = field("name", String.class);

    private final DSLContext ctx;

    public AdvancedCatService(DSLContext ctx) {
        this.ctx = ctx;
    }

    // Deleting on condition -------------------------------------------------------------------------------------------
    public void deleteDeadJPQL() {
        String jpql = "DELETE FROM Cat c WHERE c.alive = false";
        em.createQuery(jpql).executeUpdate();
    }

    public void deleteDeadNamedQuery() {
        em.createNamedQuery("Cat.deleteDead").executeUpdate();
    }

    public void deleteDeadCriteria() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaDelete<Cat> delete = cb.createCriteriaDelete(Cat.class);
        Root<Cat> root = delete.from(Cat.class);

        delete.where(cb.isFalse(root.get("alive")));

        em.createQuery(delete).executeUpdate();
    }

    public void deleteDeadNative() {
        String sql = "DELETE FROM cats WHERE alive = false";
        em.createNativeQuery(sql).executeUpdate();
    }

    public void deleteDeadJooq() {
        ctx.deleteFrom(CATS)
                .where(ALIVE.isFalse())
                .execute();
    }

    // Updating on condition -------------------------------------------------------------------------------------------
    public void upAgeIfAliveJPQL() {
        String jpql = "UPDATE Cat c SET c.age = c.age + 1 WHERE c.alive = true";
        em.createQuery(jpql).executeUpdate();
    }

    public void upAgeIfAliveNamedQuery() {
        em.createNamedQuery("Cat.upAgeIfAlive").executeUpdate();
    }

    public void upAgeIfAliveCriteria() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaUpdate<Cat> update = cb.createCriteriaUpdate(Cat.class);
        Root<Cat> root = update.from(Cat.class);

        Path<Integer> age = root.get("age");
        update
                .set(age, cb.sum(age, 1))
                .where(cb.isTrue(root.get("alive")));

        em.createQuery(update).executeUpdate();
    }

    public void upAgeIfAliveNative() {
        em.createNativeQuery(
                "UPDATE cats SET age = age + 1 WHERE alive = true"
        ).executeUpdate();
    }

    public void upAgeIfAliveJooq() {
        ctx.update(CATS)
                .set(AGE, AGE.plus(1))
                .where(ALIVE.isTrue())
                .execute();
    }

    //Using aggregation in condition------------------------------------------------------------------------------------
    public List<Cat> findOlderThanAverageJPQL() {
        String jpql = """
                    SELECT c
                    FROM Cat c
                    WHERE c.age > (
                        SELECT AVG(c2.age)
                        FROM Cat c2
                    )
                """;
        return em.createQuery(jpql, Cat.class).getResultList();
    }

    public List<Cat> findOlderThanAverageNamedQuery() {
        return em.createNamedQuery("Cat.findOlderThanAverage", Cat.class)
                .getResultList();
    }

    public List<Cat> findOlderThanAverageCriteria() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Cat> cq = cb.createQuery(Cat.class);
        Root<Cat> root = cq.from(Cat.class);

        Subquery<Double> avgAge = cq.subquery(Double.class);
        Root<Cat> subRoot = avgAge.from(Cat.class);
        avgAge.select(cb.avg(subRoot.get("age")));

        cq.select(root).where(cb.greaterThan(root.get("age"), avgAge));

        return em.createQuery(cq).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Cat> findOlderThanAverageNative() {
        String sql = """
                    SELECT *
                    FROM cats
                    WHERE age > (
                        SELECT AVG(age)
                        FROM cats
                    )
                """;
        return em.createNativeQuery(sql, Cat.class).getResultList();
    }

    public List<Cat> findOlderThanAverageJooq() {
        BigDecimal avgAgeBD = ctx
                .select(DSL.avg(AGE))
                .from(CATS)
                .fetchOne(DSL.avg(AGE));

        if (avgAgeBD == null) {
            return List.of();
        }
        int avgAge = avgAgeBD.intValue();

        return ctx.selectFrom(CATS)
                .where(AGE.gt(avgAge))
                .fetchInto(Cat.class);
    }

    //Using aggregation in result---------------------------------------------------------------------------------------
    public List<Object[]> countByBreedJPQL() {
        String jpql = """
                    SELECT c.breed, COUNT(c)
                    FROM Cat c
                    GROUP BY c.breed
                    ORDER BY c.breed
                """;

        return em.createQuery(jpql, Object[].class).getResultList();
    }

    public List<Object[]> countByBreedNamedQuery() {
        return em.createNamedQuery("Cat.countByBreed", Object[].class).getResultList();
    }

    public List<Object[]> countByBreedCriteria() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Object[]> cq = cb.createQuery(Object[].class);
        Root<Cat> root = cq.from(Cat.class);

        cq.select(cb.array(
                        root.get("breed"),
                        cb.count(root)
                ))
                .groupBy(root.get("breed"))
                .orderBy(cb.asc(root.get("breed")));

        return em.createQuery(cq).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Object[]> countByBreedNative() {
        String sql = """
                    SELECT c.breed, COUNT(c)
                    FROM cats c
                    GROUP BY c.breed
                    ORDER BY c.breed
                """;
        return em.createNativeQuery(sql, Object[].class).getResultList();
    }

    public List<Object[]> countCatsByAgeJooq() {
        return ctx.select(BREED, DSL.count())
                .from(CATS)
                .groupBy(BREED)
                .orderBy(BREED)
                .fetchInto(Object[].class);
    }

    //Join search-------------------------------------------------------------------------------------------------------
    public Map<Owner, List<Cat>> findCatsGroupedByOwnerJPQL() {
        String jpql = """
                SELECT c
                FROM Cat c
                JOIN FETCH c.owner
                """;
        List<Cat> cats = em.createQuery(jpql, Cat.class).getResultList();
        return cats.stream().collect(Collectors.groupingBy(Cat::getOwner));
    }

    public Map<Owner, List<Cat>> findCatsGroupedByOwnerNamedQuery() {
        List<Cat> cats = em.createNamedQuery("Cat.findAllWithOwner", Cat.class).getResultList();
        return cats.stream().collect(Collectors.groupingBy(Cat::getOwner));
    }

    public Map<Owner, List<Cat>> findCatsGroupedByOwnerCriteria() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Cat> query = cb.createQuery(Cat.class);
        Root<Cat> catRoot = query.from(Cat.class);

        catRoot.fetch("owner", JoinType.INNER);

        query.select(catRoot);

        List<Cat> cats = em.createQuery(query).getResultList();

        return cats.stream().collect(Collectors.groupingBy(Cat::getOwner));
    }

    @SuppressWarnings("unchecked")
    public Map<Owner, List<Cat>> findCatsGroupedByOwnerNative() {
        String sql = """
                SELECT c.*
                FROM cats c
                JOIN owners o ON c.owner_id = o.id
                """;
        List<Cat> cats = em.createNativeQuery(sql, Cat.class).getResultList();

        return cats.stream().collect(Collectors.groupingBy(Cat::getOwner));
    }

    public Map<Owner, List<Cat>> findCatsGroupedByOwnerJOOQ() {
        Table<Record> cats = table("cats");
        Table<Record> owners = table("owners");

        Field<Long> catId = field(name("cats", "id"), Long.class);
        Field<String> catName = field(name("cats", "name"), String.class);
        Field<Integer> catAge = field(name("cats", "age"), Integer.class);
        Field<Long> catOwnerId = field(name("cats", "owner_id"), Long.class);
        Field<Integer> catBreed = field(name("cats", "breed"), Integer.class);

        Field<Long> ownerId = field(name("owners", "id"), Long.class);
        Field<String> ownerName = field(name("owners", "name"), String.class);

        Result<Record7<Long, String, Long, Long, String, Integer, Integer>> records = ctx
                .select(catId, catName, catOwnerId, ownerId, ownerName, catAge, catBreed)
                .from(cats)
                .join(owners)
                .on(catOwnerId.eq(ownerId))
                .fetch();

        List<Cat> catsList = new ArrayList<>();
        Map<Long, Owner> ownersMap = new HashMap<>();

        for (Record r : records) {
            Cat cat = new Cat();
            cat.setId(r.get(catId));
            cat.setName(r.get(catName));
            cat.setAge(r.get(catAge));
            cat.setBreed(Breed.toBreed(r.get(catBreed)));

            Long oId = r.get(ownerId);
            Owner owner = ownersMap.get(oId);
            if (owner == null) {
                owner = new Owner();
                owner.setId(oId);
                owner.setName(r.get(ownerName));
                ownersMap.put(oId, owner);
            }

            cat.setOwner(owner);
            catsList.add(cat);
        }
        return catsList.stream().collect(Collectors.groupingBy(Cat::getOwner));
    }

    //Parametrised search-------------------------------------------------------------------------------------------------------
    public List<Cat> findDynamicallyJPQL(String name, Integer age) {
        StringBuilder jpql = new StringBuilder("SELECT c FROM Cat c WHERE 1=1");

        if (name != null) {
            jpql.append(" AND LOWER(c.name) LIKE LOWER(CONCAT(:name, '%'))");
        }

        if (age != null) {
            jpql.append(" AND c.age > :age");
        }

        TypedQuery<Cat> query = em.createQuery(jpql.toString(), Cat.class);

        if (name != null) {
            query.setParameter("name", name);
        }

        if (age != null) {
            query.setParameter("age", age);
        }

        return query.getResultList();
    }

    public List<Cat> findDynamicallyCriteria(String name, Integer age) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Cat> cq = cb.createQuery(Cat.class);
        Root<Cat> cat = cq.from(Cat.class);

        List<Predicate> predicates = new ArrayList<>();

        if (name != null) {
            predicates.add(
                    cb.like(
                            cb.lower(cat.get("name")),
                            name.toLowerCase() + "%"
                    )
            );
        }

        if (age != null) {
            predicates.add(
                    cb.greaterThan(cat.get("age"), age)
            );
        }

        cq.select(cat).where(predicates.toArray(new Predicate[0]));

        return em.createQuery(cq).getResultList();
    }

    @SuppressWarnings("unchecked")
    public List<Cat> findDynamicallyNative(String name, Integer age) {
        StringBuilder sql = new StringBuilder("SELECT * FROM cats WHERE 1=1");

        if (name != null) {
            sql.append(" AND LOWER(name) LIKE LOWER(:name)");
        }

        if (age != null) {
            sql.append(" AND age > :age");
        }

        jakarta.persistence.Query query = em.createNativeQuery(sql.toString(), Cat.class);

        if (name != null) {
            query.setParameter("name", name + "%");
        }

        if (age != null) {
            query.setParameter("age", age);
        }

        return query.getResultList();
    }


    public List<Cat> findDynamicallyJOOQ(String name, Integer age) {
        Condition condition = DSL.trueCondition();

        if (name != null) {
            condition = condition.and(
                    DSL.lower(field("name", String.class)).like(name.toLowerCase() + "%")
            );
        }

        if (age != null) {
            condition = condition.and(
                    field("age", Integer.class).gt(age)
            );
        }

        return ctx
                .selectFrom(CATS)
                .where(condition)
                .fetchInto(Cat.class);
    }
}
