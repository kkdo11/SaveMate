// 자주 사용하는 DOM 요소를 미리 변수에 할당 (DOM 조회 최적화)
const budgetListEl = document.getElementById('budgetList');
const budgetFormEl = document.getElementById('budgetForm');
const budgetIdInput = document.getElementById('budgetId');
const budgetMonthInput = document.getElementById('budgetMonth');
const budgetCategorySelect = document.getElementById('budgetCategory');
const totalBudgetInput = document.getElementById('totalBudget');
const searchMonthInput = document.getElementById('searchMonth');
const authNoticeEl = document.getElementById('authNotice');
const adjustCpiBtn = document.getElementById('adjustCpiBudgetBtn');

const apiUrl = '/budgetAPI';

// ===================================================================
//  1. 핵심 기능 함수 (Core Functions)
// ===================================================================

/**
 * CSRF 토큰을 포함한 fetch 래퍼 함수
 * @param {string} url - 요청 URL
 * @param {object} options - fetch 옵션
 * @returns {Promise<Response>} - fetch 응답 Promise
 */
async function csrfFetch(url, options = {}) {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    options.headers = {
        ...options.headers,
        [csrfHeader]: csrfToken,
        'Content-Type': 'application/json'
    };

    const response = await fetch(url, options);

    if (!response.ok) {
        const error = new Error(await response.text() || `HTTP error! status: ${response.status}`);
        error.status = response.status;
        throw error;
    }

    return response;
}

/**
 * API 호출 오류를 일관되게 처리하는 함수
 * @param {Error} error - 발생한 오류 객체
 * @param {string} context - 오류가 발생한 상황 (e.g., '조회', '저장')
 */
function handleApiError(error, context) {
    console.error(`[${context} 실패]`, error);
    if (error.status === 401) {
        renderUnauthorized('budgetList', `예산 ${context}을(를) 위해 로그인이 필요합니다.`);
    } else {
        alert(`❌ 예산 ${context} 중 오류가 발생했습니다: ${error.message}`);
    }
    adjustCpiBtn.disabled = true;
}

/**
 * 현재 선택된 월의 예산 목록을 다시 불러오고 화면을 갱신하는 함수 (코드 중복 제거)
 */
async function refreshBudgetList() {
    const selectedMonth = searchMonthInput.value;
    if (!selectedMonth) return;

    adjustCpiBtn.disabled = true;

    try {
        const response = await csrfFetch(`/budgetAPI/monthly?month=${selectedMonth}`);
        const data = await response.json();
        renderBudgetCards(data);

        if (data && data.length > 0) {
            adjustCpiBtn.disabled = false;
        } else {
            adjustCpiBtn.disabled = true;
        }
    } catch (error) {
        handleApiError(error, '조회');
    }
}


// ===================================================================
//  2. 렌더링 관련 함수 (Rendering Functions)
// ===================================================================

/**
 * 예산 카드 목록을 화면에 렌더링하는 함수
 * @param {Array} data - 예산 데이터 배열
 */
function renderBudgetCards(data) {
    budgetListEl.innerHTML = '';

    if (!data || data.length === 0) {
        budgetListEl.innerHTML = `
            <div class="text-center text-gray-500 py-10 col-span-1 sm:col-span-2 lg:col-span-3">
                <p>해당 월에 등록된 예산이 없습니다.</p>
            </div>
        `;
        return;
    }

    data.forEach(budget => {
        const card = document.createElement('div');
        card.className = 'border rounded shadow p-4 bg-gray-50 flex flex-col';
        card.innerHTML = `
            <div class="flex-grow">
                <h3 class="text-lg font-semibold mb-2 text-purple-600">${budget.category}</h3>
                <p><strong>총 예산:</strong> ${Number(budget.totalBudget).toLocaleString()}원</p>
                <p><strong>사용액:</strong> ${Number(budget.usedBudget).toLocaleString()}원</p>
                <p><strong>남은 금액:</strong> ${Number(budget.remainingBudget).toLocaleString()}원</p>
            </div>
            <div class="flex justify-end space-x-2 mt-4">
                <button onclick="fillFormForEdit(${budget.budgetId})" class="bg-yellow-400 text-white px-3 py-1 rounded text-sm hover:bg-yellow-500">수정</button>
                <button onclick="deleteBudget(${budget.budgetId})" class="bg-red-500 text-white px-3 py-1 rounded text-sm hover:bg-red-600">삭제</button>
            </div>
        `;
        budgetListEl.appendChild(card);
    });
}

