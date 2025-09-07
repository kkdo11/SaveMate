document.addEventListener('DOMContentLoaded', () => {
    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    document.getElementById('analysisMonth').value = `${yyyy}-${mm}`;

    // 디버깅: isAuthenticated 값 확인
    console.log('isAuthenticated:', isAuthenticated);

    // 비로그인 상태일 경우 버튼 및 입력 필드 비활성화 및 결과 영역 초기화
    if (!isAuthenticated) {
        document.getElementById('analysisMonth').disabled = true;
        document.getElementById('analysisMonth').classList.add('opacity-50', 'cursor-not-allowed');

        const analysisControls = document.getElementById('analysisControls');
        if (analysisControls) {
            analysisControls.querySelectorAll('button').forEach(button => {
                button.disabled = true;
                button.classList.add('opacity-50', 'cursor-not-allowed');
            });
        }

        // emptyState 내의 버튼도 비활성화
        const emptyStateButton = document.querySelector('#emptyState button');
        if (emptyStateButton) {
            emptyStateButton.disabled = true;
            emptyStateButton.classList.add('opacity-50', 'cursor-not-allowed');
        }

        // AI 분석 결과 영역을 즉시 비우고 로그인 필요 메시지 표시
        renderUnauthorized('analysisResult', 'AI 분석 결과를 보려면 로그인이 필요합니다.');

    } else {
        // 로그인 상태일 경우에만 최신 분석 자동 조회
        fetchLatestAnalysis();
    }

    // Add event listener for prediction button
    const predictionButton = document.getElementById('run-prediction-btn');
    if (predictionButton) {
        predictionButton.addEventListener('click', runPrediction);
    }
});

const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
let spendingChart = null;

// 비인증 상태 UI 렌더링 함수 (재사용)
function renderUnauthorized(elementId, message) {
    const container = document.getElementById(elementId);
    if (!container) return;
    container.innerHTML = `
        <div class="text-center text-gray-500 py-10 border rounded-lg bg-gray-50">
            <p class="font-medium">${message}</p>
            <a href='/user/login' class='text-blue-600 hover:underline mt-2 inline-block text-sm'>로그인 페이지로 이동</a>
        </div>
    `;
}

// 알림 표시 함수 (기존 유지, 다른 용도로 사용될 수 있으므로)
function showNotification(type, title, message, action = null) {
    const notificationArea = document.getElementById('notificationArea');
    const notification = document.getElementById('notification');
    const notificationIcon = document.getElementById('notificationIcon');
    const notificationTitle = document.getElementById('notificationTitle');
    const notificationMessage = document.getElementById('notificationMessage');
    const notificationAction = document.getElementById('notificationAction');

    // 타입별 스타일 설정
    let iconHtml = '';
    switch(type) {
        case 'error':
            notification.className = 'rounded-md p-4 bg-red-50';
            notificationTitle.className = 'text-sm font-medium text-red-800';
            notificationMessage.className = 'text-sm text-red-700 mt-1';
            iconHtml = `<svg class="h-5 w-5 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>`;
            break;
        case 'warning':
            notification.className = 'rounded-md p-4 bg-yellow-50';
            notificationTitle.className = 'text-sm font-medium text-yellow-800';
            notificationMessage.className = 'text-sm text-yellow-700 mt-1';
            iconHtml = `<svg class="h-5 w-5 text-yellow-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>`;
            break;
        case 'info':
            notification.className = 'rounded-md p-4 bg-blue-50';
            notificationTitle.className = 'text-sm font-medium text-blue-800';
            notificationMessage.className = 'text-sm text-blue-700 mt-1';
            iconHtml = `<svg class="h-5 w-5 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>`;
            break;
    }

    notificationIcon.innerHTML = iconHtml;
    notificationTitle.textContent = title;
    notificationMessage.textContent = message;

    // 액션 버튼 설정
    if (action) {
        notificationAction.innerHTML = action;
        notificationAction.classList.remove('hidden');
    } else {
        notificationAction.innerHTML = '';
        notificationAction.classList.add('hidden');
    }

    notificationArea.classList.remove('hidden');
}


// 알림 닫기 함수
function closeNotification() {
    document.getElementById('notificationArea').classList.add('hidden');
}

