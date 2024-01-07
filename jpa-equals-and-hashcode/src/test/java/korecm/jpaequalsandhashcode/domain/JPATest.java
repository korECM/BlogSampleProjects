package korecm.jpaequalsandhashcode.domain;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
class JPATest {
    private static final Logger logger = LoggerFactory.getLogger(JPATest.class);

    @Autowired
    EntityManagerFactory emf;

    @Test
    void test() {
        Member member1 = new Member("member1");

        assertEqualityAndHashCode(Member.class, Member::getId, member1);
    }

    @Test
    void test2() {
        Member2 member2 = new Member2("member2");
        Member3 member3 = new Member3("member3");

        Assertions.assertNotEquals(member2, member3);
    }

    private <T, U> void assertEqualityAndHashCode(Class<T> clazz, Function<T, U> getIdFn, T entity) {
        Set<T> set = new HashSet<>();
        set.add(entity);
        assertTrue(set.contains(entity));

        doInJPA(em -> {
            em.persist(entity);
            em.flush();
            assertTrue(set.contains(entity), "Persist 후 Entity를 Set에서 찾을 수 없습니다");
        });

        assertTrue(set.contains(entity));

        doInJPA(em -> {
            T entityProxy = em.getReference(clazz, getIdFn.apply(entity));
            assertTrue(entityProxy.equals(entity), "EntityProxy가 Entity와 같지 않습니다");
        });

        doInJPA(em -> {
            T entityProxy = em.getReference(clazz, getIdFn.apply(entity));
            assertTrue(entity.equals(entityProxy), "Entity가 EntityProxy와 같지 않습니다");
        });

        doInJPA(em -> {
            T _entity = em.merge(entity);
            assertTrue(set.contains(_entity), "Merge 후 Entity를 Set에서 찾을 수 없습니다");
        });

        doInJPA(em -> {
            T _entity = em.find(clazz, getIdFn.apply(entity));
            assertTrue(set.contains(_entity), "Entity를 다른 영속성 컨텍스트에 로드 후 Entity를 Set에서 찾을 수 없습니다");
        });

        doInJPA(em -> {
            T _entity = em.getReference(clazz, getIdFn.apply(entity));
            assertTrue(set.contains(_entity), "Entity의 Proxy를 다른 영속성 컨텍스트에 로드 후 Entity를 Set에서 찾을 수 없습니다");
        });

        T deletedEntity = doInJPA(em -> {
            T _entity = em.getReference(clazz, getIdFn.apply(entity));
            em.remove(_entity);
            return _entity;
        });
        assertTrue(set.contains(deletedEntity), "삭제된 Entity를 Set에서 찾을 수 없습니다");
    }

    private <T> T doInJPA(Function<EntityManager, T> function) {
        T result;
        EntityTransaction txn = null;
        try (EntityManager em = emf.createEntityManager()) {
            txn = em.getTransaction();
            txn.begin();
            result = function.apply(em);
            if (!txn.getRollbackOnly()) {
                txn.commit();
            } else {
                try {
                    txn.rollback();
                } catch (Exception e) {
                    logger.error("Rollback failure", e);
                }
            }
        } catch (Throwable t) {
            if (txn != null && txn.isActive()) {
                try {
                    txn.rollback();
                } catch (Exception e) {
                    logger.error("Rollback failure", e);
                }
            }
            throw t;
        }
        return result;
    }

    private void doInJPA(Consumer<EntityManager> function) {
        doInJPA(em -> {
            function.accept(em);
            return null;
        });
    }
}