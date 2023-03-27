package koo.basicquerydsl.repository;

import koo.basicquerydsl.dto.MemberSearchCondition;
import koo.basicquerydsl.dto.MemberTeamDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface MemberSpringDataJpaRepositoryCustom { // Spring Data Jpa를 위한 사용자 정의 리포지토리 인터페이스

    List<MemberTeamDto> searchByWhereParameter(MemberSearchCondition condition);
    Page<MemberTeamDto> searchPageSimple(MemberSearchCondition condition, Pageable pageable); // 단순 Spring Data Jpa 페이징
    Page<MemberTeamDto> searchPageComplex(MemberSearchCondition condition, Pageable pageable); // 데이터 내용과 totalCount를 구하는 것을 분리한 Spring Data Jpa 페이징

}
