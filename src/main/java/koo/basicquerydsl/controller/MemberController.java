package koo.basicquerydsl.controller;

import koo.basicquerydsl.dto.MemberSearchCondition;
import koo.basicquerydsl.dto.MemberTeamDto;
import koo.basicquerydsl.repository.MemberQueryDslRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
public class MemberController {

    private final MemberQueryDslRepository memberQueryDslRepository;

    @GetMapping("/v1/members")
    public List<MemberTeamDto> searchMemberV1(MemberSearchCondition condition) { // /v1/members 요청시 모든 회원이 나오고 /v1/members?teamName=teamB 로 요청시 teamB의 회원만 나온다.
        return memberQueryDslRepository.search(condition);
    }

}
