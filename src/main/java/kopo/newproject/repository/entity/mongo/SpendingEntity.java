package kopo.newproject.repository.entity.mongo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import kopo.newproject.dto.SpendingRequest;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;

@Document(collection = "spending")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpendingEntity {

    @Id
    @JsonSerialize(using = ToStringSerializer.class)
    private ObjectId id;

    private String userId;

    @Setter
    private String name;

    @Setter
    private LocalDate date;

    @Setter
    private String category;

    @Setter
    private int amount;

    @Setter
    private String description;

    @Setter
    private YearMonth month;  // 월을 YearMonth로 저장


//    // 생성자 또는 빌더에서 직접 month 세팅
//    public SpendingRequest fromEntity(SpendingEntity entity) {
//        return SpendingRequest.builder()
//                .name(entity.getName())
//                .date(entity.getDate())
//                .category(entity.getCategory())
//                .amount(entity.getAmount())
//                .description(entity.getDescription())
//                .createdAt(entity.getId().getDate()
//                        .toInstant().atZone(ZoneId.of("Asia/Seoul")).toLocalDateTime())
//                .build();
//    }
}
