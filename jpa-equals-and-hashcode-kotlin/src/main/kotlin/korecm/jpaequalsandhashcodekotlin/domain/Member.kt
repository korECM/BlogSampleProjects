package korecm.jpaequalsandhashcodekotlin.domain

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import org.hibernate.proxy.HibernateProxy

@Entity
class Member(val name: String) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        val oEffectiveClass = getEffectiveClass(other)
        val thisEffectiveClass = getEffectiveClass(this)
        if (thisEffectiveClass != oEffectiveClass) return false
        other as Member

        return id != 0L && id == other.id
    }

    final override fun hashCode(): Int = getEffectiveClass(this).hashCode()

    private fun getEffectiveClass(obj: Any?): Class<*> =
        if (obj is HibernateProxy) (this as HibernateProxy).hibernateLazyInitializer.persistentClass else this.javaClass
}