package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.model.EOrderStatus;
import com.madera.sys_madera.model.Order;
import com.madera.sys_madera.model.OrderDetail;
import com.madera.sys_madera.repository.OrderRepository;
import com.madera.sys_madera.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final OrderRepository orderRepository;

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
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("message", "Reporte de inventario - implementación completa pendiente");
        return report;
    }

    @Override
    public Map<String, Object> getTopSellingFurniture() {
        Map<String, Object> report = new LinkedHashMap<>();
        report.put("message", "Top muebles vendidos - implementación completa pendiente");
        return report;
    }

}
