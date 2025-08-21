const goalApiUrl = '/goalAPI';

// CSRF 토큰을 포함한 fetch 래퍼 함수
async function csrfFetch(url, options = {}) {
    const csrfToken = document.querySelector('meta[name="_csrf"]').content;
    const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

    options.headers = {
        ...options.headers,
        [csrfHeader]: csrfToken,
        'Content-Type': 'application/json'
    };

    const response = await fetch(url, options);

    if (response.status === 401) {
        const error = new Error('Unauthorized');
        error.status = 401;
        throw error;
    }

    if (!response.ok) {
        throw new Error(`Network response was not ok: ${response.statusText}`);
    }

    return response;
}

// 비인증 상태 UI 렌더링 함수 (재사용)
function renderUnauthorized(elementId, message) {
    const container = document.getElementById(elementId);
    if (!container) return;
    container.innerHTML = `
        <div class="text-center text-gray-500 py-10 border rounded-lg bg-gray-50 col-span-1 md:col-span-2 lg:col-span-3">
            <p class="font-medium">${message}</p>
            <a href='/user/login' class='text-blue-600 hover:underline mt-2 inline-block text-sm'>로그인 페이지로 이동</a>
        </div>
    `;
}

window.onload = () => {
    if (!isAuthenticated) {
        document.getElementById('authNotice').classList.remove('hidden');
        document.querySelectorAll('#goal-form input, #goal-form button').forEach(el => {
            el.disabled = true;
            el.classList.add('opacity-50', 'cursor-not-allowed');
        });
    }

    loadGoals();
};

function loadGoals() {
    csrfFetch(goalApiUrl)
        .then(res => res.json())
        .then(data => {
            if (data) renderGoals(data);
        })
        .catch(err => {
            if (err.status === 401) {
                renderUnauthorized('goal-list', '저축 목표를 보려면 로그인이 필요합니다.');
            } else {
                console.error("Failed to fetch goals:", err);
                document.getElementById('goal-list').innerHTML = '<p class="text-center text-red-500">데이터를 불러오는 중 오류가 발생했습니다.</p>';
            }
        });
}

function renderGoals(goals) {
    const list = document.getElementById('goal-list');
    list.innerHTML = '';

    goals.forEach(goal => {
        const progress = goal.targetAmount > 0 ? Math.min((goal.savedAmount / goal.targetAmount) * 100, 100) : 0;

        const card = document.createElement('div');
        card.className = "bg-white p-4 rounded-lg shadow-md border flex flex-col justify-between";

        card.innerHTML = `
        <div>
          <h3 class="text-lg font-semibold text-purple-700 mb-2">${goal.goalName}</h3>
          <p class="text-sm text-gray-600 mb-1">
            💰 목표: ${goal.targetAmount?.toLocaleString()}원<br>
            💵 현재: ${goal.savedAmount?.toLocaleString()}원
          </p>
          <p class="text-sm text-gray-600 mb-3">📅 D-Day: ${goal.deadline}</p>
          <div class="w-full bg-gray-200 rounded-full h-4 overflow-hidden mb-3">
            <div class="h-full bg-indigo-500 text-right pr-2 text-white text-xs leading-4" style="width: ${progress.toFixed(2)}%;">
              ${progress.toFixed(2)}%
            </div>
          </div>
        </div>
        <div class="flex justify-end space-x-2 mt-2">
          <button onclick="editGoal('${goal.goalId}')" class="bg-yellow-400 hover:bg-yellow-500 text-white px-3 py-1 rounded">✏️ 수정</button>
          <button onclick="deleteGoal('${goal.goalId}')" class="bg-red-500 hover:bg-red-600 text-white px-3 py-1 rounded">🗑️ 삭제</button>
        </div>
      `;
        list.appendChild(card);
    });
}

document.getElementById('goal-form').addEventListener('submit', function(e) {
    e.preventDefault();
    if (!isAuthenticated) return;

    // 입력값 가져오기
    const goalName = document.getElementById('goal-name').value.trim();
    const targetAmount = document.getElementById('goal-target').value.trim();
    const savedAmount = document.getElementById('goal-saved').value.trim();
    const deadline = document.getElementById('goal-deadline').value.trim();

    // 빈칸 또는 잘못된 값 검사
    if (!goalName || !targetAmount || !savedAmount || !deadline) {
        alert("⚠️ 모든 필드를 정확히 입력해주세요.");
        return;
    }
    if (targetAmount <= 0 || savedAmount <= 0){
        alert("⚠️ 목표 금액과 현재 금액은 0보다 커야 합니다.");
    }

    if (isNaN(targetAmount) || isNaN(savedAmount)) {
        alert("⚠️ 금액은 숫자로 입력해주세요.");
        return;
    }

    // 저장할 데이터 구성
    const goalData = {
        goalName,
        targetAmount: parseInt(targetAmount),
        savedAmount: parseInt(savedAmount),
        deadline
    };

    const id = document.getElementById('goal-id').value;
    const method = id ? 'PUT' : 'POST';
    const url = id ? `${goalApiUrl}/${id}` : goalApiUrl;

    csrfFetch(url, {
        method,
        body: JSON.stringify(goalData)
    })
        .then(res => res.json())
        .then(() => {
            alert("✅ 저장 완료");
            resetGoalForm();
            loadGoals();
        })
        .catch(() => alert("❌ 저장 실패"));
});


function editGoal(id) {
    if (!isAuthenticated) return;

    csrfFetch(`${goalApiUrl}/${id}`)
        .then(res => res.json())
        .then(goal => {
            if (!goal) return;
            document.getElementById('form-title').innerText = '목표 수정';
            document.getElementById('goal-id').value = goal.goalId;
            document.getElementById('goal-name').value = goal.goalName;
            document.getElementById('goal-target').value = goal.targetAmount;
            document.getElementById('goal-saved').value = goal.savedAmount;
            document.getElementById('goal-deadline').value = goal.deadline;
        })
        .catch(err => alert('❌ 목표 불러오기 실패: ' + err));
}

function deleteGoal(id) {
    if (!isAuthenticated) return;
    if (!confirm("정말 삭제하시겠습니까?")) return;

    csrfFetch(`${goalApiUrl}/${id}`, { method: 'DELETE' })
        .then(() => {
            alert("🗑️ 삭제 완료");
            loadGoals();
        })
        .catch(() => alert("❌ 삭제 실패"));
}

function resetGoalForm() {
    document.getElementById('goal-form').reset();
    document.getElementById('goal-id').value = '';
    document.getElementById('form-title').innerText = '목표 등록';
}