// 빈 상태 표시 함수
function showEmptyState(show) {
    const emptyState = document.getElementById('emptyState');
    const analysisResult = document.getElementById('analysisResult');

    if (show) {
        emptyState.classList.remove('hidden');
        analysisResult.classList.add('hidden');
    } else {
        emptyState.classList.add('hidden');
        analysisResult.classList.remove('hidden');
    }
}

// 토스트 메시지 표시 함수
function showToast(type, title, message, duration = 3000) {
    const toast = document.getElementById('toast');
    const toastIcon = document.getElementById('toastIcon');
    const toastTitle = document.getElementById('toastTitle');
    const toastMessage = document.getElementById('toastMessage');

    // 타입별 스타일 설정
    let iconHtml = '';
    switch(type) {
        case 'success':
            toast.firstElementChild.className = 'bg-white rounded-lg shadow-lg border-l-4 border-green-500 p-4 flex items-start max-w-xs';
            iconHtml = `<svg class="h-5 w-5 text-green-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M5 13l4 4L19 7" />
                </svg>`;
            break;
        case 'error':
            toast.firstElementChild.className = 'bg-white rounded-lg shadow-lg border-l-4 border-red-500 p-4 flex items-start max-w-xs';
            iconHtml = `<svg class="h-5 w-5 text-red-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
                </svg>`;
            break;
        case 'info':
            toast.firstElementChild.className = 'bg-white rounded-lg shadow-lg border-l-4 border-blue-500 p-4 flex items-start max-w-xs';
            iconHtml = `<svg class="h-5 w-5 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>`;
            break;
    }

    toastIcon.innerHTML = iconHtml;
    toastTitle.textContent = title;
    toastMessage.textContent = message;

    // 토스트 표시
    toast.classList.remove('translate-y-full');

    // 지정된 시간 후 자동 숨김
    setTimeout(() => {
        toast.classList.add('translate-y-full');
    }, duration);
}

// 개선된 최신 분석 조회 함수
function fetchLatestAnalysis() {
    toggleLoading(true);
    closeNotification();

    fetch('/api/analysis/latest', {
        headers: { [csrfHeader]: csrfToken }
    })
        .then(res => {
            const contentType = res.headers.get('content-type');
            if (res.status === 404) {
                showEmptyState(true);
                return null;
            } else if (res.status === 401 || res.status === 403) {
                renderUnauthorized('analysisResult', 'AI 분석 결과를 보려면 로그인이 필요합니다.');
                return null;
            } else if (!res.ok) {
                throw new Error("서버 오류가 발생했습니다");
            }
            if (contentType && contentType.includes('application/json')) {
                return res.json();
            } else {
                throw new Error("서버에서 올바른 데이터를 받지 못했습니다. (예상치 못한 응답)");
            }
        })
        .then(json => {
            if (!json) return; // 이미 에러 처리됨

            if (json && Object.keys(json).length > 0) {
                showEmptyState(false);
                renderAnalysisResult(JSON.parse(json.result));
            } else {
                showEmptyState(true);
            }
        })
        .catch(err => {
            // 서버 오류 처리
            console.error("Fetch error in fetchLatestAnalysis:", err);
            // 401/403은 이미 위에서 처리되었으므로, 그 외의 에러만 renderUnauthorized로 표시
            if (err.message !== 'Unauthorized') {
                renderUnauthorized('analysisResult', '데이터를 불러올 수 없습니다. 다시 시도해주세요.');
            }
        })
        .finally(() => toggleLoading(false));
}

