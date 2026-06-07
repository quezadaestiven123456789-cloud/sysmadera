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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("OrderRepository")
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private FurnitureRepository furnitureRepository;

    private Client client;
    private Furniture furniture;
    private Order order;

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll();
        clientRepository.deleteAll();
        furnitureRepository.deleteAll();

        client = Client.builder()
                .name("Juan Pérez")
                .email("juan@example.com")
                .phone("555-1234")
                .build();
        client = clientRepository.save(client);

        furniture = Furniture.builder()
                .name("Mesa Roble")
                .price(new BigDecimal("250.00"))
                .stockQuantity(10)
                .build();
        furniture = furnitureRepository.save(furniture);

        var detail = OrderDetail.builder()
                .furniture(furniture)
                .quantity(2)
                .unitPrice(new BigDecimal("250.00"))
                .subtotal(new BigDecimal("500.00"))
                .build();

        order = Order.builder()
                .orderNumber("ORD-20250601-0001")
                .client(client)
                .totalAmount(new BigDecimal("500.00"))
                .notes("Pedido urgente")
                .build();

        order.setOrderDetails(new ArrayList<>(List.of(detail)));
        detail.setOrder(order);
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist order with generated id, timestamps and details")
        void shouldSaveOrder() {
            Order saved = orderRepository.save(order);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getOrderNumber()).isEqualTo("ORD-20250601-0001");
            assertThat(saved.getClient().getId()).isEqualTo(client.getId());
            assertThat(saved.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(saved.getStatus()).isEqualTo(EOrderStatus.PENDIENTE);
            assertThat(saved.getNotes()).isEqualTo("Pedido urgente");
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
            assertThat(saved.getOrderDetails()).hasSize(1);
            assertThat(saved.getOrderDetails().get(0).getFurniture().getName()).isEqualTo("Mesa Roble");
            assertThat(saved.getOrderDetails().get(0).getQuantity()).isEqualTo(2);
            assertThat(saved.getOrderDetails().get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("500.00"));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return order when exists")
        void shouldFindById() {
            Order saved = orderRepository.save(order);

            Optional<Order> found = orderRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getOrderNumber()).isEqualTo("ORD-20250601-0001");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty_whenNotFound() {
            Optional<Order> found = orderRepository.findById(999L);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByOrderNumber")
    class FindByOrderNumber {

        @Test
        @DisplayName("should return order when order number exists")
        void shouldFindByOrderNumber() {
            orderRepository.save(order);

            Optional<Order> found = orderRepository.findByOrderNumber("ORD-20250601-0001");

            assertThat(found).isPresent();
            assertThat(found.get().getClient().getName()).isEqualTo("Juan Pérez");
        }

        @Test
        @DisplayName("should return empty when order number does not exist")
        void shouldReturnEmpty_whenOrderNumberNotFound() {
            Optional<Order> found = orderRepository.findByOrderNumber("NO-EXISTE");

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByClientId")
    class FindByClientId {

        @Test
        @DisplayName("should return orders for given client")
        void shouldFindByClientId() {
            orderRepository.save(order);

            Page<Order> result = orderRepository.findByClientId(client.getId(), PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getClient().getId()).isEqualTo(client.getId());
        }

        @Test
        @DisplayName("should return empty page when client has no orders")
        void shouldReturnEmpty_whenNoOrders() {
            Page<Order> result = orderRepository.findByClientId(999L, PageRequest.of(0, 10));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatus")
    class FindByStatus {

        @Test
        @DisplayName("should return orders filtered by status")
        void shouldFindByStatus() {
            orderRepository.save(order);

            Page<Order> result = orderRepository.findByStatus(EOrderStatus.PENDIENTE, PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getStatus()).isEqualTo(EOrderStatus.PENDIENTE);
        }

        @Test
        @DisplayName("should return empty page when no orders match status")
        void shouldReturnEmpty_whenNoMatch() {
            orderRepository.save(order);

            Page<Order> result = orderRepository.findByStatus(EOrderStatus.COMPLETADO, PageRequest.of(0, 10));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByCreatedAtBetween")
    class FindByCreatedAtBetween {

        @Test
        @DisplayName("should return orders within date range")
        void shouldFindByCreatedAtBetween() {
            orderRepository.save(order);

            LocalDateTime start = LocalDateTime.now().minusMinutes(5);
            LocalDateTime end = LocalDateTime.now().plusMinutes(5);

            List<Order> result = orderRepository.findByCreatedAtBetween(start, end);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getOrderNumber()).isEqualTo("ORD-20250601-0001");
        }

        @Test
        @DisplayName("should return empty list when no orders in range")
        void shouldReturnEmpty_whenNoOrdersInRange() {
            orderRepository.save(order);

            LocalDateTime start = LocalDateTime.now().plusDays(1);
            LocalDateTime end = LocalDateTime.now().plusDays(2);

            List<Order> result = orderRepository.findByCreatedAtBetween(start, end);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStatusAndCreatedAtBetween")
    class FindByStatusAndCreatedAtBetween {

        @Test
        @DisplayName("should return orders matching status and date range")
        void shouldFindByStatusAndCreatedAtBetween() {
            orderRepository.save(order);

            LocalDateTime start = LocalDateTime.now().minusMinutes(5);
            LocalDateTime end = LocalDateTime.now().plusMinutes(5);

            List<Order> result = orderRepository.findByStatusAndCreatedAtBetween(
                    EOrderStatus.PENDIENTE, start, end);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty when status does not match")
        void shouldReturnEmpty_whenStatusNotMatch() {
            orderRepository.save(order);

            LocalDateTime start = LocalDateTime.now().minusMinutes(5);
            LocalDateTime end = LocalDateTime.now().plusMinutes(5);

            List<Order> result = orderRepository.findByStatusAndCreatedAtBetween(
                    EOrderStatus.COMPLETADO, start, end);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("countByStatus")
    class CountByStatus {

        @Test
        @DisplayName("should count orders by status")
        void shouldCountByStatus() {
            orderRepository.save(order);

            long count = orderRepository.countByStatus(EOrderStatus.PENDIENTE);

            assertThat(count).isEqualTo(1);
        }

        @Test
        @DisplayName("should return zero when no orders with given status")
        void shouldReturnZero_whenNoOrders() {
            long count = orderRepository.countByStatus(EOrderStatus.COMPLETADO);

            assertThat(count).isZero();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should remove order from database with cascade to details")
        void shouldDeleteOrder() {
            Order saved = orderRepository.save(order);

            orderRepository.deleteById(saved.getId());

            assertThat(orderRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should reflect changes after save with same id")
        void shouldUpdateOrder() {
            Order saved = orderRepository.save(order);

            saved.setStatus(EOrderStatus.COMPLETADO);
            orderRepository.save(saved);

            Order updated = orderRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getStatus()).isEqualTo(EOrderStatus.COMPLETADO);
        }
    }

    @Nested
    @DisplayName("constraints")
    class Constraints {

        @Test
        @DisplayName("should enforce unique order_number")
        void shouldEnforceUniqueOrderNumber() {
            orderRepository.saveAndFlush(order);

            var duplicate = Order.builder()
                    .orderNumber("ORD-20250601-0001")
                    .client(client)
                    .totalAmount(new BigDecimal("100.00"))
                    .build();

            assertThatThrownBy(() -> orderRepository.saveAndFlush(duplicate))
                    .isInstanceOf(DataIntegrityViolationException.class);
        }
    }
}
