package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.response.TopSellingFurnitureItem;
import com.madera.sys_madera.dto.response.WoodInventoryReportItem;
import com.madera.sys_madera.model.EOrderStatus;
import com.madera.sys_madera.model.Order;
import com.madera.sys_madera.model.WoodInventory;
import com.madera.sys_madera.repository.OrderDetailRepository;
import com.madera.sys_madera.repository.OrderRepository;
import com.madera.sys_madera.repository.WoodInventoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReportServiceImpl")
class ReportServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderDetailRepository orderDetailRepository;

    @Mock
    private WoodInventoryRepository woodInventoryRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Nested
    @DisplayName("getSalesReport")
    class GetSalesReport {

        private static final LocalDate START_DATE = LocalDate.of(2025, 1, 1);
        private static final LocalDate END_DATE = LocalDate.of(2025, 12, 31);

        @Test
        @DisplayName("should return sales metrics when orders exist")
        void shouldReturnSalesMetrics() {
            var order1 = Order.builder()
                    .status(EOrderStatus.COMPLETADO)
                    .totalAmount(new BigDecimal("1000.00"))
                    .build();
            var order2 = Order.builder()
                    .status(EOrderStatus.COMPLETADO)
                    .totalAmount(new BigDecimal("500.00"))
                    .build();
            var order3 = Order.builder()
                    .status(EOrderStatus.CANCELADO)
                    .totalAmount(new BigDecimal("200.00"))
                    .build();
            var order4 = Order.builder()
                    .status(EOrderStatus.PENDIENTE)
                    .totalAmount(new BigDecimal("300.00"))
                    .build();

            given(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(List.of(order1, order2, order3, order4));

            Map<String, Object> result = reportService.getSalesReport(START_DATE, END_DATE);

            assertThat(result)
                    .hasSize(6)
                    .containsEntry("startDate", START_DATE)
                    .containsEntry("endDate", END_DATE)
                    .containsEntry("totalOrders", 4L)
                    .containsEntry("completedOrders", 2L)
                    .containsEntry("cancelledOrders", 1L)
                    .containsEntry("totalSales", new BigDecimal("1800.00"));
        }

        @Test
        @DisplayName("should return zeros when no orders found")
        void shouldReturnZeros_whenNoOrders() {
            given(orderRepository.findByCreatedAtBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
                    .willReturn(List.of());

            Map<String, Object> result = reportService.getSalesReport(START_DATE, END_DATE);

            assertThat(result)
                    .hasSize(6)
                    .containsEntry("startDate", START_DATE)
                    .containsEntry("endDate", END_DATE)
                    .containsEntry("totalOrders", 0L)
                    .containsEntry("completedOrders", 0L)
                    .containsEntry("cancelledOrders", 0L)
                    .containsEntry("totalSales", BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getInventoryReport")
    class GetInventoryReport {

        @Test
        @DisplayName("should return inventory metrics when items exist")
        void shouldReturnInventoryMetrics() {
            var groupedItems = List.of(
                    new WoodInventoryReportItem("Caoba", new BigDecimal("100"), "m3", new BigDecimal("50000")),
                    new WoodInventoryReportItem("Cedro", new BigDecimal("50"), "m3", new BigDecimal("25000"))
            );

            var lowStockItem = WoodInventory.builder()
                    .id(1L)
                    .woodType("Caoba")
                    .quantity(new BigDecimal("5"))
                    .unit("m3")
                    .unitPrice(new BigDecimal("500"))
                    .minimumStock(new BigDecimal("10"))
                    .build();

            given(woodInventoryRepository.findInventoryGroupedByType()).willReturn(groupedItems);
            given(woodInventoryRepository.findAll()).willReturn(List.of(lowStockItem));

            Map<String, Object> result = reportService.getInventoryReport();

            assertThat(result)
                    .hasSize(6)
                    .containsEntry("totalWoodTypes", 2)
                    .containsEntry("totalQuantity", new BigDecimal("150"))
                    .containsEntry("totalValue", new BigDecimal("75000"))
                    .containsEntry("lowStockCount", 1);

            @SuppressWarnings("unchecked")
            var details = (List<WoodInventoryReportItem>) result.get("details");
            assertThat(details).hasSize(2);

            @SuppressWarnings("unchecked")
            var lowStockItems = (List<Map<String, Object>>) result.get("lowStockItems");
            assertThat(lowStockItems).hasSize(1);
            assertThat(lowStockItems.get(0))
                    .containsEntry("id", 1L)
                    .containsEntry("woodType", "Caoba")
                    .containsEntry("quantity", new BigDecimal("5"))
                    .containsEntry("unit", "m3")
                    .containsEntry("minimumStock", new BigDecimal("10"));
        }

        @Test
        @DisplayName("should return zeros when inventory is empty")
        void shouldReturnZeros_whenEmptyInventory() {
            given(woodInventoryRepository.findInventoryGroupedByType()).willReturn(List.of());
            given(woodInventoryRepository.findAll()).willReturn(List.of());

            Map<String, Object> result = reportService.getInventoryReport();

            assertThat(result)
                    .hasSize(6)
                    .containsEntry("totalWoodTypes", 0)
                    .containsEntry("totalQuantity", BigDecimal.ZERO)
                    .containsEntry("totalValue", BigDecimal.ZERO)
                    .containsEntry("lowStockCount", 0);

            @SuppressWarnings("unchecked")
            var details = (List<WoodInventoryReportItem>) result.get("details");
            assertThat(details).isEmpty();

            @SuppressWarnings("unchecked")
            var lowStockItems = (List<Map<String, Object>>) result.get("lowStockItems");
            assertThat(lowStockItems).isEmpty();
        }
    }

    @Nested
    @DisplayName("getTopSellingFurniture")
    class GetTopSellingFurniture {

        @Test
        @DisplayName("should return top selling furniture when items exist")
        void shouldReturnTopSellingItems() {
            var items = List.of(
                    new TopSellingFurnitureItem(1L, "Mesa de Roble", 10L, new BigDecimal("5000")),
                    new TopSellingFurnitureItem(2L, "Silla de Cedro", 20L, new BigDecimal("3000"))
            );

            given(orderDetailRepository.findTopSellingFurniture()).willReturn(items);

            Map<String, Object> result = reportService.getTopSellingFurniture();

            assertThat(result)
                    .hasSize(4)
                    .containsEntry("totalItems", 2)
                    .containsEntry("totalSold", 30L)
                    .containsEntry("totalRevenue", new BigDecimal("8000"));

            @SuppressWarnings("unchecked")
            var details = (List<TopSellingFurnitureItem>) result.get("details");
            assertThat(details).hasSize(2);
            assertThat(details.get(0).furnitureName()).isEqualTo("Mesa de Roble");
            assertThat(details.get(1).furnitureName()).isEqualTo("Silla de Cedro");
        }

        @Test
        @DisplayName("should return zeros when no sales data")
        void shouldReturnZeros_whenNoSalesData() {
            given(orderDetailRepository.findTopSellingFurniture()).willReturn(List.of());

            Map<String, Object> result = reportService.getTopSellingFurniture();

            assertThat(result)
                    .hasSize(4)
                    .containsEntry("totalItems", 0)
                    .containsEntry("totalSold", 0L)
                    .containsEntry("totalRevenue", BigDecimal.ZERO);

            @SuppressWarnings("unchecked")
            var details = (List<TopSellingFurnitureItem>) result.get("details");
            assertThat(details).isEmpty();
        }
    }

}
