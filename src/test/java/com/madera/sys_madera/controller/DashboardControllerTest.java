package com.madera.sys_madera.controller;

import com.madera.sys_madera.config.RateLimitProperties;
import com.madera.sys_madera.security.CustomUserDetailsService;
import com.madera.sys_madera.security.jwt.JwtTokenProvider;
import com.madera.sys_madera.service.DashboardService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(value = DashboardController.class, excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(DashboardControllerTest.TestSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("DashboardController")
class DashboardControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    private static final String BASE_PATH = "/api/v1/dashboard";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DashboardService dashboardService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    @Nested
    @DisplayName("GET /api/v1/dashboard/admin")
    class GetAdminDashboard {

        @Test
        @DisplayName("should return 200 with admin dashboard data")
        void shouldReturn200() throws Exception {
            Map<String, Object> adminData = new LinkedHashMap<>();
            adminData.put("totalClients", 10L);
            adminData.put("totalOrders", 25L);
            adminData.put("totalFurniture", 40L);
            adminData.put("totalWoodTypes", 8L);
            adminData.put("pendingOrders", 5L);
            adminData.put("inProductionOrders", 8L);
            adminData.put("completedOrders", 7L);
            adminData.put("deliveredOrders", 3L);
            adminData.put("cancelledOrders", 2L);
            adminData.put("paidInvoices", 15L);
            adminData.put("pendingInvoices", 6L);
            adminData.put("overdueInvoices", 2L);

            given(dashboardService.getAdminDashboard()).willReturn(adminData);

            mockMvc.perform(get(BASE_PATH + "/admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalClients").value(10))
                    .andExpect(jsonPath("$.totalOrders").value(25))
                    .andExpect(jsonPath("$.totalFurniture").value(40))
                    .andExpect(jsonPath("$.totalWoodTypes").value(8))
                    .andExpect(jsonPath("$.pendingOrders").value(5))
                    .andExpect(jsonPath("$.inProductionOrders").value(8))
                    .andExpect(jsonPath("$.completedOrders").value(7))
                    .andExpect(jsonPath("$.deliveredOrders").value(3))
                    .andExpect(jsonPath("$.cancelledOrders").value(2))
                    .andExpect(jsonPath("$.paidInvoices").value(15))
                    .andExpect(jsonPath("$.pendingInvoices").value(6))
                    .andExpect(jsonPath("$.overdueInvoices").value(2));
        }

        @Test
        @DisplayName("should return 200 with zeros when database is empty")
        void shouldReturnZeros_whenEmptyDatabase() throws Exception {
            Map<String, Object> emptyData = new LinkedHashMap<>();
            emptyData.put("totalClients", 0L);
            emptyData.put("totalOrders", 0L);
            emptyData.put("totalFurniture", 0L);
            emptyData.put("totalWoodTypes", 0L);
            emptyData.put("pendingOrders", 0L);
            emptyData.put("inProductionOrders", 0L);
            emptyData.put("completedOrders", 0L);
            emptyData.put("deliveredOrders", 0L);
            emptyData.put("cancelledOrders", 0L);
            emptyData.put("paidInvoices", 0L);
            emptyData.put("pendingInvoices", 0L);
            emptyData.put("overdueInvoices", 0L);

            given(dashboardService.getAdminDashboard()).willReturn(emptyData);

            mockMvc.perform(get(BASE_PATH + "/admin"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalClients").value(0))
                    .andExpect(jsonPath("$.totalOrders").value(0))
                    .andExpect(jsonPath("$.totalFurniture").value(0))
                    .andExpect(jsonPath("$.totalWoodTypes").value(0))
                    .andExpect(jsonPath("$.pendingOrders").value(0))
                    .andExpect(jsonPath("$.inProductionOrders").value(0))
                    .andExpect(jsonPath("$.completedOrders").value(0))
                    .andExpect(jsonPath("$.deliveredOrders").value(0))
                    .andExpect(jsonPath("$.cancelledOrders").value(0))
                    .andExpect(jsonPath("$.paidInvoices").value(0))
                    .andExpect(jsonPath("$.pendingInvoices").value(0))
                    .andExpect(jsonPath("$.overdueInvoices").value(0));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/dashboard/empleado")
    class GetEmployeeDashboard {

        @Test
        @DisplayName("should return 200 with employee dashboard data")
        void shouldReturn200() throws Exception {
            Map<String, Object> employeeData = new LinkedHashMap<>();
            employeeData.put("totalOrders", 25L);
            employeeData.put("pendingOrders", 5L);
            employeeData.put("inProductionOrders", 8L);
            employeeData.put("totalFurniture", 40L);

            given(dashboardService.getEmployeeDashboard()).willReturn(employeeData);

            mockMvc.perform(get(BASE_PATH + "/empleado"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").value(25))
                    .andExpect(jsonPath("$.pendingOrders").value(5))
                    .andExpect(jsonPath("$.inProductionOrders").value(8))
                    .andExpect(jsonPath("$.totalFurniture").value(40));
        }

        @Test
        @DisplayName("should return 200 with zeros when database is empty")
        void shouldReturnZeros_whenEmptyDatabase() throws Exception {
            Map<String, Object> emptyData = new LinkedHashMap<>();
            emptyData.put("totalOrders", 0L);
            emptyData.put("pendingOrders", 0L);
            emptyData.put("inProductionOrders", 0L);
            emptyData.put("totalFurniture", 0L);

            given(dashboardService.getEmployeeDashboard()).willReturn(emptyData);

            mockMvc.perform(get(BASE_PATH + "/empleado"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").value(0))
                    .andExpect(jsonPath("$.pendingOrders").value(0))
                    .andExpect(jsonPath("$.inProductionOrders").value(0))
                    .andExpect(jsonPath("$.totalFurniture").value(0));
        }
    }

    @Nested
    @DisplayName("access control")
    class AccessControl {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN should access /admin")
        void adminCanAccessAdminDashboard() throws Exception {
            given(dashboardService.getAdminDashboard()).willReturn(Map.of("totalClients", 1L));

            mockMvc.perform(get(BASE_PATH + "/admin"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN should access /empleado")
        void adminCanAccessEmployeeDashboard() throws Exception {
            given(dashboardService.getEmployeeDashboard()).willReturn(Map.of("totalOrders", 1L));

            mockMvc.perform(get(BASE_PATH + "/empleado"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should get 403 on /admin")
        void empleadoCannotAccessAdminDashboard() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access /empleado")
        void empleadoCanAccessEmployeeDashboard() throws Exception {
            given(dashboardService.getEmployeeDashboard()).willReturn(Map.of("totalOrders", 1L));

            mockMvc.perform(get(BASE_PATH + "/empleado"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on /admin")
        void clienteCannotAccessAdminDashboard() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/admin"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on /empleado")
        void clienteCannotAccessEmployeeDashboard() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/empleado"))
                    .andExpect(status().isForbidden());
        }
    }

}
