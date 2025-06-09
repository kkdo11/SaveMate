const goalApiUrl = '/goalAPI';

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
    safeFetch(goalApiUrl)
        .then(data => {
            if (data) renderGoals(data);
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
        alert("⚠️ 모든 필드를 입력해주세요.");
        return;
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

    safeFetch(`${goalApiUrl}/${id}`)
        .then(goal => {
            if (!goal) return;
            document.getElementById('form-title').innerText = '목표 수정';
            document.getElementById('goal-id').value = goal.goalId;
            document.getElementById('goal-name').value = goal.goalName;
            document.getElementById('goal-target').value = goal.targetAmount;
            document.getElementById('goal-saved').value = goal.savedAmount;
            document.getElementById('goal-deadline').value = goal.deadline;
        });
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