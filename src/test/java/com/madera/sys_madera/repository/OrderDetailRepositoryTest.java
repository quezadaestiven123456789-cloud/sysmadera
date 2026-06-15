package com.madera.sys_madera.repository;

import com.madera.sys_madera.config.JpaConfig;
import com.madera.sys_madera.dto.response.TopSellingFurnitureItem;
import com.madera.sys_madera.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("OrderDetailRepository")
class OrderDetailRepositoryTest {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private FurnitureRepository furnitureRepository;

    private Client client;
    private Furniture mesaRoble;
    private Furniture sillaPino;
    private Furniture armarioCaoba;

    private void setUpData() {
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        clientRepository.deleteAll();
        furnitureRepository.deleteAll();

        client = Client.builder()
                .name("Juan Pérez")
                .email("juan@example.com")
                .phone("555-1234")
                .build();
        client = clientRepository.save(client);

        mesaRoble = Furniture.builder()
                .name("Mesa Roble")
                .price(new BigDecimal("250.00"))
                .stockQuantity(10)
                .category("Mesas")
                .active(true)
                .build();
        mesaRoble = furnitureRepository.save(mesaRoble);

        sillaPino = Furniture.builder()
                .name("Silla Pino")
                .price(new BigDecimal("85.00"))
                .stockQuantity(25)
                .category("Sillas")
                .active(true)
                .build();
        sillaPino = furnitureRepository.save(sillaPino);

        armarioCaoba = Furniture.builder()
                .name("Armario Caoba")
                .price(new BigDecimal("1200.00"))
                .stockQuantity(5)
                .category("Armarios")
                .active(true)
                .build();
        armarioCaoba = furnitureRepository.save(armarioCaoba);

        var detail1 = OrderDetail.builder()
                .furniture(mesaRoble)
                .quantity(3)
                .unitPrice(new BigDecimal("250.00"))
                .subtotal(new BigDecimal("750.00"))
                .build();

        var detail2 = OrderDetail.builder()
                .furniture(sillaPino)
                .quantity(5)
                .unitPrice(new BigDecimal("85.00"))
                .subtotal(new BigDecimal("425.00"))
                .build();

        var order1 = Order.builder()
                .orderNumber("ORD-001")
                .client(client)
                .totalAmount(new BigDecimal("1175.00"))
                .build();
        order1.setOrderDetails(new ArrayList<>(List.of(detail1, detail2)));
        detail1.setOrder(order1);
        detail2.setOrder(order1);
        orderRepository.save(order1);

        var detail3 = OrderDetail.builder()
                .furniture(sillaPino)
                .quantity(2)
                .unitPrice(new BigDecimal("85.00"))
                .subtotal(new BigDecimal("170.00"))
                .build();

        var detail4 = OrderDetail.builder()
                .furniture(armarioCaoba)
                .quantity(1)
                .unitPrice(new BigDecimal("1200.00"))
                .subtotal(new BigDecimal("1200.00"))
                .build();

        var order2 = Order.builder()
                .orderNumber("ORD-002")
                .client(client)
                .totalAmount(new BigDecimal("1370.00"))
                .build();
        order2.setOrderDetails(new ArrayList<>(List.of(detail3, detail4)));
        detail3.setOrder(order2);
        detail4.setOrder(order2);
        orderRepository.save(order2);
    }

    @Nested
    @DisplayName("findByOrderId")
    class FindByOrderId {

        @BeforeEach
        void setUp() {
            setUpData();
        }

        @Test
        @DisplayName("should return order details for given order")
        void shouldFindByOrderId() {
            Order order = orderRepository.findByOrderNumber("ORD-001").orElseThrow();
            List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());

            assertThat(details).hasSize(2);
            assertThat(details).extracting(d -> d.getFurniture().getName())
                    .containsExactlyInAnyOrder("Mesa Roble", "Silla Pino");
            assertThat(details).extracting(OrderDetail::getQuantity)
                    .containsExactlyInAnyOrder(3, 5);
        }

        @Test
        @DisplayName("should return empty list when order has no details")
        void shouldReturnEmpty_whenNoDetails() {
            List<OrderDetail> details = orderDetailRepository.findByOrderId(999L);

            assertThat(details).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByFurnitureId")
    class FindByFurnitureId {

        @BeforeEach
        void setUp() {
            setUpData();
        }

        @Test
        @DisplayName("should return order details for given furniture")
        void shouldFindByFurnitureId() {
            List<OrderDetail> details = orderDetailRepository.findByFurnitureId(sillaPino.getId());

            assertThat(details).hasSize(2);
            assertThat(details).extracting(d -> d.getOrder().getOrderNumber())
                    .containsExactlyInAnyOrder("ORD-001", "ORD-002");
            assertThat(details).extracting(OrderDetail::getQuantity)
                    .containsExactlyInAnyOrder(5, 2);
        }

        @Test
        @DisplayName("should return empty list when furniture has no details")
        void shouldReturnEmpty_whenNoDetails() {
            List<OrderDetail> details = orderDetailRepository.findByFurnitureId(999L);

            assertThat(details).isEmpty();
        }
    }

    @Nested
    @DisplayName("findTopSellingFurniture")
    class FindTopSellingFurniture {

        @Test
        @DisplayName("should return top selling furniture ordered by total quantity desc")
        void shouldReturnTopSellingFurniture() {
            setUpData();

            List<TopSellingFurnitureItem> top = orderDetailRepository.findTopSellingFurniture();

            assertThat(top).hasSize(3);

            assertThat(top.get(0).furnitureName()).isEqualTo("Silla Pino");
            assertThat(top.get(0).totalSold()).isEqualTo(7);
            assertThat(top.get(0).totalRevenue()).isEqualByComparingTo(new BigDecimal("595.00"));

            assertThat(top.get(1).furnitureName()).isEqualTo("Mesa Roble");
            assertThat(top.get(1).totalSold()).isEqualTo(3);
            assertThat(top.get(1).totalRevenue()).isEqualByComparingTo(new BigDecimal("750.00"));

            assertThat(top.get(2).furnitureName()).isEqualTo("Armario Caoba");
            assertThat(top.get(2).totalSold()).isEqualTo(1);
            assertThat(top.get(2).totalRevenue()).isEqualByComparingTo(new BigDecimal("1200.00"));
        }

        @Test
        @DisplayName("should return empty list when no order details exist")
        void shouldReturnEmpty_whenNoDetails() {
            List<TopSellingFurnitureItem> top = orderDetailRepository.findTopSellingFurniture();

            assertThat(top).isEmpty();
        }
    }
}
