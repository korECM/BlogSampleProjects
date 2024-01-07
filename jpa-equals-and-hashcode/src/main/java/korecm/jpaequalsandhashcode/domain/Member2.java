package korecm.jpaequalsandhashcode.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
public class Member2 extends PKEntity {
    private String name;

    public Member2(String name) {
        this.name = name;
    }
}

