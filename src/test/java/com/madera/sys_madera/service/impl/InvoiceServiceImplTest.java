package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.InvoiceRequest;
import com.madera.sys_madera.dto.response.InvoiceResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.BadRequestException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.model.*;
import com.madera.sys_madera.repository.InvoiceRepository;
import com.madera.sys_madera.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("InvoiceServiceImpl")
class InvoiceServiceImplTest {

    @Mock
    private InvoiceRepository invoiceRepository;

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private InvoiceServiceImpl invoiceService;

    private static final Long INVOICE_ID = 1L;
    private static final Long ORDER_ID = 1L;
    private static final Long CLIENT_ID = 1L;
    private static final String ORDER_NUMBER = "ORD-20250601-0001";
    private static final String CLIENT_NAME = "Juan Pérez";
    private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("1500.00");
    private static final BigDecimal PAID_AMOUNT = new BigDecimal("500.00");
    private static final LocalDate ISSUE_DATE = LocalDate.of(2025, 6, 1);
    private static final LocalDate DUE_DATE = LocalDate.of(2025, 7, 1);

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create invoice with PENDIENTE status when no payment")
        void shouldCreateInvoice_whenNoPayment() {
            var request = new InvoiceRequest(ORDER_ID, ISSUE_DATE, DUE_DATE, null, "Nota");
            var order = buildOrder();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(invoiceRepository.findByOrderId(ORDER_ID)).willReturn(Optional.empty());
            given(invoiceRepository.count()).willReturn(0L);
            given(invoiceRepository.save(any(Invoice.class))).willAnswer(i -> i.getArgument(0));

            InvoiceResponse response = invoiceService.create(request);

            assertThat(response.orderId()).isEqualTo(ORDER_ID);
            assertThat(response.totalAmount()).isEqualByComparingTo(TOTAL_AMOUNT);
            assertThat(response.paidAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.status()).isEqualTo("PENDIENTE");
            assertThat(response.balance()).isEqualByComparingTo(TOTAL_AMOUNT);

            ArgumentCaptor<Invoice> captor = ArgumentCaptor.forClass(Invoice.class);
            verify(invoiceRepository).save(captor.capture());
            assertThat(captor.getValue().getStatus()).isEqualTo(EInvoiceStatus.PENDIENTE);
        }

        @Test
        @DisplayName("should create invoice with PAGADA_PARCIAL status when partial payment")
        void shouldCreateInvoice_whenPartialPayment() {
            var request = new InvoiceRequest(ORDER_ID, ISSUE_DATE, DUE_DATE, PAID_AMOUNT, null);
            var order = buildOrder();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(invoiceRepository.findByOrderId(ORDER_ID)).willReturn(Optional.empty());
            given(invoiceRepository.count()).willReturn(0L);
            given(invoiceRepository.save(any(Invoice.class))).willAnswer(i -> i.getArgument(0));

            InvoiceResponse response = invoiceService.create(request);

            assertThat(response.status()).isEqualTo("PAGADA_PARCIAL");
            assertThat(response.paidAmount()).isEqualByComparingTo(PAID_AMOUNT);
            assertThat(response.balance()).isEqualByComparingTo(new BigDecimal("1000.00"));
        }

