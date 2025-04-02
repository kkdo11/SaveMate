package kopo.newproject.repository.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;
import java.util.Date;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "USER_INFO")
@DynamicInsert
@DynamicUpdate
@Builder
@Cacheable
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

    // 저장되기 전에 자동으로 현재 시간 입력
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
