package koo.basicquerydsl.repository;

import com.querydsl.core.QueryResults;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koo.basicquerydsl.dto.MemberSearchCondition;
import koo.basicquerydsl.dto.MemberTeamDto;
import koo.basicquerydsl.dto.QMemberTeamDto;
import koo.basicquerydsl.entity.Member;
import koo.basicquerydsl.entity.QMember;
import koo.basicquerydsl.entity.QTeam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.support.PageableExecutionUtils;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import java.util.List;

public class MemberSpringDataJpaRepositoryImpl implements MemberSpringDataJpaRepositoryCustom { // Spring Data Jpa를 위한 사용자 정의 리포지토리 구현체

    private final JPAQueryFactory queryFactory;
    private final EntityManager em;
    private QMember member = QMember.member;
    private QTeam team = QTeam.team;

    public MemberSpringDataJpaRepositoryImpl(EntityManager em) {
        this.em = em;
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public List<MemberTeamDto> searchByWhereParameter(MemberSearchCondition condition) {
        return queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .fetch();
    }

    private BooleanExpression usernameEq(String username) {
        return StringUtils.hasText(username) ? member.username.eq(username) : null;
    }

    private BooleanExpression teamNameEq(String teamName) {
        return StringUtils.hasText(teamName) ? team.name.eq(teamName) : null;
    }

    private BooleanExpression ageGoe(Integer ageGoe) {
        return ageGoe != null ? member.age.goe(ageGoe) : null;
    }

    private BooleanExpression ageLoe(Integer ageLoe) {
        return ageLoe != null ? member.age.loe(ageLoe) : null;
    }

    @Override
    public Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable) { // 단순 Spring Data Jpa 페이징
        QueryResults<MemberTeamDto> results = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset()) // 어디서 부터 시작할 것인가
                .limit(pageable.getPageSize()) // 몇개 씩 가져올 것인가
                .fetchResults();// fetchResults를 쓰면 content용 쿼리도 날리고 totalCount용 쿼리도 날린다.

        List<MemberTeamDto> content = results.getResults();
        long total = results.getTotal();

        return new PageImpl<>(content, pageable, total); // 테스트도 확인하기
    }

    /**
     * content를 구할 때 조인을 이용하지만 totalCount를 구할 때도 조인을 해서 구하면 성능상 문제 발생 => 분리해야한다.
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable) { // 데이터 내용과 totalCount를 구하는 것을 분리한 Spring Data Jpa 페이징
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset()) // 어디서 부터 시작할 것인가
                .limit(pageable.getPageSize()) // 몇개 씩 가져올 것인가
                .fetch();// 여기서 차이점 발생!, fetch()를 쓰면 content만 가져온다. (fetchResults를 쓰면 content용 쿼리도 날리고 totalCount용 쿼리도 날린다.)

        long total = queryFactory // totalCount 구하는 쿼리 따로 작성하기
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()))
                .fetchCount();

        return new PageImpl<>(content, pageable, total);
    }

    /**
     * totalCount 쿼리가 생략 가능한 경우 생략해서 처리하기 (최적화)
     * - 페이지 시작이면서 전체 컨텐츠 사이즈가 페이지 사이즈보다 작을 때
     * - 마지막 페이지 일때 (offset + 전체 컨텐츠 사이즈를 더해서 totalCount를 구함)
     * - 아래와 같은 경우 위의 경우를 자동으로 처리해줌(위의 경우일 때 totalCount를 구하는 쿼리를 호출하지 않는다.)
     */
    @Override
    public Page<MemberTeamDto> searchPageComplex2(MemberSearchCondition condition, Pageable pageable) {
        List<MemberTeamDto> content = queryFactory
                .select(new QMemberTeamDto(
                        member.id.as("memberId"),
                        member.username,
                        member.age,
                        team.id.as("teamId"),
                        team.name.as("teamName")
                ))
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe())
                )
                .offset(pageable.getOffset()) // 어디서 부터 시작할 것인가
                .limit(pageable.getPageSize()) // 몇개 씩 가져올 것인가
                .fetch();// 여기서 차이점 발생!, fetch()를 쓰면 content만 가져온다. (fetchResults를 쓰면 content용 쿼리도 날리고 totalCount용 쿼리도 날린다.)

        JPAQuery<Member> countQuery = queryFactory // totalCount 구하는 쿼리 따로 작성하기
                .select(member)
                .from(member)
                .leftJoin(member.team, team)
                .where(
                        usernameEq(condition.getUsername()),
                        teamNameEq(condition.getTeamName()),
                        ageGoe(condition.getAgeGoe()),
                        ageLoe(condition.getAgeLoe()));

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchCount());
    }

}
