let pieChartInstance = null;
let barChartInstance = null;

window.onload = () => {
    fetch('/dashboardAPI/usage-summary')
        .then(res => res.json())
        .then(data => {
            if (!data) throw new Error("No data returned");
            renderPieChart(data.categoryUsage);
            renderBarChart(data.monthlyBudget);
        })
        .catch(err => {
            alert("📉 데이터 불러오기 실패: " + err.message);
            fallbackChart("category-pie-chart", "카테고리 데이터 없음");
            fallbackChart("monthly-bar-chart", "월별 예산 데이터 없음");
        });
};

function fallbackChart(id, message) {
    const canvas = document.getElementById(id);
    const parent = canvas.parentElement;
    canvas.remove();
    const fallback = document.createElement('div');
    fallback.className = "text-center text-sm text-gray-500 w-full py-16";
    fallback.textContent = message;
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