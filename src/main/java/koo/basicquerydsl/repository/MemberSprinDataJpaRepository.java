package koo.basicquerydsl.repository;

import koo.basicquerydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MemberSprinDataJpaRepository extends JpaRepository<Member, Long> {

    List<Member> findByUsername(String username);

}
