package kopo.newproject.repository.entity.mongo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import kopo.newproject.dto.SpendingRequest;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.math.BigDecimal;
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
    private BigDecimal amount;

    @Setter
    private String description;

    @Setter
    private YearMonth month;  // 월을 YearMonth로 저장

    // Manually added getters to resolve compilation issues if Lombok fails
    public String getCategory() {
        return category;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getDescription() {
        return description;
    }
}