/**
 * 비인증 상태일 때 UI를 렌더링하는 함수
 * @param {string} elementId - 렌더링할 컨테이너 ID
 * @param {string} message - 표시할 메시지
 */
function renderUnauthorized(elementId, message) {
    const container = document.getElementById(elementId);
    if (!container) return;
    container.innerHTML = `
        <div class="text-center text-gray-500 py-10 border rounded-lg bg-gray-50 col-span-1 sm:col-span-2 lg:col-span-3">
            <p class="font-medium">${message}</p>
            <a href='/user/login' class='text-blue-600 hover:underline mt-2 inline-block text-sm'>로그인 페이지로 이동</a>
        </div>
    `;
    adjustCpiBtn.disabled = true;
}


// ===================================================================
//  3. 이벤트 핸들러 및 초기화 (Event Handlers & Initialization)
// ===================================================================

/**
 * 수정 버튼 클릭 시, 해당 예산 정보로 폼을 채우는 함수
 * @param {number} id - 수정할 예산 ID
 */
async function fillFormForEdit(id) {
    try {
        const response = await csrfFetch(`${apiUrl}/${id}`);
        const data = await response.json();

        budgetIdInput.value = data.budget_id ?? data.budgetId;
        budgetMonthInput.value = `${data.year}-${String(data.month).padStart(2, '0')}`;
        budgetCategorySelect.value = data.category;
        totalBudgetInput.value = data.total_budget ?? data.totalBudget;

        window.scrollTo(0, 0);
    } catch (error) {
        handleApiError(error, '데이터 불러오기');
    }
}

/**
 * 삭제 버튼 클릭 시, 예산을 삭제하는 함수
 * @param {number} id - 삭제할 예산 ID
 */
async function deleteBudget(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        await csrfFetch(`${apiUrl}/${id}`, { method: 'DELETE' });
        alert('🗑️ 삭제 완료');
        await refreshBudgetList();
    } catch (error) {
        handleApiError(error, '삭제');
    }
}

// 폼 제출 이벤트 핸들러 (생성/수정)
budgetFormEl.addEventListener('submit', async (e) => {
    e.preventDefault();

    const id = budgetIdInput.value ? parseInt(budgetIdInput.value) : null;
    const [year, month] = budgetMonthInput.value.split('-');

    if (!year || !month || !budgetCategorySelect.value || !totalBudgetInput.value) {
        alert('❗ 모든 항목을 입력해주세요.');
        return;
    }

    const payload = {
        year: parseInt(year),
        month: parseInt(month),
        category: budgetCategorySelect.value,
        total_budget: parseInt(totalBudgetInput.value),
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `${apiUrl}/${id}` : apiUrl;

    try {
        await csrfFetch(url, { method, body: JSON.stringify(payload) });
        alert(`✅ 예산 ${id ? '수정' : '저장'} 완료`);
        budgetFormEl.reset();
        budgetIdInput.value = '';
        await refreshBudgetList();
    } catch (error) {
        handleApiError(error, id ? '수정' : '저장');
    }
});

// 월별 예산 조회 버튼 이벤트 핸들러
document.getElementById('searchBudgetBtn').addEventListener('click', refreshBudgetList);

// 물가 반영 예산 조정 버튼 이벤트 핸들러
adjustCpiBtn.addEventListener('click', async () => {
    if (!confirm('현재 조회된 월의 모든 예산을 물가 상승률에 따라 조정하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        return;
    }

    try {
        await csrfFetch('/budgetAPI/actions/adjust-cpi', { method: 'PUT' });
        alert('✅ 예산이 성공적으로 조정되었습니다.');
        await refreshBudgetList();
    } catch (error) {
        handleApiError(error, '물가 반영 조정');
    }
});


// 페이지 로드 시 실행될 초기화 함수
function initialize() {
    if (!isAuthenticated) {
        authNoticeEl.classList.remove('hidden');
        document.querySelectorAll('#budgetForm input, #budgetForm select, #budgetForm button')
            .forEach(el => {
                el.disabled = true;
                el.classList.add('opacity-50', 'cursor-not-allowed');
            });
    }

    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    searchMonthInput.value = `${yyyy}-${mm}`;

    refreshBudgetList();
}

// 전역 스코프에 노출해야 하는 함수들 (HTML onclick에서 호출하기 위함)
window.fillFormForEdit = fillFormForEdit;
window.deleteBudget = deleteBudget;

// 초기화 함수 실행
initialize();