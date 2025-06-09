document.addEventListener('DOMContentLoaded', () => {
    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    document.getElementById('analysisMonth').value = `${yyyy}-${mm}`;

    // 최신 분석 자동 조회
    fetchLatestAnalysis();
});

const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;
let spendingChart = null;

// 알림 표시 함수
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
                showNotification(
                    'warning',
                    '로그인이 필요합니다',
                    '로그인 후 이용해 주세요.',
                    `<a href="/user/login" class="bg-yellow-500 hover:bg-yellow-600 text-white px-3 py-1 rounded text-sm">로그인하기</a>`
                );
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
            showNotification(
                'error',
                '데이터를 불러올 수 없습니다',
                err.message,
                `<button onclick="fetchLatestAnalysis()" class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-sm">다시 시도</button>`
            );
        })
        .finally(() => toggleLoading(false));
}

function requestAnalysis(event) {
    event.preventDefault();
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
            if (!res.ok) throw new Error("분석 요청 실패");
            return res.json();
        })
        .then(json => {
            renderAnalysisResult(json);
            showToast('success', '분석 완료', '소비 분석이 완료되었습니다.');
        })
        .catch(err => {
            showNotification(
                'error',
                '분석 요청 실패',
                err.message,
                `<button onclick="requestAnalysis(event)" class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded text-sm">다시 시도</button>`
            );
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
            if (res.status === 204) {
                clearResult();
                showEmptyState(true);
                showToast('success', '삭제 완료', `${month} 월 분석 내역이 삭제되었습니다.`);
            } else {
                return res.text().then(text => { throw new Error(text || "삭제 실패"); });
            }
        })
        .catch(err => {
            showNotification('error', '삭제 실패', err.message);
        });
}

function renderAnalysisResult(result) {
    const container = document.getElementById("analysisResult");

    // 기존 내용 페이드 아웃
    container.classList.add('opacity-0');

    setTimeout(() => {
        container.innerHTML = `
            <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                ${renderCard("✅ 월간 요약", result.summary, "green")}
                ${renderCard("🧾 소비 습관 분석", result.habit, "yellow")}
                ${renderCard("💡 절약 팁 제시", result.tip, "blue")}
                ${renderCard("❗ 이상 지출 탐지", result.anomaly, "red")}
                ${renderCard("📌 다음 달 행동 가이드", result.guide, "purple")}
            </div>
        `;

        container.classList.remove('opacity-0');

        if (result.categorySpending) {
            renderSpendingChart(result.categorySpending);
        }
    }, 300);
}


function renderCard(title, content, color) {
    return `
            <div class="border rounded-lg shadow-sm overflow-hidden transition-all hover:shadow-md">
                <div class="bg-${color}-50 px-4 py-3 border-b border-${color}-100">
                    <h4 class="text-${color}-700 font-medium flex items-center">
                        ${getCardIcon(title)}
                        <span class="ml-2">${title}</span>
                    </h4>
                </div>
                <div class="p-4 bg-white">
                    <p class="text-gray-700 leading-relaxed">${content || "내용 없음"}</p>
                </div>
            </div>
        `;
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
            if (!res.ok) throw new Error("히스토리 조회 실패");
            return res.json();
        })
        .then(history => {
            renderHistoryList(history, month);
            document.getElementById('historyModal').classList.remove('hidden');
        })
        .catch(err => {
            showNotification('error', '히스토리 조회 실패', err.message);
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
            if (!res.ok) throw new Error("분석 결과 조회 실패");
            return res.json();
        })
        .then(analysis => {
            const result = JSON.parse(analysis.result);
            showEmptyState(false);
            renderAnalysisResult(result);
        })
        .catch(err => {
            showNotification('error', '분석 결과 조회 실패', err.message);
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
            if (!res.ok) throw new Error("분석 결과 비교 실패");
            return res.json();
        })
        .then(comparison => {
            renderComparisonResult(comparison);
            document.getElementById('compareModal').classList.remove('hidden');
        })
        .catch(err => {
            showNotification('error', '분석 결과 비교 실패', err.message);
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