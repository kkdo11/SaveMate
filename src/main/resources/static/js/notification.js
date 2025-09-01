document.addEventListener('DOMContentLoaded', function() {
    const notificationBell = document.getElementById('notificationBell');
    const notificationDropdown = document.getElementById('notificationDropdown');
    const notificationCountSpan = document.getElementById('notificationCount');
    const notificationListDiv = document.getElementById('notificationList');
    const markAllAsReadBtn = document.getElementById('markAllAsReadBtn');

    // CSRF 토큰 가져오기
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    // CSRF 포함 fetch 함수
    async function csrfFetch(url, options = {}) {
        options.headers = {
            ...options.headers,
            [csrfHeader]: csrfToken,
            'Content-Type': 'application/json'
        };
        const response = await fetch(url, options);
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response;
    }

    // 읽지 않은 알림 개수 업데이트
    async function updateUnreadCount() {
        try {
            const response = await csrfFetch('/api/notifications/unread/count');
            const count = await response.json();
            notificationCountSpan.textContent = count > 99 ? '99+' : count.toString();
            if (count > 0) {
                notificationCountSpan.classList.remove('hidden');
            } else {
                notificationCountSpan.classList.add('hidden');
            }
        } catch (error) {
            console.error('Failed to fetch unread notification count:', error);
            notificationCountSpan.classList.add('hidden');
        }
    }

    // 알림 목록 로드 및 표시
    async function loadNotifications() {
        try {
            const response = await csrfFetch('/api/notifications');
            const notifications = await response.json();

            notificationListDiv.innerHTML = ''; // 기존 목록 초기화

            if (notifications.length === 0) {
                notificationListDiv.innerHTML = '<p class="text-center text-gray-500 py-4">알림이 없습니다.</p>';
                markAllAsReadBtn.disabled = true;
                markAllAsReadBtn.classList.add('opacity-50', 'cursor-not-allowed');
                return;
            }

            markAllAsReadBtn.disabled = false;
            markAllAsReadBtn.classList.remove('opacity-50', 'cursor-not-allowed');

            notifications.forEach(notif => {
                const notificationItem = document.createElement('div');
                notificationItem.className = `p-3 border-b border-gray-100 hover:bg-gray-50 ${notif.read ? 'text-gray-500' : 'font-medium text-gray-800'}`;
                notificationItem.innerHTML = `
                    <p class="text-sm">${notif.message}</p>
                    <p class="text-xs text-gray-400 mt-1">${new Date(notif.createdAt).toLocaleString()}</p>
                `;
                if (!notif.read) {
                    notificationItem.classList.add('cursor-pointer');
                    notificationItem.addEventListener('click', async () => {
                        await markNotificationAsRead(notif.notificationId);
                        loadNotifications(); // 목록 새로고침
                        updateUnreadCount(); // 개수 새로고침
                    });
                }
                notificationListDiv.appendChild(notificationItem);
            });
        } catch (error) {
            console.error('Failed to load notifications:', error);
            notificationListDiv.innerHTML = '<p class="text-center text-red-500 py-4">알림 로드 실패.</p>';
        }
    }

    // 특정 알림 읽음 처리
    async function markNotificationAsRead(notificationId) {
        try {
            await csrfFetch(`/api/notifications/${notificationId}/read`, { method: 'PUT' });
        } catch (error) {
            console.error(`Failed to mark notification ${notificationId} as read:`, error);
        }
    }

    // 모든 알림 읽음 처리
    markAllAsReadBtn.addEventListener('click', async () => {
        if (confirm('모든 알림을 읽음으로 표시하시겠습니까?')) {
            try {
                await csrfFetch('/api/notifications/read-all', { method: 'PUT' });
                loadNotifications();
                updateUnreadCount();
            } catch (error) {
                console.error('Failed to mark all notifications as read:', error);
            }
        }
    });

    // 벨 아이콘 클릭 시 드롭다운 토글
    notificationBell.addEventListener('click', function() {
        const isHidden = notificationDropdown.classList.contains('hidden');
        if (isHidden) {
            loadNotifications(); // 알림 로드
            notificationDropdown.classList.remove('hidden');
        } else {
            notificationDropdown.classList.add('hidden');
        }
    });

    // 드롭다운 외부 클릭 시 닫기
    document.addEventListener('click', function(event) {
        if (!notificationBell.contains(event.target) && !notificationDropdown.contains(event.target)) {
            notificationDropdown.classList.add('hidden');
        }
    });

    // 페이지 로드 시 읽지 않은 알림 개수 업데이트
    if (isAuthenticated) { // isAuthenticated는 Thymeleaf에서 주입된 전역 변수
        updateUnreadCount();
    }
});