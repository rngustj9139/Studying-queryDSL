package koo.basicquerydsl;

import com.querydsl.core.Tuple;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koo.basicquerydsl.dto.MemberDto;
import koo.basicquerydsl.dto.QMemberDto;
import koo.basicquerydsl.entity.Member;
import koo.basicquerydsl.entity.QMember;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslIntermediateLevelTest { // queryDSL 중급 문법

    @PersistenceContext
    EntityManager em;

    JPAQueryFactory queryFactory = new JPAQueryFactory(em);
    QMember member = QMember.member;

    /**
     * 프로젝션: select 대상을 지정하는 것
     * 프로젝션 대상이 하나이면 타입을 명확하게 조회, 다수이면 Tuple이나 Dto로 조회
     */
    @Test
    public void simpleProjection() { // 프로젝션 대상이 한개인 경우
        List<String> result = queryFactory
                .select(member.username)
                .from(member)
                .fetch();

        for (String s : result) {
            System.out.println("s = " + s);
        }
    }

    @Test
    public void tupleProjection() { // 프로젝션 대상이 여래개인 경우(tuple로 조회)
        List<Tuple> result = queryFactory
                .select(member.username, member.age)
                .from(member)
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple.get(member.username) = " + tuple.get(member.username));
            System.out.println("tuple.get(member.age) = " + tuple.get(member.age));
        }
    }

    @Test
    public void findDtoByJPQL() { // 프로젝션 대상이 여러개인 경우(jpa와 jpql을 이용하여 dto로 조회)
//      em.createQuery("select m from Member m", Member.class);
        List<MemberDto> result = em.createQuery("select new koo.basicquerydsl.dto.MemberDto(m.username, m.age) from Member m", MemberDto.class)
                .getResultList(); // @AllArgsConstructor가 있어야함

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 가져오려는 MemberDto의 프로퍼티 명이 Member 엔티티에 들어 있어야한다.
     * member.username.as("name") 방식으로 바꾸기 가능
     */
    @Test
    public void dtoProjection() { // 프로젝션 대상이 여러개인 경우(dto로 조회)
        List<MemberDto> result = queryFactory
                .select(Projections.bean(MemberDto.class, // @NoArgsConstructor가 있어야함 (setter로 프로퍼티 매핑)
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 가져오려는 MemberDto의 프로퍼티 명이 Member 엔티티에 들어 있어야한다.
     * member.username.as("name") 방식으로 바꾸기 가능
     */
    @Test
    public void dtoProjection2() { // 프로젝션 대상이 여러개인 경우(dto로 조회)
        List<MemberDto> result = queryFactory
                .select(Projections.fields(MemberDto.class, // @NoArgsConstructor나 @AllArgsConstructor가 없어도 된다(필드에 바로 데이터가 들어감)
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    /**
     * 가져오려는 MemberDto의 프로퍼티 명이 Member 엔티티에 들어 있어야한다.
     * member.username.as("name") 방식으로 바꾸기 가능
     */
    @Test
    public void dtoProjection3() { // 프로젝션 대상이 여러개인 경우(dto로 조회)
        List<MemberDto> result = queryFactory
                .select(Projections.constructor(MemberDto.class, // @AllArgsConstructor가 있어야 한다.
                        member.username,
                        member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

    @Test
    public void dtoProjection4() { // @QueryProjection을 통한 프로젝션(dto로 조회)
        List<MemberDto> result = queryFactory
                .select(new QMemberDto(member.username, member.age))
                .from(member)
                .fetch();

        for (MemberDto memberDto : result) {
            System.out.println("memberDto = " + memberDto);
        }
    }

}

