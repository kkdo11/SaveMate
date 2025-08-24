package kopo.newproject.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MailDTO {

    String toMail;  // 받는사람
    String title; // 보내는 메일 제목
    String contents; // 보내는 메일 내용
}