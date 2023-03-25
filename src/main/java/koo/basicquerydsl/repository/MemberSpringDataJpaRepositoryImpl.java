package koo.basicquerydsl.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import koo.basicquerydsl.dto.MemberSearchCondition;
import koo.basicquerydsl.dto.MemberTeamDto;
import koo.basicquerydsl.dto.QMemberTeamDto;
import koo.basicquerydsl.entity.QMember;
import koo.basicquerydsl.entity.QTeam;
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

}