        @Test
        @DisplayName("should create invoice with PAGADA status when full payment")
        void shouldCreateInvoice_whenFullPayment() {
            var request = new InvoiceRequest(ORDER_ID, ISSUE_DATE, DUE_DATE, TOTAL_AMOUNT, null);
            var order = buildOrder();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(invoiceRepository.findByOrderId(ORDER_ID)).willReturn(Optional.empty());
            given(invoiceRepository.count()).willReturn(0L);
            given(invoiceRepository.save(any(Invoice.class))).willAnswer(i -> i.getArgument(0));

            InvoiceResponse response = invoiceService.create(request);

            assertThat(response.status()).isEqualTo("PAGADA");
            assertThat(response.paidAmount()).isEqualByComparingTo(TOTAL_AMOUNT);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when order not found")
        void shouldThrowException_whenOrderNotFound() {
            var request = new InvoiceRequest(ORDER_ID, ISSUE_DATE, DUE_DATE, null, null);

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Orden");
        }

        @Test
        @DisplayName("should throw BadRequestException when order already has invoice")
        void shouldThrowException_whenOrderAlreadyHasInvoice() {
            var request = new InvoiceRequest(ORDER_ID, ISSUE_DATE, DUE_DATE, null, null);
            var order = buildOrder();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(invoiceRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(new Invoice()));

            assertThatThrownBy(() -> invoiceService.create(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("ya tiene una factura");
        }

        @Test
        @DisplayName("should throw BadRequestException when paid amount exceeds total")
        void shouldThrowException_whenPaidExceedsTotal() {
            var request = new InvoiceRequest(ORDER_ID, ISSUE_DATE, DUE_DATE, new BigDecimal("9999.99"), null);
            var order = buildOrder();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(invoiceRepository.findByOrderId(ORDER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.create(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("exceder");
        }

        @ParameterizedTest
        @CsvSource({
            "-200.00, PAGADA_PARCIAL",
            "0, PENDIENTE"
        })
        @DisplayName("should create with edge values for paidAmount")
        void shouldCreateWithEdgePaidAmount(String paidAmountStr, String expectedStatus) {
            BigDecimal paidAmount = new BigDecimal(paidAmountStr);
            var request = new InvoiceRequest(ORDER_ID, ISSUE_DATE, DUE_DATE, paidAmount, null);
            var order = buildOrder();

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(invoiceRepository.findByOrderId(ORDER_ID)).willReturn(Optional.empty());
            given(invoiceRepository.count()).willReturn(0L);
            given(invoiceRepository.save(any(Invoice.class))).willAnswer(i -> i.getArgument(0));

            InvoiceResponse response = invoiceService.create(request);

            assertThat(response.paidAmount()).isEqualByComparingTo(paidAmount);
            assertThat(response.status()).isEqualTo(expectedStatus);
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return invoice when exists")
        void shouldReturn_whenExists() {
            var invoice = buildInvoice(EInvoiceStatus.PENDIENTE, BigDecimal.ZERO);

            given(invoiceRepository.findById(INVOICE_ID)).willReturn(Optional.of(invoice));

            InvoiceResponse response = invoiceService.findById(INVOICE_ID);

            assertThat(response.id()).isEqualTo(INVOICE_ID);
            assertThat(response.invoiceNumber()).isEqualTo("FAC-20250601-0001");
            assertThat(response.clientName()).isEqualTo(CLIENT_NAME);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowException_whenNotFound() {
            given(invoiceRepository.findById(INVOICE_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.findById(INVOICE_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Factura");
        }
    }

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderId {

        @Test
        @DisplayName("should return invoice when order exists")
        void shouldReturn_whenOrderExists() {
            var invoice = buildInvoice(EInvoiceStatus.PENDIENTE, BigDecimal.ZERO);

            given(invoiceRepository.findByOrderId(ORDER_ID)).willReturn(Optional.of(invoice));

            InvoiceResponse response = invoiceService.findByOrderId(ORDER_ID);

            assertThat(response.orderId()).isEqualTo(ORDER_ID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when no invoice for order")
        void shouldThrowException_whenNotFound() {
            given(invoiceRepository.findByOrderId(ORDER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.findByOrderId(ORDER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Factura");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all invoices when no status filter")
        void shouldReturnAll_whenNoFilter() {
            var invoice = buildInvoice(EInvoiceStatus.PENDIENTE, BigDecimal.ZERO);
            var page = new PageImpl<>(List.of(invoice));

            given(invoiceRepository.findAll(any(Pageable.class))).willReturn(page);

            PagedResponse<InvoiceResponse> response = invoiceService.findAll(0, 10, "id", "asc", null);

            assertThat(response.content()).hasSize(1);
        }

        @Test
        @DisplayName("should filter by status")
        void shouldFilterByStatus() {
            var invoice = buildInvoice(EInvoiceStatus.PAGADA, TOTAL_AMOUNT);
            var page = new PageImpl<>(List.of(invoice));

            given(invoiceRepository.findByStatus(any(EInvoiceStatus.class), any(Pageable.class))).willReturn(page);

            PagedResponse<InvoiceResponse> response = invoiceService.findAll(0, 10, "id", "asc", "PAGADA");

            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).status()).isEqualTo("PAGADA");
        }

        @Test
        @DisplayName("should throw BadRequestException when status is invalid")
        void shouldThrowException_whenStatusInvalid() {
            assertThatThrownBy(() -> invoiceService.findAll(0, 10, "id", "asc", "INVALIDO"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Estado inválido");
        }
    }

    @Nested
    @DisplayName("registerPayment")
    class RegisterPayment {

        @Test
        @DisplayName("should register partial payment")
        void shouldRegisterPartialPayment() {
            var invoice = buildInvoice(EInvoiceStatus.PENDIENTE, BigDecimal.ZERO);

            given(invoiceRepository.findById(INVOICE_ID)).willReturn(Optional.of(invoice));
            given(invoiceRepository.save(any(Invoice.class))).willAnswer(i -> i.getArgument(0));

            InvoiceResponse response = invoiceService.registerPayment(INVOICE_ID, PAID_AMOUNT);

            assertThat(response.status()).isEqualTo("PAGADA_PARCIAL");
            assertThat(response.paidAmount()).isEqualByComparingTo(PAID_AMOUNT);
        }

        @Test
        @DisplayName("should mark as PAGADA when payment completes total")
        void shouldMarkAsPaid_whenPaymentCompletes() {
            var invoice = buildInvoice(EInvoiceStatus.PAGADA_PARCIAL, new BigDecimal("1000.00"));

            given(invoiceRepository.findById(INVOICE_ID)).willReturn(Optional.of(invoice));
            given(invoiceRepository.save(any(Invoice.class))).willAnswer(i -> i.getArgument(0));

            InvoiceResponse response = invoiceService.registerPayment(INVOICE_ID, new BigDecimal("500.00"));

            assertThat(response.status()).isEqualTo("PAGADA");
            assertThat(response.paidAmount()).isEqualByComparingTo(TOTAL_AMOUNT);
        }

        @Test
        @DisplayName("should throw BadRequestException when invoice already paid")
        void shouldThrowException_whenAlreadyPaid() {
            var invoice = buildInvoice(EInvoiceStatus.PAGADA, TOTAL_AMOUNT);

            given(invoiceRepository.findById(INVOICE_ID)).willReturn(Optional.of(invoice));

            assertThatThrownBy(() -> invoiceService.registerPayment(INVOICE_ID, PAID_AMOUNT))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("ya está completamente pagada");
        }

        @Test
        @DisplayName("should throw BadRequestException when payment exceeds balance")
        void shouldThrowException_whenPaymentExceedsBalance() {
            var invoice = buildInvoice(EInvoiceStatus.PENDIENTE, new BigDecimal("1400.00"));

            given(invoiceRepository.findById(INVOICE_ID)).willReturn(Optional.of(invoice));

            assertThatThrownBy(() -> invoiceService.registerPayment(INVOICE_ID, new BigDecimal("200.00")))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("excede");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when invoice not found")
        void shouldThrowException_whenInvoiceNotFound() {
            given(invoiceRepository.findById(INVOICE_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> invoiceService.registerPayment(INVOICE_ID, PAID_AMOUNT))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Factura");
        }

        @Test
        @DisplayName("should register zero payment")
        void shouldRegisterZeroPayment() {
            var invoice = buildInvoice(EInvoiceStatus.PENDIENTE, BigDecimal.ZERO);

            given(invoiceRepository.findById(INVOICE_ID)).willReturn(Optional.of(invoice));
            given(invoiceRepository.save(any(Invoice.class))).willAnswer(i -> i.getArgument(0));

            InvoiceResponse response = invoiceService.registerPayment(INVOICE_ID, BigDecimal.ZERO);

            assertThat(response.paidAmount()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(response.status()).isEqualTo("PENDIENTE");
        }

        @Test
        @DisplayName("should register negative payment")
        void shouldRegisterNegativePayment() {
            var invoice = buildInvoice(EInvoiceStatus.PAGADA_PARCIAL, new BigDecimal("500.00"));

            given(invoiceRepository.findById(INVOICE_ID)).willReturn(Optional.of(invoice));
            given(invoiceRepository.save(any(Invoice.class))).willAnswer(i -> i.getArgument(0));

            InvoiceResponse response = invoiceService.registerPayment(INVOICE_ID, new BigDecimal("-200.00"));

            assertThat(response.paidAmount()).isEqualByComparingTo(new BigDecimal("300.00"));
            assertThat(response.status()).isEqualTo("PAGADA_PARCIAL");
        }
    }

    private Order buildOrder() {
        var client = new Client();
        client.setId(CLIENT_ID);
        client.setName(CLIENT_NAME);

        var order = new Order();
        order.setId(ORDER_ID);
        order.setOrderNumber(ORDER_NUMBER);
        order.setTotalAmount(TOTAL_AMOUNT);
        order.setClient(client);
        order.setStatus(EOrderStatus.PENDIENTE);
        return order;
    }

    private Invoice buildInvoice(EInvoiceStatus status, BigDecimal paidAmount) {
        var order = buildOrder();

        return Invoice.builder()
                .id(INVOICE_ID)
                .invoiceNumber("FAC-20250601-0001")
                .issueDate(ISSUE_DATE)
                .dueDate(DUE_DATE)
                .totalAmount(TOTAL_AMOUNT)
                .paidAmount(paidAmount)
                .status(status)
                .notes("Nota")
                .order(order)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
