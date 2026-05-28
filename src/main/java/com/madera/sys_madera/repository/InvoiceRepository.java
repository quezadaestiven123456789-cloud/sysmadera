package com.madera.sys_madera.repository;

import com.madera.sys_madera.model.EInvoiceStatus;
import com.madera.sys_madera.model.Invoice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    Optional<Invoice> findByOrderId(Long orderId);

    Page<Invoice> findByStatus(EInvoiceStatus status, Pageable pageable);

    List<Invoice> findByStatusAndDueDateBefore(EInvoiceStatus status, LocalDate date);

    List<Invoice> findByIssueDateBetween(LocalDate start, LocalDate end);

    long countByStatus(EInvoiceStatus status);

}
