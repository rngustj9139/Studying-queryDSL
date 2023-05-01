package koo.basicquerydsl.repository;

import koo.basicquerydsl.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.querydsl.QuerydslPredicateExecutor;

import java.util.List;

public interface MemberSprinDataJpaRepository extends JpaRepository<Member, Long>, MemberSpringDataJpaRepositoryCustom, QuerydslPredicateExecutor<Member> { // QuerydslPredicateExecutor은 스프링 데이터 jpa에서 제공하는 queryDSL 기능이다.

    List<Member> findByUsername(String username);

}
