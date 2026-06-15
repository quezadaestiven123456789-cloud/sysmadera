package com.madera.sys_madera.controller;

import com.madera.sys_madera.config.RateLimitProperties;
import com.madera.sys_madera.security.CustomUserDetailsService;
import com.madera.sys_madera.security.jwt.JwtTokenProvider;
import com.madera.sys_madera.service.ReportService;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@WebMvcTest(value = ReportController.class, excludeAutoConfiguration = {
        DataSourceAutoConfiguration.class,
        HibernateJpaAutoConfiguration.class,
        JpaRepositoriesAutoConfiguration.class
})
@AutoConfigureMockMvc(addFilters = false)
@Import(ReportControllerTest.TestSecurityConfig.class)
@WithMockUser(roles = "ADMIN")
@DisplayName("ReportController")
class ReportControllerTest {

    @TestConfiguration
    @EnableMethodSecurity
    static class TestSecurityConfig {
    }

    private static final String BASE_PATH = "/api/v1/reportes";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ReportService reportService;

    @MockitoBean
    private JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private RateLimitProperties rateLimitProperties;

    @Nested
    @DisplayName("GET /reportes/ventas")
    class GetSalesReport {

        @Test
        @DisplayName("should return 200 with sales report data")
        void shouldReturn200() throws Exception {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("startDate", LocalDate.of(2025, 1, 1));
            report.put("endDate", LocalDate.of(2025, 12, 31));
            report.put("totalOrders", 4L);
            report.put("completedOrders", 2L);
            report.put("cancelledOrders", 1L);
            report.put("totalSales", new BigDecimal("1800.00"));

            given(reportService.getSalesReport(any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(report);

            mockMvc.perform(get(BASE_PATH + "/ventas")
                            .param("startDate", "2025-01-01")
                            .param("endDate", "2025-12-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").value(4))
                    .andExpect(jsonPath("$.completedOrders").value(2))
                    .andExpect(jsonPath("$.cancelledOrders").value(1))
                    .andExpect(jsonPath("$.totalSales").value(1800.00));
        }

        @Test
        @DisplayName("should return 200 with zeros when no orders found")
        void shouldReturnZeros_whenNoOrders() throws Exception {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("startDate", LocalDate.of(2025, 1, 1));
            report.put("endDate", LocalDate.of(2025, 12, 31));
            report.put("totalOrders", 0L);
            report.put("completedOrders", 0L);
            report.put("cancelledOrders", 0L);
            report.put("totalSales", BigDecimal.ZERO);

            given(reportService.getSalesReport(any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(report);

            mockMvc.perform(get(BASE_PATH + "/ventas")
                            .param("startDate", "2025-01-01")
                            .param("endDate", "2025-12-31"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalOrders").value(0))
                    .andExpect(jsonPath("$.completedOrders").value(0))
                    .andExpect(jsonPath("$.cancelledOrders").value(0))
                    .andExpect(jsonPath("$.totalSales").value(0));
        }
    }

    @Nested
    @DisplayName("GET /reportes/inventario")
    class GetInventoryReport {

        @Test
        @DisplayName("should return 200 with inventory report data")
        void shouldReturn200() throws Exception {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("totalWoodTypes", 2);
            report.put("totalQuantity", new BigDecimal("150"));
            report.put("totalValue", new BigDecimal("75000"));
            report.put("details", List.of());
            report.put("lowStockCount", 1);
            report.put("lowStockItems", List.of(Map.of(
                    "id", 1L, "woodType", "Caoba",
                    "quantity", new BigDecimal("5"), "unit", "m3",
                    "minimumStock", new BigDecimal("10"))));

            given(reportService.getInventoryReport()).willReturn(report);

            mockMvc.perform(get(BASE_PATH + "/inventario"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalWoodTypes").value(2))
                    .andExpect(jsonPath("$.totalQuantity").value(150))
                    .andExpect(jsonPath("$.totalValue").value(75000))
                    .andExpect(jsonPath("$.lowStockCount").value(1));
        }

        @Test
        @DisplayName("should return 200 with zeros when inventory is empty")
        void shouldReturnZeros_whenEmptyInventory() throws Exception {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("totalWoodTypes", 0);
            report.put("totalQuantity", BigDecimal.ZERO);
            report.put("totalValue", BigDecimal.ZERO);
            report.put("details", List.of());
            report.put("lowStockCount", 0);
            report.put("lowStockItems", List.of());

            given(reportService.getInventoryReport()).willReturn(report);

            mockMvc.perform(get(BASE_PATH + "/inventario"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalWoodTypes").value(0))
                    .andExpect(jsonPath("$.totalQuantity").value(0))
                    .andExpect(jsonPath("$.totalValue").value(0))
                    .andExpect(jsonPath("$.lowStockCount").value(0));
        }
    }

    @Nested
    @DisplayName("GET /reportes/mas-vendidos")
    class GetTopSellingFurniture {

        @Test
        @DisplayName("should return 200 with top selling furniture data")
        void shouldReturn200() throws Exception {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("totalItems", 2);
            report.put("totalSold", 30L);
            report.put("totalRevenue", new BigDecimal("8000"));
            report.put("details", List.of(
                    Map.of("furnitureId", 1, "furnitureName", "Mesa de Roble", "totalSold", 10, "totalRevenue", 5000),
                    Map.of("furnitureId", 2, "furnitureName", "Silla de Cedro", "totalSold", 20, "totalRevenue", 3000)
            ));

            given(reportService.getTopSellingFurniture()).willReturn(report);

            mockMvc.perform(get(BASE_PATH + "/mas-vendidos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(2))
                    .andExpect(jsonPath("$.totalSold").value(30))
                    .andExpect(jsonPath("$.totalRevenue").value(8000))
                    .andExpect(jsonPath("$.details.length()").value(2))
                    .andExpect(jsonPath("$.details[0].furnitureName").value("Mesa de Roble"))
                    .andExpect(jsonPath("$.details[1].furnitureName").value("Silla de Cedro"));
        }

        @Test
        @DisplayName("should return 200 with zeros when no sales data")
        void shouldReturnZeros_whenNoSalesData() throws Exception {
            Map<String, Object> report = new LinkedHashMap<>();
            report.put("totalItems", 0);
            report.put("totalSold", 0L);
            report.put("totalRevenue", BigDecimal.ZERO);
            report.put("details", List.of());

            given(reportService.getTopSellingFurniture()).willReturn(report);

            mockMvc.perform(get(BASE_PATH + "/mas-vendidos"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.totalItems").value(0))
                    .andExpect(jsonPath("$.totalSold").value(0))
                    .andExpect(jsonPath("$.totalRevenue").value(0))
                    .andExpect(jsonPath("$.details.length()").value(0));
        }
    }

    @Nested
    @DisplayName("access control")
    class AccessControl {

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN should access /ventas")
        void adminCanAccessSalesReport() throws Exception {
            given(reportService.getSalesReport(any(LocalDate.class), any(LocalDate.class)))
                    .willReturn(Map.of("totalOrders", 0L));

            mockMvc.perform(get(BASE_PATH + "/ventas")
                            .param("startDate", "2025-01-01")
                            .param("endDate", "2025-12-31"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN should access /inventario")
        void adminCanAccessInventoryReport() throws Exception {
            given(reportService.getInventoryReport()).willReturn(Map.of("totalWoodTypes", 0));

            mockMvc.perform(get(BASE_PATH + "/inventario"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "ADMIN")
        @DisplayName("ADMIN should access /mas-vendidos")
        void adminCanAccessTopSelling() throws Exception {
            given(reportService.getTopSellingFurniture()).willReturn(Map.of("totalItems", 0));

            mockMvc.perform(get(BASE_PATH + "/mas-vendidos"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should get 403 on /ventas")
        void empleadoCannotAccessSalesReport() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/ventas")
                            .param("startDate", "2025-01-01")
                            .param("endDate", "2025-12-31"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access /inventario")
        void empleadoCanAccessInventoryReport() throws Exception {
            given(reportService.getInventoryReport()).willReturn(Map.of("totalWoodTypes", 0));

            mockMvc.perform(get(BASE_PATH + "/inventario"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "EMPLEADO")
        @DisplayName("EMPLEADO should access /mas-vendidos")
        void empleadoCanAccessTopSelling() throws Exception {
            given(reportService.getTopSellingFurniture()).willReturn(Map.of("totalItems", 0));

            mockMvc.perform(get(BASE_PATH + "/mas-vendidos"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on /ventas")
        void clienteCannotAccessSalesReport() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/ventas")
                            .param("startDate", "2025-01-01")
                            .param("endDate", "2025-12-31"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on /inventario")
        void clienteCannotAccessInventoryReport() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/inventario"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = "CLIENTE")
        @DisplayName("CLIENTE should get 403 on /mas-vendidos")
        void clienteCannotAccessTopSelling() throws Exception {
            mockMvc.perform(get(BASE_PATH + "/mas-vendidos"))
                    .andExpect(status().isForbidden());
        }
    }

}
