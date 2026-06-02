package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.response.TopSellingFurnitureItem;
import com.madera.sys_madera.dto.response.WoodInventoryReportItem;
import com.madera.sys_madera.model.EOrderStatus;
import com.madera.sys_madera.model.Order;
import com.madera.sys_madera.model.WoodInventory;
import com.madera.sys_madera.repository.OrderDetailRepository;
import com.madera.sys_madera.repository.OrderRepository;
import com.madera.sys_madera.repository.WoodInventoryRepository;
import com.madera.sys_madera.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final WoodInventoryRepository woodInventoryRepository;

    @Override
    public Map<String, Object> getSalesReport(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        List<Order> orders = orderRepository.findByCreatedAtBetween(start, end);

        BigDecimal totalSales = orders.stream()
                .filter(o -> o.getStatus() != EOrderStatus.CANCELADO)
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalOrders = orders.size();
        long completedOrders = orders.stream()
                .filter(o -> o.getStatus() == EOrderStatus.COMPLETADO)
                .count();
        long cancelledOrders = orders.stream()
                .filter(o -> o.getStatus() == EOrderStatus.CANCELADO)
                .count();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("startDate", startDate);
        report.put("endDate", endDate);
        report.put("totalOrders", totalOrders);
        report.put("completedOrders", completedOrders);
        report.put("cancelledOrders", cancelledOrders);
        report.put("totalSales", totalSales);

        return report;
    }

    @Override
    public Map<String, Object> getInventoryReport() {
        List<WoodInventoryReportItem> groupedItems = woodInventoryRepository.findInventoryGroupedByType();

        List<WoodInventory> lowStockItems = woodInventoryRepository.findAll().stream()
                .filter(item -> item.getMinimumStock() != null
                        && item.getQuantity().compareTo(item.getMinimumStock()) < 0)
                .toList();

        BigDecimal totalQuantity = groupedItems.stream()
                .map(WoodInventoryReportItem::totalQuantity)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalValue = groupedItems.stream()
                .map(WoodInventoryReportItem::totalValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalWoodTypes", groupedItems.size());
        report.put("totalQuantity", totalQuantity);
        report.put("totalValue", totalValue);
        report.put("details", groupedItems);
        report.put("lowStockCount", lowStockItems.size());
        report.put("lowStockItems", lowStockItems.stream().map(item -> Map.of(
                "id", item.getId(),
                "woodType", item.getWoodType(),
                "quantity", item.getQuantity(),
                "unit", item.getUnit(),
                "minimumStock", item.getMinimumStock()
        )).toList());

        return report;
    }

    @Override
    public Map<String, Object> getTopSellingFurniture() {
        List<TopSellingFurnitureItem> items = orderDetailRepository.findTopSellingFurniture();

        long totalSold = items.stream()
                .mapToLong(TopSellingFurnitureItem::totalSold)
                .sum();

        BigDecimal totalRevenue = items.stream()
                .map(TopSellingFurnitureItem::totalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("totalItems", items.size());
        report.put("totalSold", totalSold);
        report.put("totalRevenue", totalRevenue);
        report.put("details", items);

        return report;
    }

}
