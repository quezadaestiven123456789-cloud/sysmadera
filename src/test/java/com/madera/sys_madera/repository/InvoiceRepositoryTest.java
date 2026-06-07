package com.madera.sys_madera.repository;

import com.madera.sys_madera.config.JpaConfig;
import com.madera.sys_madera.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("InvoiceRepository")
class InvoiceRepositoryTest {

    @Autowired
    private InvoiceRepository invoiceRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    private Client client;
    private Order order1;
    private Order order2;
    private Invoice invoicePagada;
    private Invoice invoicePendiente;

    @BeforeEach
    void setUp() {
        invoiceRepository.deleteAll();
        orderRepository.deleteAll();
        clientRepository.deleteAll();

        client = Client.builder()
                .name("Carlos López")
                .email("carlos@example.com")
                .phone("555-5678")
                .build();
        client = clientRepository.save(client);

        order1 = Order.builder()
                .orderNumber("ORD-001")
                .client(client)
                .totalAmount(new BigDecimal("500.00"))
                .status(EOrderStatus.COMPLETADO)
                .build();
        order1 = orderRepository.save(order1);

        order2 = Order.builder()
                .orderNumber("ORD-002")
                .client(client)
                .totalAmount(new BigDecimal("300.00"))
                .status(EOrderStatus.COMPLETADO)
                .build();
        order2 = orderRepository.save(order2);

        invoicePagada = Invoice.builder()
                .invoiceNumber("INV-001")
                .order(order1)
                .issueDate(LocalDate.of(2025, 6, 1))
                .dueDate(LocalDate.of(2025, 7, 1))
                .totalAmount(new BigDecimal("500.00"))
                .paidAmount(new BigDecimal("500.00"))
                .status(EInvoiceStatus.PAGADA)
                .notes("Pagada en tiempo")
                .build();

        invoicePendiente = Invoice.builder()
                .invoiceNumber("INV-002")
                .order(order2)
                .issueDate(LocalDate.of(2025, 6, 15))
                .dueDate(LocalDate.of(2025, 7, 15))
                .totalAmount(new BigDecimal("300.00"))
                .paidAmount(BigDecimal.ZERO)
                .status(EInvoiceStatus.PENDIENTE)
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist invoice with generated id and timestamps")
        void shouldSaveInvoice() {
            Invoice saved = invoiceRepository.save(invoicePagada);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getInvoiceNumber()).isEqualTo("INV-001");
            assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(saved.getStatus()).isEqualTo(EInvoiceStatus.PAGADA);
            assertThat(saved.getOrder().getId()).isEqualTo(order1.getId());
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return invoice when exists")
        void shouldFindById() {
            Invoice saved = invoiceRepository.save(invoicePagada);

            Optional<Invoice> found = invoiceRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getInvoiceNumber()).isEqualTo("INV-001");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty_whenNotFound() {
            Optional<Invoice> found = invoiceRepository.findById(999L);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByInvoiceNumber")
    class FindByInvoiceNumber {

        @Test
        @DisplayName("should return invoice when invoice number exists")
        void shouldFindByInvoiceNumber() {
            invoiceRepository.save(invoicePagada);

            Optional<Invoice> found = invoiceRepository.findByInvoiceNumber("INV-001");

            assertThat(found).isPresent();
            assertThat(found.get().getTotalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("should return empty when invoice number does not exist")
        void shouldReturnEmpty_whenInvoiceNumberNotFound() {
            Optional<Invoice> found = invoiceRepository.findByInvoiceNumber("INV-999");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderId {

        @Test
        @DisplayName("should return invoice when order id exists")
        void shouldFindByOrderId() {
            invoiceRepository.save(invoicePagada);

            Optional<Invoice> found = invoiceRepository.findByOrderId(order1.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getInvoiceNumber()).isEqualTo("INV-001");
        }

        @Test
        @DisplayName("should return empty when order id does not have an invoice")
        void shouldReturnEmpty_whenOrderIdNotFound() {
            Optional<Invoice> found = invoiceRepository.findByOrderId(999L);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatus")
    class FindByStatus {

        @Test
        @DisplayName("should return invoices filtered by status")
        void shouldFindByStatus() {
            invoiceRepository.save(invoicePagada);
            invoiceRepository.save(invoicePendiente);

            Page<Invoice> result = invoiceRepository
                    .findByStatus(EInvoiceStatus.PENDIENTE, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(EInvoiceStatus.PENDIENTE);
        }

        @Test
        @DisplayName("should return empty page when no invoices match status")
        void shouldReturnEmpty_whenNoMatch() {
            invoiceRepository.save(invoicePagada);

            Page<Invoice> result = invoiceRepository
                    .findByStatus(EInvoiceStatus.CANCELADA, PageRequest.of(0, 10));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatusAndDueDateBefore")
    class FindByStatusAndDueDateBefore {

        @Test
        @DisplayName("should return overdue invoices by status")
        void shouldFindByStatusAndDueDateBefore() {
            invoiceRepository.save(invoicePendiente);
            invoiceRepository.save(invoicePagada);

            List<Invoice> result = invoiceRepository.findByStatusAndDueDateBefore(
                    EInvoiceStatus.PENDIENTE, LocalDate.of(2025, 8, 1));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getInvoiceNumber()).isEqualTo("INV-002");
        }

        @Test
        @DisplayName("should return empty when no invoices match")
        void shouldReturnEmpty_whenNoMatch() {
            invoiceRepository.save(invoicePagada);

            List<Invoice> result = invoiceRepository.findByStatusAndDueDateBefore(
                    EInvoiceStatus.PENDIENTE, LocalDate.of(2025, 8, 1));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByIssueDateBetween")
    class FindByIssueDateBetween {

        @Test
        @DisplayName("should return invoices issued within date range")
        void shouldFindByIssueDateBetween() {
            invoiceRepository.save(invoicePagada);
            invoiceRepository.save(invoicePendiente);

            List<Invoice> result = invoiceRepository.findByIssueDateBetween(
                    LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 10));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getInvoiceNumber()).isEqualTo("INV-001");
        }

        @Test
        @DisplayName("should return empty when no invoices in range")
        void shouldReturnEmpty_whenNoInvoicesInRange() {
            invoiceRepository.save(invoicePagada);

            List<Invoice> result = invoiceRepository.findByIssueDateBetween(
                    LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByStatus")
    class CountByStatus {

        @Test
        @DisplayName("should count invoices by status")
        void shouldCountByStatus() {
            invoiceRepository.save(invoicePagada);
            invoiceRepository.save(invoicePendiente);

            long count = invoiceRepository.countByStatus(EInvoiceStatus.PAGADA);

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("should return zero when no invoices with given status")
        void shouldReturnZero_whenNoInvoices() {
            long count = invoiceRepository.countByStatus(EInvoiceStatus.VENCIDA);

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should remove invoice from database")
        void shouldDeleteInvoice() {
            Invoice saved = invoiceRepository.save(invoicePagada);

            invoiceRepository.deleteById(saved.getId());

            assertThat(invoiceRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should reflect changes after save with same id")
        void shouldUpdateInvoice() {
            Invoice saved = invoiceRepository.save(invoicePendiente);

            saved.setStatus(EInvoiceStatus.PAGADA);
            saved.setPaidAmount(new BigDecimal("300.00"));
            invoiceRepository.save(saved);

            Invoice updated = invoiceRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(EInvoiceStatus.PAGADA);
            assertThat(updated.getPaidAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
        }
    }
}
