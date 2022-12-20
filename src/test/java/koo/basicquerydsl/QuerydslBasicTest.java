package koo.basicquerydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koo.basicquerydsl.entity.Member;
import koo.basicquerydsl.entity.QMember;
import koo.basicquerydsl.entity.QTeam;
import koo.basicquerydsl.entity.Team;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

    @PersistenceContext
    EntityManager em;

    @BeforeEach
    public void before() {
        Team teamA = new Team("teamA");
        Team teamB = new Team("teamB");
        em.persist(teamA);
        em.persist(teamB);

        Member member1 = new Member("member1", 10, teamA);
        Member member2 = new Member("member2", 20, teamA);

        Member member3 = new Member("member3", 30, teamB);
        Member member4 = new Member("member4", 40, teamB);
        em.persist(member1);
        em.persist(member2);
        em.persist(member3);
        em.persist(member4);
    }

    @Test
    public void startJPQL() {
        // member1을 찾아라
        Member findByMember = em.createQuery("select m from Member m where m.username = :username", Member.class)
                .setParameter("username", "member1")
                .getSingleResult();

        Assertions.assertThat(findByMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void startQuerydsl() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        // Q 타입을 통해 컴파일 시점에 에러를 잡을 수 있게 한다.
        QMember m = new QMember("m");// QMember가 없을 경우 Tasks/Other/compileQuerydsl을 더블 클릭해야한다. (m은 어떤 QMember인지 이름을 부여한 것이다.)
//      QMember m2 = QMember.member; // 이방법도 사용가능 혹은 static import 하기

        Member findMember = queryFactory
                .select(m)
                .from(m)
                .where(m.username.eq("member1")) // 파라미터 바인딩 처리를 통해 sql 인젝션을 예방 (테스트 데이터 이미 들어가 있다.)
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void search() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;

        Member findMember = queryFactory
                .selectFrom(member) // select랑 from을 합침
                .where(member.username.eq("member1").and(member.age.eq(10))) // not equal은 nq
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void searchAndParam() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;

        Member findMember = queryFactory
                .selectFrom(member) // select랑 from을 합침
                .where(member.username.eq("member1"),
                       member.age.eq(10)) // and() 말고 쉼표 사용 가능
                .fetchOne();

        Assertions.assertThat(findMember.getUsername()).isEqualTo("member1");
    }

    @Test
    public void resultFetch() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;

        List<Member> fetch = queryFactory
                .selectFrom(member)
                .fetch();// fetch() => 결과를 리스트로 조회 (결과가 없으면 빈 리스트 반환)

        Member fetchOne = queryFactory
                .selectFrom(member)
                .fetchOne();// fetchOne() => 결과 단건 조회 (결과가 없으면 에러 발생)

        Member fetchFirst = queryFactory
                .selectFrom(member)
                .fetchFirst();// fetchFirst() => 처음 한개만 조회

        QueryResults<Member> fetchResults = queryFactory
                .selectFrom(member)
                .fetchResults(); //  fetchResults() => 페이징 기능 추가(totalCount 사용 가능)

        long total = fetchResults.getTotal(); // totalCount 구하기
        long limit = fetchResults.getLimit();
        List<Member> results = fetchResults.getResults();

        long fetchCount = queryFactory
                .selectFrom(member)
                .fetchCount(); // select count(m) from Member m 수행
    }

    /**
     * 회원 정렬 순서
     * 1. 회원 나이 내림차순(desc)
     * 2. 회원 이름 오름차순(asc)
     * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
     **/
    @Test
    public void sort() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;

        // 테스트 데이터 더 추가하기
        em.persist(new Member(null, 100));
        em.persist(new Member("member5", 100));
        em.persist(new Member("member6", 100));

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(100))
                .orderBy(member.age.desc(),
                         member.username.asc().nullsLast()) // username이 null값이면 맨 마지막으로 오게 한다.
                .fetch();

        Member member5 = result.get(0);
        Member member6 = result.get(1);
        Member memberNull = result.get(2);

        Assertions.assertThat(member5.getUsername()).isEqualTo("member5");
        Assertions.assertThat(member6.getUsername()).isEqualTo("member6");
        Assertions.assertThat(memberNull.getUsername()).isNull();
    }

    @Test
    public void paging1() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;

        List<Member> result = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 1부터 시작
                .limit(2)
                .fetch();

        Assertions.assertThat(result.size()).isEqualTo(2);
    }

    @Test
    public void paging2() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;

        QueryResults<Member> fetchResults = queryFactory
                .selectFrom(member)
                .orderBy(member.username.desc())
                .offset(1) // 1부터 시작
                .limit(2)
                .fetchResults();

        Assertions.assertThat(fetchResults.getTotal()).isEqualTo(4);
        Assertions.assertThat(fetchResults.getLimit()).isEqualTo(2);
        Assertions.assertThat(fetchResults.getOffset()).isEqualTo(1);
        Assertions.assertThat(fetchResults.getResults().size()).isEqualTo(2);
    }

    @Test
    public void aggregation() { // 집합
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;

        List<Tuple> result = queryFactory // 튜플은 queryDSL이 제공하는 튜플이다.
                .select(member.count(), member.age.sum(), member.age.avg(), member.age.max(), member.age.min())
                .from(member)
                .fetch();

        Tuple tuple = result.get(0);
        Assertions.assertThat(tuple.get(member.count())).isEqualTo(4);
        Assertions.assertThat(tuple.get(member.age.sum())).isEqualTo(100);
        Assertions.assertThat(tuple.get(member.age.avg())).isEqualTo(25);
        Assertions.assertThat(tuple.get(member.age.max())).isEqualTo(40);
        Assertions.assertThat(tuple.get(member.age.min())).isEqualTo(10);
    }

    /**
     * 팀의 이름과 각 팀의 평균 연령을 구해라.
     **/
    @Test
    public void group() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QTeam team = QTeam.team;
        QMember member = QMember.member;

        List<Tuple> result = queryFactory
                .select(team.name, member.age.avg())
                .from(member)
                .join(member.team, team)
                .groupBy(team.name) // 팀의 이름으로 그룹핑
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        Assertions.assertThat(teamA.get(team.name)).isEqualTo("teamA");
        Assertions.assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        Assertions.assertThat(teamB.get(team.name)).isEqualTo("teamB");
        Assertions.assertThat(teamB.get(member.age.avg())).isEqualTo(15);
    }

}
