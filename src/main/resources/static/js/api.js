const API_BASE = 'http://localhost:8080/api/v1';

const api = {
  getToken() {
    return localStorage.getItem('wm_token');
  },

  getUser() {
    const data = localStorage.getItem('wm_user');
    return data ? JSON.parse(data) : null;
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  async request(endpoint, options = {}) {
    const token = this.getToken();
    const headers = {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
      ...options.headers,
    };

    const res = await fetch(`${API_BASE}${endpoint}`, {
      ...options,
      headers,
    });

    if (!res.ok) {
      const error = await res.json().catch(() => ({ message: `Error ${res.status}` }));
      if (res.status === 401) {
        localStorage.removeItem('wm_token');
        localStorage.removeItem('wm_user');
        window.location.href = '/producto/login.html';
      }
      throw new Error(error.message || `Error ${res.status}`);
    }

    if (res.status === 204) return null;
    return res.json();
  },

  get(endpoint) {
    return this.request(endpoint, { method: 'GET' });
  },

  post(endpoint, data) {
    return this.request(endpoint, { method: 'POST', body: JSON.stringify(data) });
  },

  put(endpoint, data) {
    return this.request(endpoint, { method: 'PUT', body: JSON.stringify(data) });
  },

  patch(endpoint, data) {
    return this.request(endpoint, { method: 'PATCH', body: JSON.stringify(data) });
  },

  del(endpoint) {
    return this.request(endpoint, { method: 'DELETE' });
  },

  // ===================== AUTH =====================
  async login(username, password) {
    const data = await this.post('/auth/login', { username, password });
    localStorage.setItem('wm_token', data.token);
    localStorage.setItem('wm_user', JSON.stringify({
      id: data.id, username: data.username, email: data.email, roles: data.roles
    }));
    return data;
  },

  async register(userData) {
    return this.post('/auth/register', userData);
  },

  logout() {
    localStorage.removeItem('wm_token');
    localStorage.removeItem('wm_user');
    window.location.href = '/producto/login.html';
  },

  // ===================== CLIENTES =====================
  getClientes(page = 0, size = 10) {
    return this.get(`/clientes?page=${page}&size=${size}`);
  },

  getCliente(id) {
    return this.get(`/clientes/${id}`);
  },

  createCliente(data) {
    return this.post('/clientes', data);
  },

  updateCliente(id, data) {
    return this.put(`/clientes/${id}`, data);
  },

  deleteCliente(id) {
    return this.del(`/clientes/${id}`);
  },

  // ===================== MUEBLES =====================
  getMuebles(page = 0, size = 20) {
    return this.get(`/muebles?page=${page}&size=${size}`);
  },

  getMueble(id) {
    return this.get(`/muebles/${id}`);
  },

  createMueble(data) {
    return this.post('/muebles', data);
  },

  updateMueble(id, data) {
    return this.put(`/muebles/${id}`, data);
  },

  deleteMueble(id) {
    return this.del(`/muebles/${id}`);
  },

  // ===================== INVENTARIO MADERA =====================
  getInventario(page = 0, size = 10) {
    return this.get(`/inventario-madera?page=${page}&size=${size}`);
  },

  getInventarioItem(id) {
    return this.get(`/inventario-madera/${id}`);
  },

  getStockBajo() {
    return this.get('/inventario-madera/bajo-stock');
  },

  createInventario(data) {
    return this.post('/inventario-madera', data);
  },

  updateInventario(id, data) {
    return this.put(`/inventario-madera/${id}`, data);
  },

  deleteInventario(id) {
    return this.del(`/inventario-madera/${id}`);
  },

  // ===================== PEDIDOS =====================
  getPedidos(page = 0, size = 10, status = '') {
    let url = `/pedidos?page=${page}&size=${size}`;
    if (status) url += `&status=${status}`;
    return this.get(url);
  },

  getPedido(id) {
    return this.get(`/pedidos/${id}`);
  },

  getPedidosByCliente(clientId, page = 0, size = 10) {
    return this.get(`/pedidos/cliente/${clientId}?page=${page}&size=${size}`);
  },

  createPedido(data) {
    return this.post('/pedidos', data);
  },

  updatePedidoStatus(id, status) {
    return this.patch(`/pedidos/${id}/estado?status=${status}`);
  },

  // ===================== FACTURAS =====================
  getFacturas(page = 0, size = 10) {
    return this.get(`/facturas?page=${page}&size=${size}`);
  },

  getFactura(id) {
    return this.get(`/facturas/${id}`);
  },

  createFactura(data) {
    return this.post('/facturas', data);
  },

  registrarPago(id, amount) {
    return this.post(`/facturas/${id}/pago?amount=${amount}`);
  },

  // ===================== DASHBOARD =====================
  getDashboardAdmin() {
    return this.get('/dashboard/admin');
  },

  getDashboardEmpleado() {
    return this.get('/dashboard/empleado');
  },

  // ===================== REPORTES =====================
  getReporteVentas(startDate, endDate) {
    return this.get(`/reportes/ventas?startDate=${startDate}&endDate=${endDate}`);
  },

  getReporteInventario() {
    return this.get('/reportes/inventario');
  },

  getMasVendidos() {
    return this.get('/reportes/mas-vendidos');
  }
};
