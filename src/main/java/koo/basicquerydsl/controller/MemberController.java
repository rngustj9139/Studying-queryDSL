package koo.basicquerydsl.controller;

import koo.basicquerydsl.dto.MemberSearchCondition;
import koo.basicquerydsl.dto.MemberTeamDto;
import koo.basicquerydsl.repository.MemberQueryDslRepository;
import koo.basicquerydsl.repository.MemberSprinDataJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberQueryDslRepository memberQueryDslRepository;
    private final MemberSprinDataJpaRepository memberSprinDataJpaRepository;

    @GetMapping("/v1/members") // 동적 쿼리 검색 (queryDSL)
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) { // /v1/members 요청시 모든 회원이 나오고 /v1/members?teamName=teamB 로 요청시 teamB의 회원만 나온다.
        return memberQueryDslRepository.searchByWhereParameter(condition);
    }

    // /v2/members?page=0&size=5 형태로 요청하기
    @GetMapping("/v2/members") // Spring Data Jpa의 페이징을 활용하면서(페이징 구현) 동적 쿼리 검색 (queryDSL)
    public Page<MemberTeamDto> searchMemberV2(MemberSearchCondition condition, Pageable pageable) {
        return memberSprinDataJpaRepository.searchPageSimple(condition, pageable);
    }

    // /v3/members?page=0&size=5 형태로 요청하기
    @GetMapping("/v3/members") // Spring Data Jpa의 페이징을 활용하면서(페이징 구현) 동적 쿼리 검색 (queryDSL)
    public Page<MemberTeamDto> searchMemberV3(MemberSearchCondition condition, Pageable pageable) {
        return memberSprinDataJpaRepository.searchPageComplex(condition, pageable);
    }


}
