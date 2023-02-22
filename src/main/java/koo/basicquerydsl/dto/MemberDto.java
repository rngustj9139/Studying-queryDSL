package koo.basicquerydsl.dto;

import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class MemberDto {

    private String username;
    private int age;

    @QueryProjection // @QueryProjection을 통한 프로젝션(dto로 조회) (tasks -> other -> complieQuerydsl 클릭하면 dto도 Q파일이 생성됨)
    public MemberDto(String username, int age) {
        this.username = username;
        this.age = age;
    }

    public MemberDto() {
    }

}
