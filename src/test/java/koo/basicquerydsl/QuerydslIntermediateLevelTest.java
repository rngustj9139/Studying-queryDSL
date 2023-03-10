package koo.basicquerydsl;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koo.basicquerydsl.dto.MemberDto;
import koo.basicquerydsl.dto.QMemberDto;
import koo.basicquerydsl.entity.Member;
import koo.basicquerydsl.entity.QMember;
import org.assertj.core.api.Assertions;
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
                .select(member.username) // .select().distinct()
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

    @Test
    public void dynamicQuery_BooleanBuilder() { // 동적 쿼리 - BooleanBuilder 이용
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember1(usernameParam, ageParam);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    private List<Member> searchMember1(String usernameParam, Integer ageParam) {
        BooleanBuilder builder = new BooleanBuilder();
//      BooleanBuilder builder = new BooleanBuilder(member.username.eq(usernameParam)); // 필수 값 지정

        if (usernameParam != null) {
            builder.and(member.username.eq(usernameParam));
        }

        if (ageParam != null) {
            builder.and(member.age.eq(ageParam));
        }

        return queryFactory
                    .selectFrom(member)
                    .where()
                    .fetch();
    }

    @Test
    public void dynamicQuery_WhereParam() { // 동적 쿼리 - where 다중 파라미터 사용
        String usernameParam = "member1";
        Integer ageParam = 10;

        List<Member> result = searchMember2(usernameParam, ageParam);

        Assertions.assertThat(result.size()).isEqualTo(1);
    }

    @Test
    public List<Member> searchMember2(String usernameParam, Integer ageParam) {
        return queryFactory
                .selectFrom(member)
                .where(usernameEq(usernameParam), ageEq(ageParam))
                .fetch();
    }

    @Test
    public BooleanExpression usernameEq(String usernameParam) {
        if (usernameParam != null) {
            return member.username.eq(usernameParam);
        } else {
            return null;
        }

//      return usernameParam != null ? member.username.eq(usernameParam) : null; // 삼항 연산자 이용
    }

    @Test
    public BooleanExpression ageEq(Integer ageParam) {
        return ageParam != null ? member.age.eq(ageParam) : null;
    }

    @Test
    public void bulkUpdate() { // 수정, 삭제 벌크 연산 (영속성 컨텍스트를 무시하고 바로 DB에 쿼리를 날림, DB 속 데이터와 영속성 컨텍스트 내의 엔티티의 상태가 달라짐)
        long cnt = queryFactory
                .update(member)
                .set(member.username, "비회원")
                .where(member.age.lt(28))
                .execute();

        // DB 속 데이터와 영속성 컨텍스트 내의 엔티티와의 상태 맞추기
        em.flush();
        em.clear();

        Assertions.assertThat(cnt).isEqualTo(2);
    }

    @Test
    public void bulkAdd() {
        long cnt = queryFactory
                .update(member)
                .set(member.age, member.age.add(1)) // 곱하기는 multiply
                .execute();

        Assertions.assertThat(cnt).isEqualTo(4);
    }

    @Test
    public void bulkDelete() {
        long cnt = queryFactory
                .delete(member)
                .where(member.age.gt(18))
                .execute();

        Assertions.assertThat(cnt).isEqualTo(3);
    }

    @Test
    public void sqlFunction() { // queryDSL에서 SQL function 호출
        List<String> results = queryFactory // member.username에서 member라는 단어를 M으로 교체
                .select(Expressions.stringTemplate("function('replace', {0}, {1}, {2})", member.username, "member", "M"))
                .from(member)
                .fetch();

        for (String result : results) {
            System.out.println("result = " + result);
        }
    }

    @Test
    public void sqlFunction2() {
        List<String> results = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(Expressions.stringTemplate("function('lower', {0})", member.username)))
                .fetch();

        for (String result : results) {
            System.out.println("result = " + result);
        }
    }
    
    @Test
    public void sqlFunction3() { // sqlFunction2를 아래와 같이 바꿀 수 있음(queryDSL 내장 기능)
        List<String> results = queryFactory
                .select(member.username)
                .from(member)
                .where(member.username.eq(member.username.lower()))
                .fetch();

        for (String result : results) {
            System.out.println("result = " + result);
        }
    }

}