function requestAnalysis(event) {
    event.preventDefault();

    if (!confirm("AI 분석은 약 20초 정도 소요될 수 있습니다. 계속하시겠습니까?")) {
        return;
    }

    const month = document.getElementById('analysisMonth').value;
    if (!month) {
        showToast('error', '월을 선택하세요', '분석할 월을 선택해주세요.');
        return;
    }

    toggleLoading(true);
    closeNotification();
    showEmptyState(false);

    fetch(`/api/analysis/${month}`, {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json',
            [csrfHeader]: csrfToken
        }
    })
        .then(res => {
            if (res.status === 401 || res.status === 403) {
                renderUnauthorized('analysisResult', 'AI 분석을 요청하려면 로그인이 필요합니다.');
                return null; // 에러 처리 후 다음 then 블록으로 넘어가지 않도록 null 반환
            }
            if (!res.ok) throw new Error("분석 요청 실패");
            return res.json();
        })
        .then(json => {
            if (!json) return; // 401/403 처리로 null이 넘어온 경우
            renderAnalysisResult(json);
            showToast('success', '분석 완료', '소비 분석이 완료되었습니다.');
        })
        .catch(err => {
            console.error("Fetch error in requestAnalysis:", err);
            if (err.message !== 'Unauthorized') {
                renderUnauthorized('analysisResult', '분석 요청 실패. 다시 시도해주세요.');
            }
        })
        .finally(() => toggleLoading(false));
}

function deleteAnalysis(event) {
    event.preventDefault();
    const month = document.getElementById('analysisMonth').value;
    if (!month) {
        showToast('error', '월을 선택하세요', '삭제할 월을 선택해주세요.');
        return;
    }

    if (!confirm(`❗ 정말 ${month} 월의 분석 내역을 삭제하시겠습니까?`)) return;

    fetch(`/api/analysis/${month}`, {
        method: 'DELETE',
        headers: { [csrfHeader]: csrfToken }
    })
        .then(res => {
            if (res.status === 401 || res.status === 403) {
                renderUnauthorized('analysisResult', 'AI 분석 내역을 삭제하려면 로그인이 필요합니다.');
                return null; // 에러 처리 후 다음 then 블록으로 넘어가지 않도록 null 반환
            }
            if (res.status === 204) {
                clearResult();
                showEmptyState(true);
                showToast('success', '삭제 완료', `${month} 월 분석 내역이 삭제되었습니다.`);
            } else {
                return res.text().then(text => { throw new Error(text || "삭제 실패"); });
            }
        })
        .catch(err => {
            console.error("Fetch error in deleteAnalysis:", err);
            if (err.message !== 'Unauthorized') {
                renderUnauthorized('analysisResult', '삭제 실패. 다시 시도해주세요.');
            }
        });
}

function renderAnalysisResult(result) {
    const container = document.getElementById("analysisResult");

    // 분석 항목 순서 및 제목, 아이콘 정의
    const analysisItems = [
        { key: 'summary', title: '✅ 월간 요약', color: 'green' },
        { key: 'habit', title: '🧾 소비 습관 분석', color: 'yellow' },
        { key: 'tip', title: '💡 절약 팁 제시', color: 'blue' },
        { key: 'anomaly', title: '❗ 이상 지출 탐지', color: 'red' },
        { key: 'guide', title: '📌 다음 달 행동 가이드', color: 'purple' }
    ];

    // 리스트를 담을 div 생성
    let listHtml = '<div class="space-y-4">'

    analysisItems.forEach(item => {
        const content = result[item.key];
        if (content) {
            listHtml += `
                <div class="p-4 border-l-4 border-${item.color}-500 bg-gray-50 rounded-r-lg">
                    <h4 class="font-semibold text-gray-800 flex items-center">
                        ${getCardIcon(item.title)} <!-- 아이콘 재활용 -->
                        <span class="ml-2">${item.title.substring(2)}</span> <!-- 이모지 제외한 제목 -->
                    </h4>
                    <p class="text-gray-600 mt-2 leading-relaxed">${content}</p>
                </div>
            `;
        }
    });

    listHtml += '</div>';

    // 기존 내용 페이드 아웃 후 새 내용으로 교체
    container.classList.add('opacity-0');
    setTimeout(() => {
        container.innerHTML = listHtml;
        container.classList.remove('opacity-0');

        // 차트 렌더링 (필요 시)
        if (result.categorySpending) {
            // 이 부분은 차트를 다른 곳에 그리거나, 이 UI와 통합해야 할 수 있습니다.
            // renderSpendingChart(result.categorySpending);
        }
    }, 300);
}


