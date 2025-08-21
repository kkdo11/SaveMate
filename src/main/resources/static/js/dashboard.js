let pieChartInstance = null;
let barChartInstance = null;

window.onload = () => {
    fetch('/dashboardAPI/usage-summary')
        .then(res => {
            if (res.status === 401) { // 401 Unauthorized 상태 확인
                throw new Error('Unauthorized');
            }
            if (!res.ok) {
                // 서버에서 500 에러 등 다른 문제가 발생했을 경우를 대비
                throw new Error('Network response was not ok');
            }
            return res.json();
        })
        .then(data => {
            if (!data) throw new Error("No data returned");
            renderPieChart(data.categoryUsage);
            renderBarChart(data.monthlyBudget);
        })
        .catch(err => {
            if (err.message === 'Unauthorized') {
                fallbackChart("category-pie-chart", "로그인이 필요합니다. <a href='/user/login' class='text-blue-600 hover:underline'>로그인하기</a>");
                fallbackChart("monthly-bar-chart", "로그인 후 월별 예산과 사용량을 확인하세요.");
            } else {
                // 기타 네트워크 오류나 데이터 파싱 오류 등
                console.error("Dashboard data fetch failed:", err);
                fallbackChart("category-pie-chart", "데이터를 불러오는 중 오류가 발생했습니다.");
                fallbackChart("monthly-bar-chart", "새로고침 후 다시 시도해주세요.");
            }
        });
};

function fallbackChart(id, message) {
    const canvas = document.getElementById(id);
    const parent = canvas.parentElement;
    canvas.remove();
    const fallback = document.createElement('div');
    fallback.className = "text-center text-sm text-gray-500 w-full py-16";
    fallback.innerHTML = message; // innerHTML을 사용하여 a 태그 렌더링
    parent.appendChild(fallback);
}

function renderPieChart(categoryData) {
    const ctx = document.getElementById('category-pie-chart').getContext('2d');
    if (pieChartInstance) pieChartInstance.destroy();
    pieChartInstance = new Chart(ctx, {
        type: 'pie',
        data: {
            labels: Object.keys(categoryData),
            datasets: [{
                data: Object.values(categoryData),
                backgroundColor: [
                    '#6366F1', '#EC4899', '#F59E0B', '#10B981',
                    '#EF4444', '#3B82F6', '#8B5CF6', '#F87171'
                ]
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    position: 'bottom',
                    labels: {
                        color: '#4B5563',
                        padding: 20
                    }
                }
            }
        }
    });
}

function renderBarChart(monthlyData) {
    if (!monthlyData || monthlyData.length === 0) return;
    const ctx = document.getElementById('monthly-bar-chart').getContext('2d');
    if (barChartInstance) barChartInstance.destroy();

    const maxVal = Math.max(...monthlyData.flatMap(m => [m.budget, m.used])) * 1.1;
    barChartInstance = new Chart(ctx, {
        type: 'bar',
        data: {
            labels: monthlyData.map(item => item.month),
            datasets: [
                {
                    label: '예산',
                    backgroundColor: '#4F46E5',
                    data: monthlyData.map(item => item.budget)
                },
                {
                    label: '사용',
                    backgroundColor: '#EF4444',
                    data: monthlyData.map(item => item.used)
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    beginAtZero: true,
                    suggestedMax: maxVal,
                    ticks: {
                        callback: value => value.toLocaleString() + "원",
                        color: '#6B7280'
                    },
                    grid: {
                        color: '#E5E7EB'
                    }
                },
                x: {
                    ticks: { color: '#6B7280' },
                    grid: { display: false }
                }
            },
            plugins: {
                legend: {
                    position: 'top',
                    labels: {
                        color: '#374151'
                    }
                }
            }
        }
    });
}