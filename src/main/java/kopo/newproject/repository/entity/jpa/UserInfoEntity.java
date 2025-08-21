package kopo.newproject.repository.entity.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_INFO")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class UserInfoEntity {

    @Id
    @Column(name = "user_id")
    private String userId;

    @NonNull
    @Column(name = "email")
    private String email;


    @NonNull
    @Column(name = "password", length = 255, nullable = false)
    private String password;

    @NonNull
    @Column(name = "name",length = 500, nullable = false)
    private String name;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "global_alert_enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean globalAlertEnabled; // 전역 알림 활성화 여부

    @Column(name = "auto_budget_adjustment_enabled", columnDefinition = "BOOLEAN DEFAULT FALSE", nullable = false)
    private Boolean autoBudgetAdjustmentEnabled; // 물가 반영 예산 자동 조정 활성화 여부

    @Column(name = "gender", length = 10)
    private String gender; // 성별 (M: 남성, F: 여성)

    @Column(name = "birth_date", length = 10)
    private String birthDate; // 생년월일 (YYYY-MM-DD)

    public String getUserId() {
        return userId;
    }

    public String getGender() {
        return gender;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public Boolean getAutoBudgetAdjustmentEnabled() {
        return autoBudgetAdjustmentEnabled;
    }

    // 비밀번호 변경 메서드 (setter 대체)
    public void changePassword(String newPassword) {
        this.password = newPassword;
    }

    // 저장되기 전에 자동으로 현재 시간 입력
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
