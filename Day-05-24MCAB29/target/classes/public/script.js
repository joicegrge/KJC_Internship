// App State
let manageMode = false;
let isEditing = false;
let currentPage = 1;
let totalPages = 1;
let currentSort = "name";
let currentOrder = "asc";
let departmentChart = null;
let hiringTrendChart = null;

// Initialize the app
document.addEventListener('DOMContentLoaded', () => {
    loadDashboardStats();
    setupEventListeners();
    fetchPaginatedEmployees(1);
});

function setupEventListeners() {
    // Form submission
    document.getElementById('employee-form').addEventListener('submit', async (e) => {
        e.preventDefault();
        await handleEmployeeFormSubmit();
    });

    // Navigation
    document.querySelectorAll('.app-sidebar a').forEach(link => {
        link.addEventListener('click', function(e) {
            document.querySelectorAll('.app-sidebar li').forEach(li => li.classList.remove('active'));
            this.parentElement.classList.add('active');
        });
    });
}

// Theme Toggle
function toggleTheme() {
    document.body.classList.toggle('dark-theme');
    const themeToggle = document.querySelector('.theme-toggle i');
    if (document.body.classList.contains('dark-theme')) {
        themeToggle.classList.remove('fa-moon');
        themeToggle.classList.add('fa-sun');
    } else {
        themeToggle.classList.remove('fa-sun');
        themeToggle.classList.add('fa-moon');
    }
}

// Navigation Functions
function loadDashboard() {
    hideAllSections();
    document.getElementById('dashboard-section').classList.remove('hidden');
    loadDashboardStats();
}

function showEmployeeSection() {
    hideAllSections();
    document.getElementById('employee-section').classList.remove('hidden');
    document.getElementById('employee-table').classList.remove('hidden');
    fetchPaginatedEmployees(1);
}

function showAnalytics() {
    hideAllSections();
    document.getElementById('analytics-section').classList.remove('hidden');
    loadDepartmentStats();
    loadHiringTrends();
}

function hideAllSections() {
    document.querySelectorAll('.app-main > section').forEach(section => {
        section.classList.add('hidden');
    });
}

// Employee CRUD Operations
async function handleEmployeeFormSubmit() {
    const employee = {
        name: document.getElementById('employee-name').value,
        email: document.getElementById('employee-email').value,
        department: document.getElementById('employee-dept').value,
        skills: document.getElementById('employee-skills').value.split(',').map(s => s.trim()),
        joiningDate: document.getElementById('employee-date').value
    };

    const id = document.getElementById('employee-id').value;
    let response;

    try {
        showLoading();

        if (isEditing && id) {
            employee._id = id;
            response = await updateEmployee(employee);
            isEditing = false;
            document.getElementById('submit-btn').innerHTML = '<i class="fas fa-plus"></i> Add Employee';
        } else {
            response = await addEmployee(employee);
        }

        showMessage(response.message, 'success');
        document.getElementById('employee-form').reset();
        fetchPaginatedEmployees(currentPage);
        loadDashboardStats();
    } catch (error) {
        showMessage(error.message, 'error');
    } finally {
        hideLoading();
    }
}

async function addEmployee(employee) {
    const response = await fetch('/api/add-employee', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(employee)
    });
    return await response.json();
}

async function updateEmployee(employee) {
    const response = await fetch('/api/update-employee', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(employee)
    });
    return await response.json();
}

async function deleteEmployee(id) {
    if (!confirm('Are you sure you want to delete this employee?')) return;

    try {
        showLoading();
        const response = await fetch(`/api/delete-employee?id=${id}`, { method: 'DELETE' });
        const result = await response.json();
        showMessage(result.message, 'success');
        fetchPaginatedEmployees(currentPage);
        loadDashboardStats();
    } catch (error) {
        showMessage('Failed to delete employee', 'error');
    } finally {
        hideLoading();
    }
}

function editEmployee(employee) {
    document.getElementById('employee-id').value = employee._id.$oid;
    document.getElementById('employee-name').value = employee.name;
    document.getElementById('employee-email').value = employee.email;
    document.getElementById('employee-dept').value = employee.department;
    document.getElementById('employee-skills').value = employee.skills.join(', ');
    document.getElementById('employee-date').value = employee.joiningDate.substring(0, 10);
    document.getElementById('submit-btn').innerHTML = '<i class="fas fa-pen"></i> Update Employee';
    isEditing = true;
}

