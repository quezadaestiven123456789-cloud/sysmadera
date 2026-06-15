package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.model.EInvoiceStatus;
import com.madera.sys_madera.model.EOrderStatus;
import com.madera.sys_madera.repository.ClientRepository;
import com.madera.sys_madera.repository.FurnitureRepository;
import com.madera.sys_madera.repository.InvoiceRepository;
import com.madera.sys_madera.repository.OrderRepository;
import com.madera.sys_madera.repository.WoodInventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardServiceImpl")
class DashboardServiceImplTest {

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private FurnitureRepository furnitureRepository;

    @Mock
    private WoodInventoryRepository woodInventoryRepository;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Nested
    @DisplayName("getAdminDashboard")
    class GetAdminDashboard {

        @Test
        @DisplayName("should return all admin metrics when data exists")
        void shouldReturnAllMetrics() {
            given(clientRepository.count()).willReturn(10L);
            given(orderRepository.count()).willReturn(25L);
            given(furnitureRepository.count()).willReturn(40L);
            given(woodInventoryRepository.count()).willReturn(8L);
            given(orderRepository.countByStatus(EOrderStatus.PENDIENTE)).willReturn(5L);
            given(orderRepository.countByStatus(EOrderStatus.EN_PRODUCCION)).willReturn(8L);
            given(orderRepository.countByStatus(EOrderStatus.COMPLETADO)).willReturn(7L);
            given(orderRepository.countByStatus(EOrderStatus.ENTREGADO)).willReturn(3L);
            given(orderRepository.countByStatus(EOrderStatus.CANCELADO)).willReturn(2L);
            given(invoiceRepository.countByStatus(EInvoiceStatus.PAGADA)).willReturn(15L);
            given(invoiceRepository.countByStatus(EInvoiceStatus.PENDIENTE)).willReturn(6L);
            given(invoiceRepository.countByStatus(EInvoiceStatus.VENCIDA)).willReturn(2L);

            Map<String, Object> result = dashboardService.getAdminDashboard();

            assertThat(result)
                    .hasSize(12)
                    .containsEntry("totalClients", 10L)
                    .containsEntry("totalOrders", 25L)
                    .containsEntry("totalFurniture", 40L)
                    .containsEntry("totalWoodTypes", 8L)
                    .containsEntry("pendingOrders", 5L)
                    .containsEntry("inProductionOrders", 8L)
                    .containsEntry("completedOrders", 7L)
                    .containsEntry("deliveredOrders", 3L)
                    .containsEntry("cancelledOrders", 2L)
                    .containsEntry("paidInvoices", 15L)
                    .containsEntry("pendingInvoices", 6L)
                    .containsEntry("overdueInvoices", 2L);
        }

        @Test
        @DisplayName("should return zeros when database is empty")
        void shouldReturnZeros_whenEmptyDatabase() {
            given(clientRepository.count()).willReturn(0L);
            given(orderRepository.count()).willReturn(0L);
            given(furnitureRepository.count()).willReturn(0L);
            given(woodInventoryRepository.count()).willReturn(0L);
            given(orderRepository.countByStatus(EOrderStatus.PENDIENTE)).willReturn(0L);
            given(orderRepository.countByStatus(EOrderStatus.EN_PRODUCCION)).willReturn(0L);
            given(orderRepository.countByStatus(EOrderStatus.COMPLETADO)).willReturn(0L);
            given(orderRepository.countByStatus(EOrderStatus.ENTREGADO)).willReturn(0L);
            given(orderRepository.countByStatus(EOrderStatus.CANCELADO)).willReturn(0L);
            given(invoiceRepository.countByStatus(EInvoiceStatus.PAGADA)).willReturn(0L);
            given(invoiceRepository.countByStatus(EInvoiceStatus.PENDIENTE)).willReturn(0L);
            given(invoiceRepository.countByStatus(EInvoiceStatus.VENCIDA)).willReturn(0L);

            Map<String, Object> result = dashboardService.getAdminDashboard();

            assertThat(result)
                    .hasSize(12)
                    .allSatisfy((key, value) -> assertThat(value).isEqualTo(0L));
        }
    }

    @Nested
    @DisplayName("getEmployeeDashboard")
    class GetEmployeeDashboard {

        @Test
        @DisplayName("should return employee metrics when data exists")
        void shouldReturnEmployeeMetrics() {
            given(orderRepository.count()).willReturn(25L);
            given(orderRepository.countByStatus(EOrderStatus.PENDIENTE)).willReturn(5L);
            given(orderRepository.countByStatus(EOrderStatus.EN_PRODUCCION)).willReturn(8L);
            given(furnitureRepository.count()).willReturn(40L);

            Map<String, Object> result = dashboardService.getEmployeeDashboard();

            assertThat(result)
                    .hasSize(4)
                    .containsEntry("totalOrders", 25L)
                    .containsEntry("pendingOrders", 5L)
                    .containsEntry("inProductionOrders", 8L)
                    .containsEntry("totalFurniture", 40L);
        }

        @Test
        @DisplayName("should return zeros when database is empty")
        void shouldReturnZeros_whenEmptyDatabase() {
            given(orderRepository.count()).willReturn(0L);
            given(orderRepository.countByStatus(EOrderStatus.PENDIENTE)).willReturn(0L);
            given(orderRepository.countByStatus(EOrderStatus.EN_PRODUCCION)).willReturn(0L);
            given(furnitureRepository.count()).willReturn(0L);

            Map<String, Object> result = dashboardService.getEmployeeDashboard();

            assertThat(result)
                    .hasSize(4)
                    .allSatisfy((key, value) -> assertThat(value).isEqualTo(0L));
        }
    }

}
