package kopo.newproject.repository.entity.jpa;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "NOTIFICATION")
@DynamicInsert
@DynamicUpdate
@Builder
@Entity
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @NonNull
    @Column(name = "user_id")
    private String userId;

    @NonNull
    @Column(name = "type")
    private String type; // 예: BUDGET_ALERT, SYSTEM_MESSAGE 등

    @NonNull
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_read", nullable = false)
    private boolean isRead; // 읽음 여부

    @NonNull
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt; // 읽은 시간

    // 알림을 읽음으로 표시하는 메서드
    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }
}