package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.OrderRequest;
import com.madera.sys_madera.dto.response.OrderResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.BadRequestException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.model.*;
import com.madera.sys_madera.repository.ClientRepository;
import com.madera.sys_madera.repository.FurnitureRepository;
import com.madera.sys_madera.repository.OrderRepository;
import com.madera.sys_madera.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ClientRepository clientRepository;
    private final FurnitureRepository furnitureRepository;

    @Override
    @Transactional
    public OrderResponse create(OrderRequest request) {
        Client client = clientRepository.findById(request.clientId())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", request.clientId()));

        if (request.details().isEmpty()) {
            throw new BadRequestException("La orden debe tener al menos un detalle");
        }

        Order order = Order.builder()
                .orderNumber(generateOrderNumber())
                .client(client)
                .notes(request.notes())
                .totalAmount(BigDecimal.ZERO)
                .build();

        List<OrderDetail> details = request.details().stream().map(detailRequest -> {
            Furniture furniture = furnitureRepository.findById(detailRequest.furnitureId())
                    .orElseThrow(() -> new ResourceNotFoundException("Mueble", "id", detailRequest.furnitureId()));

            if (furniture.getStockQuantity() < detailRequest.quantity()) {
                throw new BadRequestException(
                        "Stock insuficiente para '" + furniture.getName()
                                + "'. Disponible: " + furniture.getStockQuantity()
                                + ", solicitado: " + detailRequest.quantity());
            }

            furniture.setStockQuantity(furniture.getStockQuantity() - detailRequest.quantity());

            BigDecimal subtotal = furniture.getPrice()
                    .multiply(BigDecimal.valueOf(detailRequest.quantity()));

            return OrderDetail.builder()
                    .order(order)
                    .furniture(furniture)
                    .quantity(detailRequest.quantity())
                    .unitPrice(furniture.getPrice())
                    .subtotal(subtotal)
                    .build();
        }).toList();

        BigDecimal total = details.stream()
                .map(OrderDetail::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setTotalAmount(total);
        order.setOrderDetails(details);

        Order savedOrder = orderRepository.save(order);
        return toResponse(savedOrder);
    }

    @Override
    public OrderResponse findById(Long id) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", id));
        return toResponse(order);
    }

    @Override
    public PagedResponse<OrderResponse> findAll(int page, int size, String sort, String direction, String status) {
        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<Order> orders;
        if (status != null && !status.isEmpty()) {
            EOrderStatus orderStatus;
            try {
                orderStatus = EOrderStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Estado inválido: " + status);
            }
            orders = orderRepository.findByStatus(orderStatus, pageable);
        } else {
            orders = orderRepository.findAll(pageable);
        }

        return buildPagedResponse(orders);
    }

    @Override
    public PagedResponse<OrderResponse> findByClientId(Long clientId, int page, int size, String sort, String direction) {
        if (!clientRepository.existsById(clientId)) {
            throw new ResourceNotFoundException("Cliente", "id", clientId);
        }

        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<Order> orders = orderRepository.findByClientId(clientId, pageable);
        return buildPagedResponse(orders);
    }

    @Override
    @Transactional
    public OrderResponse updateStatus(Long id, String status) {
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", id));

        EOrderStatus newStatus;
        try {
            newStatus = EOrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Estado inválido: " + status);
        }

        order.setStatus(newStatus);
        order = orderRepository.save(order);
        return toResponse(order);
    }

    private String generateOrderNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long count = orderRepository.count() + 1;
        return "ORD-" + datePart + "-" + String.format("%04d", count);
    }

    private PagedResponse<OrderResponse> buildPagedResponse(Page<Order> orders) {
        List<OrderResponse> content = orders.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                orders.getNumber(),
                orders.getSize(),
                orders.getTotalElements(),
                orders.getTotalPages(),
                orders.isLast()
        );
    }

    private OrderResponse toResponse(Order order) {
        List<OrderResponse.OrderDetailResponse> details = order.getOrderDetails().stream()
                .map(d -> new OrderResponse.OrderDetailResponse(
                        d.getId(),
                        d.getFurniture().getId(),
                        d.getFurniture().getName(),
                        d.getQuantity(),
                        d.getUnitPrice(),
                        d.getSubtotal()
                ))
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getTotalAmount(),
                order.getNotes(),
                order.getClient().getName(),
                order.getClient().getId(),
                details,
                order.getCreatedAt(),
                order.getUpdatedAt()
        );
    }

}
