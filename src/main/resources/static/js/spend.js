const apiUrl = '/spendingAPI';
let filterMonth = '';
let filterCategory = '';
const csrfToken = document.querySelector('meta[name="_csrf"]').content;
const csrfHeader = document.querySelector('meta[name="_csrf_header"]').content;

async function csrfFetch(url, options = {}) {
    options.headers = {
        ...options.headers,
        [csrfHeader]: csrfToken,
    };

    const res = await fetch(url, options);

    if (res.status === 401) {
        const error = new Error('Unauthorized');
        error.status = 401;
        throw error;
    }

    if (!res.ok) {
        throw new Error(`${res.status} ${res.statusText}`);
    }

    const contentType = res.headers.get("content-type") || "";
    if (contentType.includes("application/json")) {
        return await res.json();
    }

    return null;
}


window.onload = () => {
    if (!isAuthenticated) {
        document.getElementById('authNotice').classList.remove('hidden');

        document.querySelectorAll('#spendingForm input, #spendingForm select, #spendingForm button').forEach(el => {
            el.disabled = true;
            el.classList.add('opacity-50', 'cursor-not-allowed');
        });
    }

    getSpendings();
};

function applyFilters() {
    filterMonth = document.getElementById('filterMonth').value;
    filterCategory = document.getElementById('filterCategory').value;
    getSpendings();
}

async function getSpendings() {
    document.getElementById('loading').classList.remove('hidden');
    const listContainer = document.getElementById('spendingList');
    listContainer.innerHTML = ''; // Start by clearing the list

    const url = `${apiUrl}?month=${filterMonth}&category=${filterCategory}`;

    try {
        const data = await csrfFetch(url);
        if (data) {
            renderSpendings(data);
        }
    } catch (err) {
        if (err.status === 401) {
            renderUnauthorized('spendingList', '소비 내역을 보려면 로그인이 필요합니다.');
        } else {
            console.error("Failed to fetch spendings:", err);
            listContainer.innerHTML = '<p class="text-center text-red-500">데이터를 불러오는 중 오류가 발생했습니다.</p>';
        }
    } finally {
        document.getElementById('loading').classList.add('hidden');
    }
}

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


function renderSpendings(data) {
    const list = document.getElementById('spendingList');
    list.innerHTML = '';

    data.forEach(sp => {
        const item = document.createElement('div');
        item.className = "p-4 rounded-lg bg-white shadow flex flex-col md:flex-row md:items-center md:justify-between hover:bg-gray-50 transition";

        item.innerHTML = `
                <div>
                  <h4 class="text-lg font-semibold text-gray-800">${sp.name}</h4>
                  <p class="text-sm text-gray-500 mt-1">
                    📁 ${sp.category} | 💰 ${sp.amount.toLocaleString()}원 | 📅 ${sp.date}
                  </p>
                  <p class="text-sm text-gray-600 mt-1">${sp.description || '-'}</p>
                </div>
                <div class="flex space-x-2 mt-4 md:mt-0">
                  <button onclick="editSpending('${sp.id?.$oid || sp.id}')" class="edit-btn bg-yellow-400 text-white px-3 py-1 rounded hover:bg-yellow-500">수정</button>
                  <button onclick="deleteSpending('${sp.id?.$oid || sp.id}')" class="delete-btn bg-red-500 text-white px-3 py-1 rounded hover:bg-red-600">삭제</button>
                </div>
            `;

        list.appendChild(item);

        if (!isAuthenticated) {
            item.querySelectorAll('button').forEach(btn => {
                btn.disabled = true;
                btn.classList.add('opacity-50', 'cursor-not-allowed');
            });
        }
    });
}

document.getElementById('spendingForm').addEventListener('submit', async function (e) {
    e.preventDefault();

    if (!isAuthenticated) {
        alert("❗ 로그인 후 저장할 수 있습니다.");
        return;
    }

    const name = document.getElementById('name').value.trim();
    const category = document.getElementById('category').value.trim();
    const amount = parseInt(document.getElementById('amount').value);
    const description = document.getElementById('description').value.trim();
    const date = document.getElementById('date').value.trim();

    if (!name || !category || !date || !amount ||!description || amount <= 0) {
        alert("⚠️ 모든 항목을 정확히 입력해주세요.");
        return;
    }

    const spending = { name, category, amount, description, date };
    const id = document.getElementById('spendingId').value;
    const method = id ? 'PUT' : 'POST';
    const url = id ? `${apiUrl}/${id}` : apiUrl;

    try {
        await csrfFetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(spending),
        });
        alert('✅ 저장 완료');
        resetForm();
        getSpendings();
    } catch (err) {
        alert('❌ 저장 실패: ' + err.message);
    }
});

async function editSpending(id) {
    if (!isAuthenticated) return alert("❗ 로그인 후 이용 가능합니다.");

    try {
        const data = await csrfFetch(`${apiUrl}/${id}`);
        if (data) {
            document.getElementById('formTitle').innerText = '지출 수정';
            document.getElementById('spendingId').value = data.id;
            document.getElementById('name').value = data.name;
            document.getElementById('category').value = data.category;
            document.getElementById('amount').value = data.amount;
            document.getElementById('description').value = data.description;
            document.getElementById('date').value = data.date;
        }
    } catch (err) {
        alert('❌ 수정 데이터 불러오기 실패: ' + err.message);
    }
}

async function deleteSpending(id) {
    if (!isAuthenticated) return alert("❗ 로그인 후 삭제할 수 있습니다.");
    if (!confirm('정말 삭제하시겠습니까?')) return;

    try {
        await csrfFetch(`${apiUrl}/${id}`, {
            method: 'DELETE'
        });
        alert('🗑️ 삭제 완료');
        getSpendings();
    } catch (err) {
        alert('❌ 삭제 실패: ' + err.message);
    }
}

function resetForm() {
    document.getElementById('spendingForm').reset();
    document.getElementById('spendingId').value = '';
    document.getElementById('formTitle').innerText = '지출 등록';
}