// Search and Filter
async function searchEmployees() {
    const params = new URLSearchParams();

    const name = document.getElementById('search-name').value;
    const dept = document.getElementById('search-dept').value;
    const skill = document.getElementById('search-skill').value;
    const from = document.getElementById('search-from').value;
    const to = document.getElementById('search-to').value;

    if (name) params.append('name', name);
    if (dept) params.append('department', dept);
    if (skill) params.append('skill', skill);
    if (from) params.append('from', from);
    if (to) params.append('to', to);

    try {
        showLoading();
        const response = await fetch(`/api/search-employees?${params.toString()}`);
        const employees = await response.json();
        renderEmployeeTable(employees);
    } catch (error) {
        showMessage('Search failed', 'error');
    } finally {
        hideLoading();
    }
}

function resetSearch() {
    document.getElementById('search-name').value = '';
    document.getElementById('search-dept').value = '';
    document.getElementById('search-skill').value = '';
    document.getElementById('search-from').value = '';
    document.getElementById('search-to').value = '';
    fetchPaginatedEmployees(1);
}

// Pagination
async function fetchPaginatedEmployees(page) {
    currentPage = page;
    currentSort = document.getElementById('sort-by').value;

    try {
        showLoading();
        const response = await fetch(
            `/api/employees-paginated?page=${page}&size=5&sort=${currentSort}&order=${currentOrder}`
        );
        const data = await response.json();

        renderEmployeeTable(data.data);
        updatePaginationInfo(data.currentPage, data.totalPages);
    } catch (error) {
        showMessage('Failed to load employees', 'error');
    } finally {
        hideLoading();
    }
}

function renderEmployeeTable(employees) {
    const tbody = document.querySelector('#employee-table tbody');
    tbody.innerHTML = '';

    employees.forEach(emp => {
        const row = document.createElement('tr');
        row.innerHTML = `
            <td>${emp.name}</td>
            <td>${emp.email}</td>
            <td>${emp.department}</td>
            <td>${emp.skills.join(', ')}</td>
            <td>${emp.joiningDate.substring(0, 10)}</td>
            <td class="manage-col ${manageMode ? '' : 'hidden'}">
                <button onclick="editEmployee(${JSON.stringify(emp).replace(/"/g, '&quot;')})">
                    <i class="fas fa-pen"></i> Edit
                </button>
                <button onclick="deleteEmployee('${emp._id.$oid}')">
                    <i class="fas fa-trash"></i> Delete
                </button>
            </td>
        `;
        tbody.appendChild(row);
    });

    document.getElementById('employee-table').classList.remove('hidden');
}

function updatePaginationInfo(current, total) {
    currentPage = current;
    totalPages = total;
    document.getElementById('page-info').textContent = `Page ${currentPage} of ${totalPages}`;
}

function prevPage() {
    if (currentPage > 1) {
        fetchPaginatedEmployees(currentPage - 1);
    }
}

function nextPage() {
    if (currentPage < totalPages) {
        fetchPaginatedEmployees(currentPage + 1);
    }
}

// Dashboard Functions
async function loadDashboardStats() {
    try {
        const response = await fetch('/api/dashboard-stats');
        const data = await response.json();

        document.getElementById('totalEmployees').textContent = data.totalEmployees;
        document.getElementById('totalDepartments').textContent = data.totalDepartments;
        document.getElementById('newHires').textContent = data.newHires;
    } catch (error) {
        console.error('Failed to load dashboard stats', error);
    }
}

