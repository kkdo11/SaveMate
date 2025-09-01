package kopo.newproject.controller;

import kopo.newproject.repository.entity.jpa.NotificationEntity;
import kopo.newproject.repository.jpa.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationRepository notificationRepository;

    private String getCurrentUserId() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        if ("anonymousUser".equals(userId)) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        return userId;
    }

    // 모든 알림 조회 (최신순)
    @GetMapping
    public ResponseEntity<List<NotificationEntity>> getAllNotifications() {
        try {
            String userId = getCurrentUserId();
            List<NotificationEntity> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
            return ResponseEntity.ok(notifications);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 읽지 않은 알림 조회 (최신순)
    @GetMapping("/unread")
    public ResponseEntity<List<NotificationEntity>> getUnreadNotifications() {
        try {
            String userId = getCurrentUserId();
            List<NotificationEntity> notifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
            return ResponseEntity.ok(notifications);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 읽지 않은 알림 개수 조회
    @GetMapping("/unread/count")
    public ResponseEntity<Long> getUnreadNotificationsCount() {
        try {
            String userId = getCurrentUserId();
            long count = notificationRepository.countByUserIdAndIsReadFalse(userId);
            return ResponseEntity.ok(count);
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 알림 읽음 처리
    @PutMapping("/{notificationId}/read")
    public ResponseEntity<Void> markNotificationAsRead(@PathVariable Long notificationId) {
        try {
            String userId = getCurrentUserId();
            Optional<NotificationEntity> optionalNotification = notificationRepository.findById(notificationId);

            if (optionalNotification.isPresent()) {
                NotificationEntity notification = optionalNotification.get();
                // 알림의 소유자가 현재 사용자인지 확인
                if (!notification.getUserId().equals(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 권한 없음
                }
                notification.markAsRead();
                notificationRepository.save(notification);
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // 모든 알림 읽음 처리
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllNotificationsAsRead() {
        try {
            String userId = getCurrentUserId();
            List<NotificationEntity> unreadNotifications = notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId);
            unreadNotifications.forEach(NotificationEntity::markAsRead);
            notificationRepository.saveAll(unreadNotifications);
            return ResponseEntity.ok().build();
        } catch (AccessDeniedException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}