function getCardIcon(title) {
    // 카드 타입별 아이콘 반환
    switch(title) {
        case "✅ 월간 요약":
            return `<svg class="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2"></path>
                </svg>`;
        case "🧾 소비 습관 분석":
            return `<svg class="w-4 h-4 text-yellow-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z"></path>
                </svg>`;
        case "💡 절약 팁 제시":
            return `<svg class="w-4 h-4 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M9.663 17h4.673M12 3v1m6.364 1.636l-.707.707M21 12h-1M4 12H3m3.343-5.657l-.707-.707m2.828 9.9a5 5 0 117.072 0l-.548.547A3.374 3.374 0 0014 18.469V19a2 2 0 11-4 0v-.531c0-.895-.356-1.754-.988-2.386l-.548-.547z"></path>
                </svg>`;
        case "❗ 이상 지출 탐지":
            return `<svg class="w-4 h-4 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"></path>
                </svg>`;
        case "📌 다음 달 행동 가이드":
            return `<svg class="w-4 h-4 text-purple-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path>
                </svg>`;
        default:
            return '';
    }
}

function renderSpendingChart(data) {
    const ctx = document.getElementById("categoryChart").getContext("2d");
    const labels = Object.keys(data);
    const values = Object.values(data);

    if (spendingChart) spendingChart.destroy();

    spendingChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [{
                label: "소비 금액 (원)",
                data: values,
                backgroundColor: '#6366f1',
                borderRadius: 8
            }]
        },
        options: {
            responsive: true,
            plugins: {
                legend: { display: false }
            },
            scales: {
                y: {
                    beginAtZero: true,
                    ticks: {
                        callback: val => val.toLocaleString() + "원"
                    }
                }
            }
        }
    });
}

function toggleLoading(show) {
    document.getElementById('loading').classList.toggle('hidden', !show);
}

function clearResult() {
    document.getElementById('analysisResult').innerHTML = '';
    if (spendingChart) {
        spendingChart.destroy();
        spendingChart = null;
    }
}

// 히스토리 모달 표시
function showHistory(event) {
    event.preventDefault();
    const month = document.getElementById('analysisMonth').value;
    if (!month) {
        showToast('error', '월을 선택하세요', '조회할 월을 선택해주세요.');
        return;
    }

    toggleLoading(true);

    fetch(`/api/analysis/history?yearMonth=${month}`, {
        headers: { [csrfHeader]: csrfToken }
    })
        .then(res => {
            if (res.status === 401 || res.status === 403) {
                renderUnauthorized('historyList', '분석 히스토리를 보려면 로그인이 필요합니다.');
                document.getElementById('historyModal').classList.remove('hidden'); // 모달은 열되 내용만 변경
                return null;
            }
            if (!res.ok) throw new Error("히스토리 조회 실패");
            return res.json();
        })
        .then(history => {
            if (!history) return; // 401/403 처리로 null이 넘어온 경우
            renderHistoryList(history, month);
            document.getElementById('historyModal').classList.remove('hidden');
        })
        .catch(err => {
            console.error("Fetch error in showHistory:", err);
            if (err.message !== 'Unauthorized') {
                renderUnauthorized('historyList', '히스토리 조회 실패. 다시 시도해주세요.');
            }
        })
        .finally(() => toggleLoading(false));
}

// 히스토리 목록 렌더링
function renderHistoryList(history, month) {
    const container = document.getElementById('historyList');

    if (history.length === 0) {
        container.innerHTML = `<p class="text-gray-500 text-center py-4">
                ${month} 월의 분석 결과가 없습니다.
            </p>`;
        return;
    }

    // 버전 내림차순 정렬 (최신순)
    history.sort((a, b) => b.version - a.version);

    let html = '';
    history.forEach((item, index) => {
        const date = new Date(item.createdAt).toLocaleString();
        const isLatest = item.isLatest ?
            '<span class="bg-green-100 text-green-800 text-xs px-2 py-1 rounded">최신</span>' : '';

        html += `
                <div class="border rounded p-3 bg-gray-50 hover:bg-gray-100 transition">
                    <div class="flex justify-between items-center">
                        <div>
                            <span class="font-medium">버전 ${item.version}</span> ${isLatest}
                            <div class="text-xs text-gray-500">${date}</div>
                        </div>
                        <div class="space-x-2">
                            <button onclick="viewAnalysis('${item.id}')"
                                    class="bg-blue-500 text-white text-sm px-3 py-1 rounded hover:bg-blue-600">
                                보기
                            </button>
                            ${index > 0 ? `
                                <button onclick="compareAnalysis('${history[0].id}', '${item.id}')"
                                        class="bg-purple-500 text-white text-sm px-3 py-1 rounded hover:bg-purple-600">
                                    최신과 비교
                                </button>
                            ` : ''}
                        </div>
                    </div>
                </div>
            `;
    });

    container.innerHTML = html;
}

