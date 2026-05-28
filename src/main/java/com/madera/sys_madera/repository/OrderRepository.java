package com.madera.sys_madera.repository;

import com.madera.sys_madera.model.EOrderStatus;
import com.madera.sys_madera.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findByOrderNumber(String orderNumber);

    Page<Order> findByClientId(Long clientId, Pageable pageable);

    Page<Order> findByStatus(EOrderStatus status, Pageable pageable);

    List<Order> findByStatusAndCreatedAtBetween(EOrderStatus status, LocalDateTime start, LocalDateTime end);

    List<Order> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);

    long countByStatus(EOrderStatus status);

}
