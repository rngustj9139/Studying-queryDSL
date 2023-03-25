package koo.basicquerydsl.repository;

import koo.basicquerydsl.dto.MemberSearchCondition;
import koo.basicquerydsl.dto.MemberTeamDto;

import java.util.List;

public interface MemberSpringDataJpaRepositoryCustom { // Spring Data Jpa를 위한 사용자 정의 리포지토리 인터페이스

    List<MemberTeamDto> searchByWhereParameter(MemberSearchCondition condition);

}
