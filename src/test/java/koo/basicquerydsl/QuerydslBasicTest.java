package koo.basicquerydsl;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.JPAExpressions;
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
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceUnit;
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
                .join(member.team, team) // 첫번째 파라미터에 조인 대상을 지정하고, 두번째 파라미터에 별칭으로 사용할 Q타입을 지정하면 된다.
                .groupBy(team.name) // 팀의 이름으로 그룹핑
                .fetch();

        Tuple teamA = result.get(0);
        Tuple teamB = result.get(1);

        Assertions.assertThat(teamA.get(team.name)).isEqualTo("teamA");
        Assertions.assertThat(teamA.get(member.age.avg())).isEqualTo(15);

        Assertions.assertThat(teamB.get(team.name)).isEqualTo("teamB");
        Assertions.assertThat(teamB.get(member.age.avg())).isEqualTo(15);
    }

    @Test
    public void join() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        List<Member> result = queryFactory
                .selectFrom(member)
                .join(member.team, team) // 첫번째 파라미터에 조인 대상을 지정하고, 두번째 파라미터에 별칭으로 사용할 Q타입을 지정하면 된다.
                .where(team.name.eq("teamA"))
                .fetch();

        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("member1", "member2");
    }

    /**
     * 세타 조인(연관 관계가 없어도 조인 가능)
     * 회원의 이름이 팀 이름과 같은 회원을 조회
     **/
    @Test
    public void theta_join() {
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));

        List<Member> result = queryFactory
                .select(member)
                .from(member, team) // 세타 조인 수행(막 조인 - 카테시안 곱) (세타 조인에서는 left outer 조인이나 right outer 조인이 안된다.)
                .where(member.username.eq(team.name))
                .fetch();

        Assertions.assertThat(result)
                .extracting("username")
                .containsExactly("teamA", "teamB");
    }

    /**
     * 조인 - on 절
     * on절을 통해 조인 대상 필터링과 연관관계가 없는 엔티티를 외부조인하기 위해 사용한다.
    **/
    @Test
    public void join_on_filtering() { // 조인 대상 필터링, 예시) 회원과 팀을 조인하면서 팀 이름이 teamA인 팀만 조인하기, 회원은 모두 조회 (jpql: select m from Member m left join m.team t on t.name = 'teamA)
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(member.team, team).on(team.name.eq("teamA")) // 그냥 내부 조인(inner join)을 쓸 경우 where절로 대체 가능하다.
                .fetch();

        for (Tuple tuple : result) {
            System.out.println("tuple = " + tuple);
        }
    }

    @Test
    public void join_on_no_relation() { // 연관관게 없는 엔티티 외부 조인(join - on) (ex: 회원의 이름이 팀 이름과 같은 대상 left outer 조인)
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        em.persist(new Member("teamA"));
        em.persist(new Member("teamB"));
        em.persist(new Member("teamC"));

        List<Tuple> result = queryFactory
                .select(member, team)
                .from(member)
                .leftJoin(team).on(member.username.eq(team.name))
                .where(member.username.eq(team.name))
                .fetch();
    }

    @PersistenceUnit
    EntityManagerFactory emf;

    @Test
    public void fetchJoinNo() { // 페치 조인이 없을 때
        em.flush();
        em.clear();

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;

        Member findMember = queryFactory
                .selectFrom(member)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam()); // Team 객체가 초기화 되었는지 확인(지연로딩이므로 false이다.)

        Assertions.assertThat(isLoaded).isFalse(); // 페치 조인 미적용이므로 false
    }

    @Test
    public void fetchJoinUse() { // 페치 조인 적용
        em.flush();
        em.clear();

        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;
        QTeam team = QTeam.team;

        Member findMember = queryFactory
                .selectFrom(member)
                .join(member.team, team).fetchJoin() // 페치 조인을 적용했으므로 team의 프록시가 실제 객체로 초기화됨(member, team 둘다 한번에 select로 가져온다.)
                .where(member.username.eq("member1"))
                .fetchOne();

        boolean isLoaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());

        Assertions.assertThat(isLoaded).isTrue();
    }

    /**
     * 나이가 가장 많은 회원 조회회
    **/
    @Test
    public void subQuery() { // 서브 쿼리(쿼리 안에 쿼리 넣음) => JPAExpressions 사용해야함, where 절 안에 새로운 쿼리를 넣음(alias가 겹치지 않게 해야함)
        JPAQueryFactory queryFactory = new JPAQueryFactory(em);
        QMember member = QMember.member;
        QMember memberSub = new QMember("memberSub");

        List<Member> result = queryFactory
                .selectFrom(member)
                .where(member.age.eq(
                        JPAExpressions
                                .select(memberSub.age.max())
                                .from(memberSub)
                ))
                .fetch();

        Assertions.assertThat(result)
                .extracting("age")
                .containsExactly(40);
    }

}