// 특정 분석 결과 조회
function viewAnalysis(analysisId) {
    toggleLoading(true);
    closeHistoryModal();

    fetch(`/api/analysis/id/${analysisId}`, {
        headers: { [csrfHeader]: csrfToken }
    })
        .then(res => {
            if (res.status === 401 || res.status === 403) {
                renderUnauthorized('analysisResult', '분석 결과를 보려면 로그인이 필요합니다.');
                return null;
            }
            if (!res.ok) throw new Error("분석 결과 조회 실패");
            return res.json();
        })
        .then(analysis => {
            if (!analysis) return; // 401/403 처리로 null이 넘어온 경우
            const result = JSON.parse(analysis.result);
            showEmptyState(false);
            renderAnalysisResult(result);
        })
        .catch(err => {
            console.error("Fetch error in viewAnalysis:", err);
            if (err.message !== 'Unauthorized') {
                renderUnauthorized('analysisResult', '분석 결과 조회 실패. 다시 시도해주세요.');
            }
        })
        .finally(() => toggleLoading(false));
}

// 분석 결과 비교
function compareAnalysis(analysisId1, analysisId2) {
    toggleLoading(true);
    closeHistoryModal();

    fetch(`/api/analysis/compare?analysisId1=${analysisId1}&analysisId2=${analysisId2}`, {
        headers: { [csrfHeader]: csrfToken }
    })
        .then(res => {
            if (res.status === 401 || res.status === 403) {
                renderUnauthorized('compareResult', '분석 결과를 비교하려면 로그인이 필요합니다.');
                document.getElementById('compareModal').classList.remove('hidden'); // 모달은 열되 내용만 변경
                return null;
            }
            if (!res.ok) throw new Error("분석 결과 비교 실패");
            return res.json();
        })
        .then(comparison => {
            if (!comparison) return; // 401/403 처리로 null이 넘어온 경우
            renderComparisonResult(comparison);
            document.getElementById('compareModal').classList.remove('hidden');
        })
        .catch(err => {
            console.error("Fetch error in compareAnalysis:", err);
            if (err.message !== 'Unauthorized') {
                renderUnauthorized('compareResult', '분석 결과 비교 실패. 다시 시도해주세요.');
            }
        })
        .finally(() => toggleLoading(false));
}

// 비교 결과 렌더링
function renderComparisonResult(comparison) {
    const container = document.getElementById('compareResult');
    const date1 = new Date(comparison.createdAt1).toLocaleString();
    const date2 = new Date(comparison.createdAt2).toLocaleString();

    let html = `
            <div class="bg-gray-100 p-4 rounded">
                <div class="flex justify-between mb-2">
                    <div>
                        <span class="font-semibold">이전 버전:</span> 버전 ${comparison.version2}
                        <div class="text-xs text-gray-500">${date2}</div>
                    </div>
                    <div>
                        <span class="font-semibold">최신 버전:</span> 버전 ${comparison.version1}
                        <div class="text-xs text-gray-500">${date1}</div>
                    </div>
                </div>
            </div>
        `;

    const differences = comparison.differences;
    if (Object.keys(differences).length === 0) {
        html += `
                <div class="text-center py-6 text-gray-500">
                    두 분석 결과 사이에 차이점이 없습니다.
                </div>
            `;
    } else {
        html += `<h4 class="font-semibold text-lg mt-4">변경된 항목</h4>`;

        for (const [key, diff] of Object.entries(differences)) {
            let title;
            switch (key) {
                case 'summary': title = '✅ 월간 요약'; break;
                case 'habit': title = '🧾 소비 습관 분석'; break;
                case 'tip': title = '💡 절약 팁 제시'; break;
                case 'anomaly': title = '❗ 이상 지출 탐지'; break;
                case 'guide': title = '📌 다음 달 행동 가이드'; break;
                default: title = key;
            }

            html += `
                    <div class="border rounded p-4 mt-2">
                        <h5 class="font-medium mb-2">${title}</h5>
                        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                            <div class="bg-red-50 p-3 rounded">
                                <div class="text-xs text-red-600 mb-1">이전</div>
                                <p class="text-gray-800">${diff.before}</p>
                            </div>
                            <div class="bg-green-50 p-3 rounded">
                                <div class="text-xs text-green-600 mb-1">최신</div>
                                <p class="text-gray-800">${diff.after}</p>
                            </div>
                        </div>
                    </div>
                `;
        }
    }

    container.innerHTML = html;
}

