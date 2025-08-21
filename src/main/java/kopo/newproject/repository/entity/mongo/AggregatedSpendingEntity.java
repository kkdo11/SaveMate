package kopo.newproject.repository.entity.mongo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Map;

@Getter
@Setter
@Document(collection = "aggregated_spending")
public class AggregatedSpendingEntity {

    @Id
    private String id; // 예: "2023-01_M_20대"

    private YearMonth month;
    private String gender; // "M" 또는 "F"
    private String ageGroup; // "10대", "20대", "30대" 등
    private Map<String, BigDecimal> categoryAverageSpending; // 카테고리별 평균 지출
    private long userCount; // 해당 그룹에 속한 사용자 수

    // Manually added builder method to resolve compilation issues if Lombok fails
    public static AggregatedSpendingEntityBuilder builder() {
        return new AggregatedSpendingEntityBuilder();
    }

    public static class AggregatedSpendingEntityBuilder {
        private String id;
        private YearMonth month;
        private String gender;
        private String ageGroup;
        private Map<String, BigDecimal> categoryAverageSpending;
        private long userCount;

        AggregatedSpendingEntityBuilder() {}

        public AggregatedSpendingEntityBuilder id(String id) {
            this.id = id;
            return this;
        }

        public AggregatedSpendingEntityBuilder month(YearMonth month) {
            this.month = month;
            return this;
        }

        public AggregatedSpendingEntityBuilder gender(String gender) {
            this.gender = gender;
            return this;
        }

        public AggregatedSpendingEntityBuilder ageGroup(String ageGroup) {
            this.ageGroup = ageGroup;
            return this;
        }

        public AggregatedSpendingEntityBuilder categoryAverageSpending(Map<String, BigDecimal> categoryAverageSpending) {
            this.categoryAverageSpending = categoryAverageSpending;
            return this;
        }

        public AggregatedSpendingEntityBuilder userCount(long userCount) {
            this.userCount = userCount;
            return this;
        }

        public AggregatedSpendingEntity build() {
            return new AggregatedSpendingEntity(id, month, gender, ageGroup, categoryAverageSpending, userCount);
        }

        public String toString() {
            return "AggregatedSpendingEntity.AggregatedSpendingEntityBuilder(id=" + this.id + ", month=" + this.month + ", gender=" + this.gender + ", ageGroup=" + this.ageGroup + ", categoryAverageSpending=" + this.categoryAverageSpending + ", userCount=" + this.userCount + ")";
        }
    }

    // Manually added constructor to resolve compilation issues if Lombok fails
    public AggregatedSpendingEntity(String id, YearMonth month, String gender, String ageGroup, Map<String, BigDecimal> categoryAverageSpending, long userCount) {
        this.id = id;
        this.month = month;
        this.gender = gender;
        this.ageGroup = ageGroup;
        this.categoryAverageSpending = categoryAverageSpending;
        this.userCount = userCount;
    }

    // Manually added getter for id to resolve compilation issues if Lombok fails
    public String getId() {
        return id;
    }
}
