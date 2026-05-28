package com.madera.sys_madera.service;

import java.util.Map;

public interface DashboardService {

    Map<String, Object> getAdminDashboard();

    Map<String, Object> getEmployeeDashboard();

}