// 모달 닫기 함수들
function closeHistoryModal() {
    document.getElementById('historyModal').classList.add('hidden');
}

function closeCompareModal() {
    document.getElementById('compareModal').classList.add('hidden');
}

// 모달 외부 클릭 시 닫기
window.addEventListener('click', function(event) {
    const historyModal = document.getElementById('historyModal');
    const compareModal = document.getElementById('compareModal');

    if (event.target === historyModal) {
        closeHistoryModal();
    }

    if (event.target === compareModal) {
        closeCompareModal();
    }
});

// --- 공공데이터 비교 기능 추가 ---

let comparisonChart = null;

/**
 * "공공데이터와 비교" 버튼 클릭 시 실행되는 메인 함수
 */
async function requestPublicDataComparison(event) {
    event.preventDefault();
    if (!isAuthenticated) {
        showToast('error', '로그인 필요', '또래와 소비를 비교하려면 로그인이 필요합니다.');
        return;
    }

    const month = document.getElementById('analysisMonth').value;
    if (!month) {
        showToast('error', '월을 선택하세요', '비교할 월을 선택해주세요.');
        return;
    }

    toggleLoading(true);
    document.getElementById('comparisonResult').classList.add('hidden');

    try {
        // 1. 사용자 정보 가져오기 (성별, 생년월일)
        const userInfo = await getUserInfo();
        console.log("User Info:", userInfo); // 사용자 정보 로그

        if (!userInfo || !userInfo.gender || !userInfo.birthDate) {
            showToast('error', '사용자 정보 부족', '프로필에 성별과 생년월일 정보가 필요합니다.');
            return;
        }
        const ageGroup = getAgeGroup(userInfo.birthDate);
        const gender = userInfo.gender;

        // 2. 최신 원본 소비 데이터와 집계된 평균 데이터를 동시에 요청
        const results = await Promise.allSettled([
            fetch(`/api/analysis/${month}/raw-spending`, { headers: { [csrfHeader]: csrfToken } }), // 항상 최신 원본 데이터 조회
            fetch(`/api/aggregated-spending/compare?gender=${gender}&ageGroup=${ageGroup}`, { headers: { [csrfHeader]: csrfToken } })
        ]);

        const userSpendingResponse = results[0];
        const aggregatedDataResponse = results[1];

        // 3. 사용자 소비 데이터 처리
        let userSpendingData = null;
        if (userSpendingResponse.status === 'fulfilled' && userSpendingResponse.value.ok) {
            userSpendingData = await userSpendingResponse.value.json();
            console.log("User Spending Data:", userSpendingData); // 사용자 소비 데이터 로그
        } else {
            showToast('info', '소비 데이터 부족', `해당 월의 소비 데이터가 없어 비교할 수 없습니다.`);
            return;
        }

        if (!userSpendingData || Object.keys(userSpendingData).length === 0) {
            renderNoComparisonData(); // 새로운 함수 호출
            return;
        }

        // 4. 집계 데이터 처리
        let aggregatedData = null;
        if (aggregatedDataResponse.status === 'fulfilled' && aggregatedDataResponse.value.ok) {
            aggregatedData = await aggregatedDataResponse.value.json();
            console.log("Aggregated Peer Data:", aggregatedData); // 또래 집계 데이터 로그
        } else {
            showToast('info', '비교 데이터 없음', '아직 또래 평균 소비 데이터가 없어요. 내 소비 내역만 표시됩니다.');
        }

        // 5. 데이터 매핑 및 결합
        const combinedData = mapAndCombineData(userSpendingData, aggregatedData);

        // 6. 차트 렌더링
        renderComparisonChart(combinedData);
        document.getElementById('comparisonResult').classList.remove('hidden');
        showToast('success', '비교 완료', '또래 평균 소비 내역 비교가 완료되었습니다.');

    } catch (error) {
        console.error("Error during aggregated data comparison:", error);
        showToast('error', '오류 발생', '데이터를 비교하는 중 오류가 발생했습니다.');
    } finally {
        toggleLoading(false);
    }
}

