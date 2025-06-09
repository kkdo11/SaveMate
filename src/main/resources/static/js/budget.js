const apiUrl = '/budgetAPI';

// ✅ 초기 로딩
window.onload = () => {
    if (!isAuthenticated) {
        document.getElementById('authNotice').classList.remove('hidden');
        document.querySelectorAll('#budgetForm input, #budgetForm select, #budgetForm button')
            .forEach(el => {
                el.disabled = true;
                el.classList.add('opacity-50', 'cursor-not-allowed');
            });
    }

    // 오늘 날짜 기준으로 월 input 기본값 설정
    const now = new Date();
    const yyyy = now.getFullYear();
    const mm = String(now.getMonth() + 1).padStart(2, '0');
    const thisMonth = `${yyyy}-${mm}`;
    document.getElementById('searchMonth').value = thisMonth;

    // 현재 월 예산 자동 조회 및 렌더링
    csrfFetch(`/budgetAPI/monthly?month=${thisMonth}`)
        .then(res => res.json())
        .then(data => renderBudgetCards(data));
};


// ✅ 예산 폼 제출 처리
document.getElementById('budgetForm').addEventListener('submit', function (e) {
    e.preventDefault();

    const rawId = document.getElementById('budgetId').value;
    const id = rawId && rawId !== 'undefined' && !isNaN(rawId) ? parseInt(rawId) : null;

    const dateVal = document.getElementById('budgetMonth').value;
    const [year, month] = dateVal.split('-');
    const category = document.getElementById('budgetCategory').value;
    const totalBudget = parseInt(document.getElementById('totalBudget').value);

    if (!year || !month || !category || !totalBudget) {
        alert('❗ 모든 항목을 입력해주세요.');
        return;
    }

    const payload = {
        year: parseInt(year),
        month: parseInt(month),
        category: category,
        total_budget: totalBudget,
        used_budget: 0
    };

    const method = id ? 'PUT' : 'POST';
    const url = id ? `${apiUrl}/${id}` : apiUrl;

    csrfFetch(url, {
        method,
        body: JSON.stringify(payload)
    })
        .then(res => {
            if (!res.ok) throw new Error('요청 실패');
            const contentType = res.headers.get("content-type");
            if (contentType && contentType.includes("application/json")) {
                return res.json();
            } else {
                return res.text();
            }
        })
        .then(() => {
            alert('✅ 저장 완료');
            resetForm();
            // 현재 선택된 월로 다시 조회
            const selectedMonth = document.getElementById('searchMonth').value;
            csrfFetch(`/budgetAPI/monthly?month=${selectedMonth}`)
                .then(res => res.json())
                .then(data => renderBudgetCards(data));
        })
        .catch(err => alert('❌ 저장 실패: ' + err));
});



function resetForm() {
    document.getElementById('budgetForm').reset();
    document.getElementById('budgetId').value = '';
}



function loadBudgets() {
    fetch(apiUrl)
        .then(res => res.json())
        .then(renderBudgetCards);
}
// 월별 예산 조회 기능
function renderBudgetCards(data) {
    const list = document.getElementById('budgetList');
    list.innerHTML = '';
    data.forEach(budget => {
        const total = budget.totalBudget != null ? Number(budget.totalBudget).toLocaleString() : '0';
        const used = budget.usedBudget != null ? Number(budget.usedBudget).toLocaleString() : '0';
        const remain = budget.remainingBudget != null ? Number(budget.remainingBudget).toLocaleString() : '0';
        const card = document.createElement('div');
        card.className = 'border rounded shadow p-4 bg-gray-50';
        card.innerHTML = `
            <h3 class="text-lg font-semibold mb-2 text-purple-600">${budget.category}</h3>
            <p><strong>연도/월:</strong> ${budget.year}년 ${String(budget.month).padStart(2, '0')}월</p>
            <p><strong>총 예산:</strong> ${total}원</p>
            <p><strong>사용액:</strong> ${used}원</p>
            <p><strong>남은 금액:</strong> ${remain}원</p>
            <div class="flex justify-end space-x-2 mt-4">
                <button onclick="editBudget(${budget.budgetId})" class="bg-yellow-400 text-white px-3 py-1 rounded hover:bg-yellow-500">수정</button>
                <button onclick="deleteBudget(${budget.budgetId})" class="bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600">삭제</button>
            </div>
        `;
        list.appendChild(card);
    });
}

function editBudget(id) {
    fetch(`${apiUrl}/${id}`)
        .then(res => res.json())
        .then(data => {
            console.log('[DEBUG] 불러온 budget:', data);

            document.getElementById('budgetId').value = data.budget_id ?? data.budgetId;
            document.getElementById('budgetMonth').value = `${data.year}-${String(data.month).padStart(2, '0')}`;
            document.getElementById('budgetCategory').value = data.category;
            document.getElementById('totalBudget').value = data.total_budget ?? data.totalBudget;
        })
        .catch(err => alert('❌ 예산 불러오기 실패: ' + err));
}



function deleteBudget(id) {
    if (!confirm('정말 삭제하시겠습니까?')) return;

    csrfFetch(`${apiUrl}/${id}`, {
        method: 'DELETE'
    })
        .then(() => {
            alert('🗑️ 삭제 완료');
            // 현재 선택된 월로 다시 조회
            const selectedMonth = document.getElementById('searchMonth').value;
            csrfFetch(`/budgetAPI/monthly?month=${selectedMonth}`)
                .then(res => res.json())
                .then(data => renderBudgetCards(data));
        })
        .catch(err => alert('❌ 삭제 실패: ' + err));
}

// 월별 예산 조회 버튼 클릭 시 budgetList에 표시
document.getElementById('searchBudgetBtn').addEventListener('click', function() {
    const month = document.getElementById('searchMonth').value;
    if (!month) {
        alert('월을 선택하세요.');
        return;
    }
    csrfFetch(`/budgetAPI/monthly?month=${month}`)
        .then(res => res.json())
        .then(data => {
            renderBudgetCards(data); // budgetList에 월별 예산 표시
        })
        .catch(() => alert('예산 조회에 실패했습니다.'));
});