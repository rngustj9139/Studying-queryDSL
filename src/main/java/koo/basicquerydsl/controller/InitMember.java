package koo.basicquerydsl.controller;

import koo.basicquerydsl.entity.Member;
import koo.basicquerydsl.entity.Team;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Profile("local") // 샘플 데이터 추가가 테스트 케이스에 영향을 미치지 않게 하기 위해 추가 (application.yml도 추가)
@Component
@RequiredArgsConstructor
public class InitMember {

    private final InitMemberService initMemberService;

    @PostConstruct
    public void init() {
        initMemberService.init();
    }

    @Component
    static class InitMemberService {

        @PersistenceContext
        private EntityManager em;

        @Transactional
        public void init() {
            Team teamA = new Team("teamA");
            Team teamB = new Team("teamB");

            em.persist(teamA);
            em.persist(teamB);

            for (int i = 0; i < 100; i++) { // 100명의 회원중 절반은 teamA, 나머지는 teamB에 소속되게 하기
                Team selectedTeam = (i % 2 == 0) ? teamA : teamB;
                em.persist(new Member("member" + i, i, selectedTeam));
            }
        }

    }

}
