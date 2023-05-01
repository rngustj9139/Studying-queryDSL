package koo.basicquerydsl.repository;

import koo.basicquerydsl.entity.Member;
import koo.basicquerydsl.entity.QMember;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberSprinDataJpaRepositoryTest {

    @Autowired
    EntityManager em;

    @Autowired
    MemberSprinDataJpaRepository memberSprinDataJpaRepository;

    @Test
    public void basicTest() {
        Member member = new Member("member1", 10);
        memberSprinDataJpaRepository.save(member);
        Member findMember = memberSprinDataJpaRepository.findById(member.getId()).get();

        Assertions.assertThat(findMember).isEqualTo(member);

        List<Member> result1 = memberSprinDataJpaRepository.findAll();

        Assertions.assertThat(result1.size()).isEqualTo(1);
        Assertions.assertThat(result1).containsExactly(member);

        List<Member> result2 = memberSprinDataJpaRepository.findByUsername("member1");

        Assertions.assertThat(result2).containsExactly(member);
    }

    /**
     * Spring Data Jpa가 제공하는 QueryDSL 기능 (한계점: 조인이 불가능하다.)
     */
    @Test
    public void querydslPredicateExecutorTest() { // MemberSpringDataJpaRepository에 QuerydslPredicateExecutor<Member> 추가해야함
        QMember member = QMember.member;

        Iterable<Member> result = memberSprinDataJpaRepository.findAll(member.age.between(10, 40).and(member.username.eq("member1")));

        for (Member findedMember : result) {
            System.out.println("member1 = " + findedMember);
        }
    }

}