// Analytics Functions
async function loadDepartmentStats() {
    try {
        showLoading();
        const response = await fetch('/api/department-stats');
        const stats = await response.json();

        // Prepare data for chart
        const departments = stats.map(stat => stat.department);
        const counts = stats.map(stat => stat.count);

        // Create or update chart
        const ctx = document.getElementById('departmentChart').getContext('2d');

        if (departmentChart) {
            departmentChart.destroy();
        }

        departmentChart = new Chart(ctx, {
            type: 'bar',
            data: {
                labels: departments,
                datasets: [{
                    label: 'Employees per Department',
                    data: counts,
                    backgroundColor: [
                        'rgba(108, 92, 231, 0.7)',
                        'rgba(0, 206, 201, 0.7)',
                        'rgba(253, 203, 110, 0.7)',
                        'rgba(85, 239, 196, 0.7)',
                        'rgba(255, 118, 117, 0.7)'
                    ],
                    borderColor: [
                        'rgba(108, 92, 231, 1)',
                        'rgba(0, 206, 201, 1)',
                        'rgba(253, 203, 110, 1)',
                        'rgba(85, 239, 196, 1)',
                        'rgba(255, 118, 117, 1)'
                    ],
                    borderWidth: 1
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {
                            color: '#f5f6fa'
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            color: '#dfe6e9'
                        },
                        grid: {
                            color: 'rgba(75, 90, 101, 0.5)'
                        }
                    },
                    x: {
                        ticks: {
                            color: '#dfe6e9'
                        },
                        grid: {
                            color: 'rgba(75, 90, 101, 0.5)'
                        }
                    }
                }
            }
        });
    } catch (error) {
        showMessage('Failed to load department stats', 'error');
    } finally {
        hideLoading();
    }
}

async function loadHiringTrends() {
    try {
        showLoading();
        // This is a mock function - you would replace with actual API call
        const response = await fetch('/api/hiring-trends');
        const trends = await response.json();

        // Prepare data for chart
        const months = ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun', 'Jul', 'Aug', 'Sep', 'Oct', 'Nov', 'Dec'];
        const hires = trends.map(trend => trend.count);

        // Create or update chart
        const ctx = document.getElementById('hiringTrendChart').getContext('2d');

        if (hiringTrendChart) {
            hiringTrendChart.destroy();
        }

        hiringTrendChart = new Chart(ctx, {
            type: 'line',
            data: {
                labels: months,
                datasets: [{
                    label: 'Monthly Hiring Trends',
                    data: hires,
                    backgroundColor: 'rgba(108, 92, 231, 0.2)',
                    borderColor: 'rgba(108, 92, 231, 1)',
                    borderWidth: 2,
                    tension: 0.4,
                    fill: true
                }]
            },
            options: {
                responsive: true,
                maintainAspectRatio: false,
                plugins: {
                    legend: {
                        position: 'top',
                        labels: {
                            color: '#f5f6fa'
                        }
                    }
                },
                scales: {
                    y: {
                        beginAtZero: true,
                        ticks: {
                            color: '#dfe6e9'
                        },
                        grid: {
                            color: 'rgba(75, 90, 101, 0.5)'
                        }
                    },
                    x: {
                        ticks: {
                            color: '#dfe6e9'
                        },
                        grid: {
                            color: 'rgba(75, 90, 101, 0.5)'
                        }
                    }
                }
            }
        });
    } catch (error) {
        showMessage('Failed to load hiring trends', 'error');
    } finally {
        hideLoading();
    }
}

// UI Helpers
function toggleEmployeeTable() {
    document.getElementById('employee-table').classList.toggle('hidden');
}

function toggleManageMode() {
    manageMode = !manageMode;
    document.querySelectorAll('.manage-col').forEach(col => {
        col.classList.toggle('hidden');
    });
}

function showDeleteByEmail() {
    const container = document.getElementById('delete-email-container');
    container.classList.toggle('hidden');
    if (!container.classList.contains('hidden')) {
        document.getElementById('email-to-delete').focus();
    }
}

async function deleteByEmail() {
    const email = document.getElementById('email-to-delete').value;
    if (!email) {
        showMessage('Please enter an email', 'error');
        return;
    }

    try {
        showLoading();
        const response = await fetch(`/api/delete-by-email?email=${encodeURIComponent(email)}`, {
            method: 'DELETE'
        });
        const result = await response.json();

        showMessage(result.message, 'success');
        document.getElementById('email-to-delete').value = '';
        document.getElementById('delete-email-container').classList.add('hidden');
        fetchPaginatedEmployees(currentPage);
        loadDashboardStats();
    } catch (error) {
        showMessage('Failed to delete employee', 'error');
    } finally {
        hideLoading();
    }
}

function showMessage(message, type) {
    const messageDiv = document.getElementById('form-message');
    messageDiv.textContent = message;
    messageDiv.className = type;
    messageDiv.style.display = 'block';
    setTimeout(() => messageDiv.style.display = 'none', 3000);
}

function showLoading() {
    document.getElementById('loading-overlay').classList.remove('hidden');
}

function hideLoading() {
    document.getElementById('loading-overlay').classList.add('hidden');
}