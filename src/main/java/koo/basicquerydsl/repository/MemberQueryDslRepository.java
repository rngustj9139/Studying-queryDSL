package koo.basicquerydsl.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import koo.basicquerydsl.entity.Member;
import koo.basicquerydsl.entity.QMember;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import java.util.List;

@Repository
public class MemberQueryDslRepository {

    private final EntityManager em;
    private final JPAQueryFactory queryFactory; // 이걸 그냥 스프링 빈으로 등록해도 된다.
    private QMember member = QMember.member;

    public MemberQueryDslRepository(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    public List<Member> findAll() {
        return queryFactory
                .selectFrom(member)
                .fetch();
    }

    public List<Member> findByUsername(String username) {
        return queryFactory
                .selectFrom(member)
                .where(member.username.eq(username))
                .fetch();
    }

}