/**
 * (임시) 사용자 정보를 가져오는 함수.
 * TODO: 실제로는 서버 API를 호출하여 사용자 정보를 가져와야 합니다.
 */
async function getUserInfo() {
    try {
        const response = await fetch('/user/info', {
            headers: {
                [csrfHeader]: csrfToken,
                'Accept': 'application/json'
            }
        });
        if (!response.ok) {
            if (response.status === 401) {
                // 세션이 만료되었거나 로그인하지 않은 경우
                console.warn('User not authenticated, redirecting to login.');
                // 로그인 페이지로 리디렉션하거나 로그인 모달을 표시할 수 있습니다.
                // window.location.href = '/user/login';
                return null;
            }
            throw new Error('Failed to fetch user info');
        }
        return await response.json();
    } catch (error) {
        console.error('Error in getUserInfo:', error);
        showToast('error', '사용자 정보 조회 실패', '사용자 정보를 가져오는 데 실패했습니다.');
        return null; // 오류 발생 시 null 반환
    }
}

/**
 * 생년월일을 KOSIS 연령대 코드로 변환하는 함수
 */
function getAgeGroup(birthDate) {
    const today = new Date();
    const birth = new Date(birthDate);
    let age = today.getFullYear() - birth.getFullYear();
    const m = today.getMonth() - birth.getMonth();
    if (m < 0 || (m === 0 && today.getDate() < birth.getDate())) {
        age--;
    }

    if (age < 20) return '10s';
    if (age < 30) return '20s';
    if (age < 40) return '30s';
    if (age < 50) return '40s';
    if (age < 60) return '50s';
    if (age < 70) return '60s';
    return '70s_and_up';
}

/**
 * 사용자 소비 데이터와 집계된 평균 데이터를 매핑하고 결합하는 함수
 */
function mapAndCombineData(userSpending, aggregatedData) {
    // 사용자 소비 데이터가 없으면 빈 객체로 초기화
    const currentUserSpending = userSpending || {};

    // 모든 카테고리 목록을 미리 추출 (사용자 + 또래 평균)
    const allCategories = new Set([
        ...Object.keys(currentUserSpending),
        ...(aggregatedData ? Object.keys(aggregatedData.categoryAverageSpending) : [])
    ]);

    const combined = {};

    allCategories.forEach(category => {
        const userAmount = currentUserSpending[category] || 0;
        const publicAmount = (aggregatedData && aggregatedData.categoryAverageSpending && aggregatedData.categoryAverageSpending[category])
            ? parseFloat(aggregatedData.categoryAverageSpending[category])
            : 0;

        // 둘 중 하나라도 데이터가 있는 경우에만 결과에 포함
        if (userAmount > 0 || publicAmount > 0) {
            combined[category] = {
                user: userAmount,
                public: publicAmount
            };
        }
    });

    console.log("Combined Chart Data:", combined);
    return combined;
}

/**
 * 비교 차트를 렌더링하는 함수
 */
