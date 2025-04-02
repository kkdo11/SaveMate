package kopo.newproject.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
public record UserInfoDTO(

        String userId,

        String email,

        String password,

        String name,

        String created_at,

        String exist_yn


) {
}
