package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.InvoiceRequest;
import com.madera.sys_madera.dto.response.InvoiceResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.BadRequestException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.model.*;
import com.madera.sys_madera.repository.InvoiceRepository;
import com.madera.sys_madera.repository.OrderRepository;
import com.madera.sys_madera.service.InvoiceService;
import com.madera.sys_madera.service.SequenceGeneratorService;
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
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final OrderRepository orderRepository;
    private final SequenceGeneratorService sequenceGeneratorService;

    @Override
    @Transactional
    public InvoiceResponse create(InvoiceRequest request) {
        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new ResourceNotFoundException("Orden", "id", request.orderId()));

        if (invoiceRepository.findByOrderId(request.orderId()).isPresent()) {
            throw new BadRequestException("La orden ya tiene una factura asociada");
        }

        if (request.paidAmount() != null
                && request.paidAmount().compareTo(order.getTotalAmount()) > 0) {
            throw new BadRequestException("El monto pagado no puede exceder el total de la orden");
        }

        Invoice invoice = Invoice.builder()
                .invoiceNumber(generateInvoiceNumber())
                .issueDate(request.issueDate())
                .dueDate(request.dueDate())
                .totalAmount(order.getTotalAmount())
                .paidAmount(request.paidAmount() != null ? request.paidAmount() : BigDecimal.ZERO)
                .status(determineStatus(request.paidAmount(), order.getTotalAmount()))
                .notes(request.notes())
                .order(order)
                .build();

        invoice = invoiceRepository.save(invoice);
        return toResponse(invoice);
    }

    @Override
    public InvoiceResponse findById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", id));
        return toResponse(invoice);
    }

    @Override
    public InvoiceResponse findByOrderId(Long orderId) {
        Invoice invoice = invoiceRepository.findByOrderId(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "orderId", orderId));
        return toResponse(invoice);
    }

    @Override
    public PagedResponse<InvoiceResponse> findAll(int page, int size, String sort, String direction, String status) {
        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<Invoice> invoices;
        if (status != null && !status.isEmpty()) {
            EInvoiceStatus invoiceStatus;
            try {
                invoiceStatus = EInvoiceStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                throw new BadRequestException("Estado inválido: " + status);
            }
            invoices = invoiceRepository.findByStatus(invoiceStatus, pageable);
        } else {
            invoices = invoiceRepository.findAll(pageable);
        }

        List<InvoiceResponse> content = invoices.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                invoices.getNumber(),
                invoices.getSize(),
                invoices.getTotalElements(),
                invoices.getTotalPages(),
                invoices.isLast()
        );
    }

    @Override
    @Transactional
    public InvoiceResponse registerPayment(Long id, BigDecimal amount) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Factura", "id", id));

        if (invoice.getStatus() == EInvoiceStatus.PAGADA) {
            throw new BadRequestException("La factura ya está completamente pagada");
        }

        BigDecimal newPaidAmount = invoice.getPaidAmount().add(amount);
        if (newPaidAmount.compareTo(invoice.getTotalAmount()) > 0) {
            throw new BadRequestException("El pago excede el saldo pendiente");
        }

        invoice.setPaidAmount(newPaidAmount);
        invoice.setStatus(determineStatus(newPaidAmount, invoice.getTotalAmount()));

        invoice = invoiceRepository.save(invoice);
        return toResponse(invoice);
    }

    private EInvoiceStatus determineStatus(BigDecimal paidAmount, BigDecimal totalAmount) {
        if (paidAmount == null || paidAmount.compareTo(BigDecimal.ZERO) == 0) {
            return EInvoiceStatus.PENDIENTE;
        } else if (paidAmount.compareTo(totalAmount) >= 0) {
            return EInvoiceStatus.PAGADA;
        } else {
            return EInvoiceStatus.PAGADA_PARCIAL;
        }
    }

    private String generateInvoiceNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        long nextVal = sequenceGeneratorService.nextValue("INVOICE_SEQ");
        return "FAC-" + datePart + "-" + String.format("%04d", nextVal);
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getInvoiceNumber(),
                invoice.getIssueDate(),
                invoice.getDueDate(),
                invoice.getTotalAmount(),
                invoice.getPaidAmount(),
                invoice.getTotalAmount().subtract(invoice.getPaidAmount()),
                invoice.getStatus().name(),
                invoice.getNotes(),
                invoice.getOrder().getId(),
                invoice.getOrder().getOrderNumber(),
                invoice.getOrder().getClient().getName(),
                invoice.getCreatedAt(),
                invoice.getUpdatedAt()
        );
    }

}
