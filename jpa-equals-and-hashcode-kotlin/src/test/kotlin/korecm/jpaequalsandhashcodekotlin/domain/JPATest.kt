package korecm.jpaequalsandhashcodekotlin.domain

import jakarta.persistence.EntityManager
import jakarta.persistence.EntityManagerFactory
import jakarta.persistence.EntityTransaction
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import java.util.function.Consumer
import java.util.function.Function
import kotlin.reflect.KClass


@DataJpaTest
class JPATest {
    @Autowired
    private lateinit var emf: EntityManagerFactory

    @Test
    fun test() {
        val member1 = Member("member1")
        assertEqualityAndHashCode(Member::class, Member::id, member1)
    }

    private fun <T : Any, U> assertEqualityAndHashCode(kClazz: KClass<T>, getIdFn: Function<T, U>, entity: T) {
        val clazz = kClazz.java
        val set: MutableSet<T> = HashSet()
        set.add(entity)
        assertTrue(set.contains(entity))

        doInJPA { em ->
            em.persist(entity)
            em.flush()
            assertTrue(set.contains(entity), "Persist 후 Entity를 Set에서 찾을 수 없습니다")
        }

        assertTrue(set.contains(entity))

        doInJPA { em ->
            val entityProxy = em.getReference(clazz, getIdFn.apply(entity))
            assertTrue(entityProxy == entity, "EntityProxy가 Entity와 같지 않습니다")
        }

        doInJPA { em ->
            val entityProxy = em.getReference(clazz, getIdFn.apply(entity))
            assertTrue(entity == entityProxy, "Entity가 EntityProxy와 같지 않습니다")
        }

        doInJPA { em ->
            val _entity = em.merge(entity)
            assertTrue(set.contains(_entity), "Merge 후 Entity를 Set에서 찾을 수 없습니다")
        }

        doInJPA { em ->
            val _entity = em.find(clazz, getIdFn.apply(entity))
            assertTrue(
                set.contains(_entity),
                "Entity를 다른 영속성 컨텍스트에 로드 후 Entity를 Set에서 찾을 수 없습니다"
            )
        }

        doInJPA { em ->
            val _entity = em.getReference(clazz, getIdFn.apply(entity))
            assertTrue(
                set.contains(_entity),
                "Entity의 Proxy를 다른 영속성 컨텍스트에 로드 후 Entity를 Set에서 찾을 수 없습니다"
            )
        }

        val deletedEntity = doInJPA<T> { em ->
            val _entity = em.getReference(clazz, getIdFn.apply(entity))
            em.remove(_entity)
            _entity
        }
        assertTrue(set.contains(deletedEntity), "삭제된 Entity를 Set에서 찾을 수 없습니다")
    }

    private fun <T> doInJPA(function: Function<EntityManager, T>): T {
        var result: T
        var txn: EntityTransaction? = null
        try {
            emf.createEntityManager().use { em ->
                txn = em.transaction
                txn!!.begin()
                result = function.apply(em)
                if (!txn!!.rollbackOnly) {
                    txn!!.commit()
                } else {
                    try {
                        txn!!.rollback()
                    } catch (e: Exception) {
                        logger.error("Rollback failure", e)
                    }
                }
            }
        } catch (t: Throwable) {
            if (txn?.isActive == true) {
                try {
                    txn!!.rollback()
                } catch (e: Exception) {
                    logger.error("Rollback failure", e)
                }
            }
            throw t
        }
        return result
    }

    private fun doInJPA(function: Consumer<EntityManager>) {
        doInJPA<Any?> { em ->
            function.accept(em)
            null
        }
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(JPATest::class.java)
    }
}
