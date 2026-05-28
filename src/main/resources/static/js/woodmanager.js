document.addEventListener('DOMContentLoaded', function () {
  initSidebar();
  initDropdowns();
  initCharts();
});

function initSidebar() {
  const toggleBtn = document.getElementById('toggleSidebar');
  const sidebar = document.getElementById('sidebar');

  if (toggleBtn && sidebar) {
    toggleBtn.addEventListener('click', function () {
      sidebar.classList.toggle('collapsed');
    });
  }

  const sidebarLinks = document.querySelectorAll('.nav-item');
  sidebarLinks.forEach(function (link) {
    link.addEventListener('click', function () {
      sidebarLinks.forEach(function (l) { l.classList.remove('active'); });
      this.classList.add('active');
    });
  });
}

function initDropdowns() {
  var dropdowns = document.querySelectorAll('[data-bs-toggle="dropdown"]');
  if (dropdowns.length > 0 && typeof bootstrap !== 'undefined') {
    dropdowns.forEach(function (el) {
      new bootstrap.Dropdown(el);
    });
  }
}

function initCharts() {
  if (typeof Chart === 'undefined') return;

  Chart.defaults.color = '#8b95a5';
  Chart.defaults.borderColor = '#1e2a3a';
  Chart.defaults.font.family = "'Inter', sans-serif";

  var salesChart = document.getElementById('salesChart');
  if (salesChart) {
    new Chart(salesChart, {
      type: 'line',
      data: {
        labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'],
        datasets: [{
          label: 'Ventas 2026',
          data: [18500, 22300, 19800, 25600, 28900, 31200, 27800, 34500, 32100, 36700, 41200, 45800],
          borderColor: '#C9A84C',
          backgroundColor: 'rgba(201, 168, 76, 0.08)',
          fill: true,
          tension: 0.4,
          pointBackgroundColor: '#C9A84C',
          pointBorderColor: '#141b2d',
          pointBorderWidth: 2,
          pointRadius: 4,
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: {
            grid: { color: 'rgba(30, 42, 58, 0.5)' },
            ticks: { callback: function (v) { return '$' + v.toLocaleString(); } }
          },
          x: { grid: { display: false } }
        }
      }
    });
  }

  var woodChart = document.getElementById('woodChart');
  if (woodChart) {
    new Chart(woodChart, {
      type: 'doughnut',
      data: {
        labels: ['Caoba', 'Roble', 'Cedro', 'Pino', 'Nogal', 'Otros'],
        datasets: [{
          data: [30, 25, 18, 12, 10, 5],
          backgroundColor: ['#C9A84C', '#8B5E3C', '#a0704a', '#d4a76a', '#5a3a2a', '#8b95a5'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        cutout: '70%',
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 16, usePointStyle: true, pointStyle: 'circle', font: { size: 11 } }
          }
        }
      }
    });
  }

  var monthlyChart = document.getElementById('monthlyChart');
  if (monthlyChart) {
    new Chart(monthlyChart, {
      type: 'bar',
      data: {
        labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun'],
        datasets: [
          { label: 'Órdenes', data: [24, 28, 22, 32, 35, 30], backgroundColor: '#C9A84C', borderRadius: 4 },
          { label: 'Muebles Prod.', data: [18, 22, 20, 28, 30, 26], backgroundColor: '#8B5E3C', borderRadius: 4 }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
            labels: { usePointStyle: true, pointStyle: 'circle', font: { size: 11 }, padding: 16 }
          }
        },
        scales: {
          y: { grid: { color: 'rgba(30, 42, 58, 0.5)' }, beginAtZero: true },
          x: { grid: { display: false } }
        }
      }
    });
  }

  var reportsSalesChart = document.getElementById('reportsSalesChart');
  if (reportsSalesChart) {
    new Chart(reportsSalesChart, {
      type: 'line',
      data: {
        labels: ['Ene', 'Feb', 'Mar', 'Abr', 'May', 'Jun', 'Jul', 'Ago', 'Sep', 'Oct', 'Nov', 'Dic'],
        datasets: [{
          label: 'Ventas',
          data: [18500, 22300, 19800, 25600, 28900, 31200, 27800, 34500, 32100, 36700, 41200, 45800],
          borderColor: '#C9A84C',
          backgroundColor: 'rgba(201, 168, 76, 0.08)',
          fill: true,
          tension: 0.4,
          pointBackgroundColor: '#C9A84C',
          pointRadius: 3,
          borderWidth: 2
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: { legend: { display: false } },
        scales: {
          y: { grid: { color: 'rgba(30, 42, 58, 0.5)' }, beginAtZero: true },
          x: { grid: { display: false } }
        }
      }
    });
  }

  var woodUsageChart = document.getElementById('woodUsageChart');
  if (woodUsageChart) {
    new Chart(woodUsageChart, {
      type: 'bar',
      data: {
        labels: ['Caoba', 'Roble', 'Cedro', 'Pino', 'Nogal'],
        datasets: [
          { label: 'Utilizado (m³)', data: [45, 38, 22, 18, 12], backgroundColor: '#C9A84C', borderRadius: 4 },
          { label: 'Disponible (m³)', data: [60, 50, 35, 28, 20], backgroundColor: '#1e2a3a', borderRadius: 4 }
        ]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'top',
            labels: { usePointStyle: true, pointStyle: 'circle', font: { size: 11 }, padding: 16 }
          }
        },
        scales: {
          y: { grid: { color: 'rgba(30, 42, 58, 0.5)' }, beginAtZero: true },
          x: { grid: { display: false } }
        }
      }
    });
  }

  var wasteChart = document.getElementById('wasteChart');
  if (wasteChart) {
    new Chart(wasteChart, {
      type: 'pie',
      data: {
        labels: ['Reutilizable', 'Desperdicio', 'Reciclado'],
        datasets: [{
          data: [55, 25, 20],
          backgroundColor: ['#C9A84C', '#ff4d4f', '#4facfe'],
          borderWidth: 0
        }]
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: {
            position: 'bottom',
            labels: { padding: 16, usePointStyle: true, pointStyle: 'circle', font: { size: 11 } }
          }
        }
      }
    });
  }
}

function searchTable(inputId, tableId) {
  var input = document.getElementById(inputId);
  var table = document.getElementById(tableId);
  if (!input || !table) return;

  input.addEventListener('keyup', function () {
    var filter = this.value.toLowerCase();
    var rows = table.querySelectorAll('tbody tr');
    rows.forEach(function (row) {
      var text = row.textContent.toLowerCase();
      row.style.display = text.includes(filter) ? '' : 'none';
    });
  });
}

function confirmDelete(name) {
  if (typeof Swal !== 'undefined') {
    Swal.fire({
      title: '¿Eliminar?',
      text: '¿Estás seguro de eliminar ' + name + '?',
      icon: 'warning',
      showCancelButton: true,
      confirmButtonColor: '#ff4d4f',
      cancelButtonColor: '#1e2a3a',
      confirmButtonText: 'Sí, eliminar',
      cancelButtonText: 'Cancelar',
      background: '#141b2d',
      color: '#fff'
    });
  } else {
    return confirm('¿Estás seguro de eliminar ' + name + '?');
  }
}

function formatCurrency(amount) {
  return '$' + Number(amount).toLocaleString('es-MX', { minimumFractionDigits: 2, maximumFractionDigits: 2 });
}