function renderComparisonChart(data) {
    const ctx = document.getElementById('comparisonChart').getContext('2d');
    const labels = Object.keys(data);
    const userData = labels.map(label => data[label].user);
    const publicData = labels.map(label => data[label].public);

    if (comparisonChart) {
        comparisonChart.destroy();
    }

    comparisonChart = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: labels,
            datasets: [
                {
                    label: '내 소비 (원)',
                    data: userData,
                    backgroundColor: 'rgba(79, 70, 229, 0.8)', // Indigo
                    borderColor: 'rgba(79, 70, 229, 1)',
                    borderWidth: 1
                },
                {
                    label: '또래 평균 소비 (원)',
                    data: publicData,
                    backgroundColor: 'rgba(13, 148, 136, 0.8)', // Teal
                    borderColor: 'rgba(13, 148, 136, 1)',
                    borderWidth: 1
                }
            ]
        },
        options: {
            responsive: true,
            plugins: {
                legend: {
                    position: 'top',
                },
                tooltip: {
                    callbacks: {
                        label: function(context) {
                            let label = context.dataset.label || '';
                            if (label) {
                                label += ': ';
                            }
                            // 로그 스케일에서는 context.raw를 사용해야 원래 값을 표시할 수 있음
                            if (context.raw !== null) {
                                label += new Intl.NumberFormat('ko-KR').format(context.raw) + '원';
                            }
                            return label;
                        }
                    }
                }
            },
            scales: {
                y: {
                    type: 'logarithmic', // 값의 차이가 클 때 효과적인 로그 스케일 사용
                    min: 1, // 로그 스케일은 0이 될 수 없으므로 최소값을 1로 설정
                    ticks: {
                        callback: function(value, index, ticks) {
                            // 1, 10, 100, 1000 등 10의 거듭제곱 값만 표시하여 가독성 향상
                            const log10 = Math.log10(value);
                            if (log10 === Math.floor(log10)) {
                                return new Intl.NumberFormat('ko-KR', { notation: 'compact' }).format(value);
                            }
                        }
                    }
                }
            }
        }
    });
}

/**
 * 비교할 소비 데이터가 없을 때 표시하는 UI
 */
function renderNoComparisonData() {
    const container = document.getElementById('comparisonResult');
    if (!container) return;

    if (comparisonChart) {
        comparisonChart.destroy();
        comparisonChart = null;
    }

    container.innerHTML = `
        <div class="text-center text-gray-500 py-10 border rounded-lg bg-gray-50">
            <p class="font-medium mb-4">해당 월의 소비 데이터가 없어 또래와 비교할 수 없습니다.</p>
            <a href='/spending/page' class='bg-blue-500 hover:bg-blue-600 text-white font-bold py-2 px-4 rounded inline-flex items-center'>
                <svg class="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24" xmlns="http://www.w3.org/2000/svg"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 6v6m0 0v6m0-6h6m-6 0H6"></path></svg>
                <span>소비 내역 추가하기</span>
            </a>
        </div>
    `;
    container.classList.remove('hidden');
}

async function runPrediction(event) {
    event.preventDefault();
    const resultDiv = document.getElementById('prediction-result');
    const loadingDiv = document.getElementById('prediction-loading');
    const button = document.getElementById('run-prediction-btn');

    button.style.display = 'none';
    loadingDiv.style.display = 'flex';

    try {
        const response = await fetch('/api/analysis/prediction', {
            method: 'GET',
            headers: { [csrfHeader]: csrfToken }
        });

        if (!response.ok) {
            const errorData = await response.json().catch(() => ({ message: `HTTP error! status: ${response.status}` }));
            throw new Error(errorData.message || `HTTP error! status: ${response.status}`);
        }

        const prediction = await response.json();

        if (prediction.message && !prediction.totalPredictedAmount) {
            resultDiv.innerHTML = `<p class="text-gray-500">${prediction.message}</p>`;
            return;
        }

        let categoryHtml = '<ul class="list-disc list-inside text-left max-w-md mx-auto mt-4 space-y-1">';
        for (const [category, amount] of Object.entries(prediction.categoryPredictedAmounts)) {
            categoryHtml += `<li><span class="font-semibold">${category}:</span> ${amount.toLocaleString('ko-KR')}원</li>`;
        }
        categoryHtml += '</ul>';

        resultDiv.innerHTML = `
            <div class="p-4 rounded-lg bg-indigo-50">
                <p class="text-gray-600">다음 달 예상 소비액</p>
                <p class="text-3xl font-bold text-indigo-600 my-2">${prediction.totalPredictedAmount.toLocaleString('ko-KR')}원</p>
                <hr class="my-3">
                <p class="text-sm text-gray-500 mb-2">카테고리별 예상 지출</p>
                ${categoryHtml}
            </div>
        `;

    } catch (error) {
        resultDiv.innerHTML = `<p class="text-red-500 font-semibold p-4 bg-red-50 rounded-lg">⚠️ 예측 실패: ${error.message}</p>`;
    } finally {
        loadingDiv.style.display = 'none';
    }
}