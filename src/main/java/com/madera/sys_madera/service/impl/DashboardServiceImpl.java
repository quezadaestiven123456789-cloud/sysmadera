package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.model.EInvoiceStatus;
import com.madera.sys_madera.model.EOrderStatus;
import com.madera.sys_madera.repository.*;
import com.madera.sys_madera.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final ClientRepository clientRepository;
    private final OrderRepository orderRepository;
    private final InvoiceRepository invoiceRepository;
    private final FurnitureRepository furnitureRepository;
    private final WoodInventoryRepository woodInventoryRepository;

    @Override
    public Map<String, Object> getAdminDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        dashboard.put("totalClients", clientRepository.count());
        dashboard.put("totalOrders", orderRepository.count());
        dashboard.put("totalFurniture", furnitureRepository.count());
        dashboard.put("totalWoodTypes", woodInventoryRepository.count());

        dashboard.put("pendingOrders", orderRepository.countByStatus(EOrderStatus.PENDIENTE));
        dashboard.put("inProductionOrders", orderRepository.countByStatus(EOrderStatus.EN_PRODUCCION));
        dashboard.put("completedOrders", orderRepository.countByStatus(EOrderStatus.COMPLETADO));
        dashboard.put("deliveredOrders", orderRepository.countByStatus(EOrderStatus.ENTREGADO));
        dashboard.put("cancelledOrders", orderRepository.countByStatus(EOrderStatus.CANCELADO));

        dashboard.put("paidInvoices", invoiceRepository.countByStatus(EInvoiceStatus.PAGADA));
        dashboard.put("pendingInvoices", invoiceRepository.countByStatus(EInvoiceStatus.PENDIENTE));
        dashboard.put("overdueInvoices", invoiceRepository.countByStatus(EInvoiceStatus.VENCIDA));

        return dashboard;
    }

    @Override
    public Map<String, Object> getEmployeeDashboard() {
        Map<String, Object> dashboard = new LinkedHashMap<>();

        dashboard.put("totalOrders", orderRepository.count());
        dashboard.put("pendingOrders", orderRepository.countByStatus(EOrderStatus.PENDIENTE));
        dashboard.put("inProductionOrders", orderRepository.countByStatus(EOrderStatus.EN_PRODUCCION));
        dashboard.put("totalFurniture", furnitureRepository.count());

        return dashboard;
    }

}
