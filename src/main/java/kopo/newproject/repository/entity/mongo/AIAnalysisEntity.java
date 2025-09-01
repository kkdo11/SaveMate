package kopo.newproject.repository.entity.mongo;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.Instant;
import java.time.LocalDateTime;

@Document(collection = "AI_analysis")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AIAnalysisEntity {

    @Id
    private String id;

    private String userId;
    private String month;
    private String requestData; // 사용자 id
    private String result; // GPT 응답

     private int version;

    @CreatedDate
    private LocalDateTime createdAt;
}
