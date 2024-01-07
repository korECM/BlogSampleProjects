package korecm.jpaequalsandhashcode.domain;

import jakarta.persistence.Entity;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@NoArgsConstructor(access = PROTECTED)
@Getter
@Entity
public class Member3 extends PKEntity {
    private String name;

    public Member3(String name) {
        this.name = name;
    }
}

