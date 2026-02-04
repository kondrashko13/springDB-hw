package com.springdbhw.features.cat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import jakarta.persistence.criteria.Path;
import jakarta.transaction.Transactional;
import org.jooq.*;
import org.jooq.Record;
import org.jooq.impl.DSL;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.jooq.impl.DSL.field;
import static org.jooq.impl.DSL.table;

import org.jooq.Table;

@Component
@Transactional
public class AdvancedCatService {

    private static final String table = Cat.class.getAnnotation(jakarta.persistence.Table.class).name();

    @PersistenceContext
    private EntityManager em;

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
        String sql = "DELETE FROM " + table + " WHERE alive = false";
        em.createNativeQuery(sql).executeUpdate();
    }

    public void deleteDeadJooq() {
        ctx.deleteFrom(table(table))
                .where(field("alive").eq(false))
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
                "UPDATE " + table + " SET age = age + 1 WHERE alive = true"
        ).executeUpdate();
    }

    public void upAgeIfAliveJooq() {
        ctx.update(table(table))
                .set(field("age", Integer.class), field("age", Integer.class).plus(1))
                .where(field("alive").isTrue())
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

    public List<Cat> findOlderThanAverageNative() {
        String sql = """
                    SELECT *
                    FROM cats
                    WHERE age > (
                        SELECT AVG(age)
                        FROM cats
                    )
                """;
        TypedQuery<Cat> query = em.createQuery(sql, Cat.class);
        return query.getResultList();
    }

    public List<Cat> findOlderThanAverageJooq() {
        int avgAge = DSL
                .select(DSL.avg(field("age", Integer.class)))
                .from(table(table)).execute();

        return ctx.selectFrom(table(table))
                .where(field("age", Integer.class).gt(avgAge))
                .fetchInto(Cat.class);
    }

    //Using aggregation in result---------------------------------------------------------------------------------------
    public List<Object[]> countByBreedJPQL() {
        String jpql = """
                    SELECT c.age, COUNT(c)
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

    public List<Object[]> countByBreedNative() {
        String sql = """
                    SELECT c.breed, COUNT(*)
                    FROM cats c
                    GROUP BY c.breed
                    ORDER BY c.breed
                """;
        TypedQuery<Object[]> query = em.createQuery(sql, Object[].class);
        return query.getResultList();
    }

    public Result<Record2<Integer, Integer>> countCatsByAgeJooq() {
        Field<Integer> breed = field("breed", Integer.class);

        return ctx.select(breed, DSL.count())
                .from(table(table))
                .groupBy(breed)
                .orderBy(breed)
                .fetch();
    }

    //JOIN search-------------------------------------------------------------------------------------------------------
    public List<Cat> findByOwnerJPQL(String name) {
        String jpql = """
                    SELECT c
                    FROM Cat c
                    JOIN c.owner o
                    WHERE o.name LIKE :ownerName
                """;

        return em.createQuery(jpql, Cat.class)
                .setParameter("ownerName", name + "%")
                .getResultList();
    }

    public List<Cat> findByOwnerNamedQuery(String name) {
        return em.createNamedQuery("Cat.findByOwnerName", Cat.class)
                .setParameter("ownerName", name + "%")
                .getResultList();
    }

    public List<Cat> findByOwnerCriteria(String name) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Cat> cq = cb.createQuery(Cat.class);
        Root<Cat> cat = cq.from(Cat.class);

        Join<Cat, Owner> owner = cat.join("owner");
        cq.select(cat)
                .where(cb.like(owner.get("name"), name + "%"));

        return em.createQuery(cq).getResultList();
    }

//    public List<Cat> findByOwnerNative(String name) {
//        String sql = """
//                    SELECT *
//                    FROM cats c1
//                    WHERE c1.id IN (
//                        SELECT c.id
//                        FROM cats c
//                        JOIN owners o ON c.owner_id = o.id
//                        WHERE o.name = LIKE :name
//                    )
//                """;
//        TypedQuery<Cat> query = em.createNativeQuery(sql, Cat.class);
//        return query.getResultList();
//    }

    public List<Cat> findByOwnerJooq(String name) {
        String ownerTableName = Owner.class.getAnnotation(jakarta.persistence.Table.class).name();

        Table<Record> catTable = table(table);
        Table<Record> ownerTable = table(ownerTableName);

        Field<Long> catOwnerId = catTable.field("owner_id", Long.class);
        Field<Long> ownerId = ownerTable.field("id", Long.class);

        return ctx.select(catTable.fields())
                .from(catTable)
                .join(ownerTable)
                .on(catOwnerId.eq(ownerId))
                .where(ownerTable.field("name").like(name + "%"))
                .fetchInto(Cat.class);
    }


}
