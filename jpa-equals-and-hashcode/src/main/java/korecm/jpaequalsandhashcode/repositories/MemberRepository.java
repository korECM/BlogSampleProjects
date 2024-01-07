package korecm.jpaequalsandhashcode.repositories;

import korecm.jpaequalsandhashcode.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {
}