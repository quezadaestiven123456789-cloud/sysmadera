package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.OrderRequest;
import com.madera.sys_madera.dto.response.OrderResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.BadRequestException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.model.Client;
import com.madera.sys_madera.model.EOrderStatus;
import com.madera.sys_madera.model.Furniture;
import com.madera.sys_madera.model.Order;
import com.madera.sys_madera.model.OrderDetail;
import com.madera.sys_madera.repository.ClientRepository;
import com.madera.sys_madera.repository.FurnitureRepository;
import com.madera.sys_madera.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderServiceImpl")
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ClientRepository clientRepository;

    @Mock
    private FurnitureRepository furnitureRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private static final Long ORDER_ID = 1L;
    private static final Long CLIENT_ID = 1L;
    private static final Long FURNITURE_ID = 1L;
    private static final String CLIENT_NAME = "Juan Pérez";
    private static final String FURNITURE_NAME = "Mesa Roble";
    private static final BigDecimal FURNITURE_PRICE = new BigDecimal("250.00");
    private static final Integer FURNITURE_STOCK = 10;
    private static final int QUANTITY = 2;

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create and return order when request is valid")
        void shouldCreateOrder_whenValidRequest() {
            var request = buildRequest();
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(client));
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));
            given(orderRepository.count()).willReturn(0L);
            given(orderRepository.save(any(Order.class))).willReturn(order);

            OrderResponse response = orderService.create(request);

            assertThat(response.id()).isEqualTo(ORDER_ID);
            assertThat(response.clientId()).isEqualTo(CLIENT_ID);
            assertThat(response.clientName()).isEqualTo(CLIENT_NAME);
            assertThat(response.status()).isEqualTo("PENDIENTE");
            assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
            assertThat(response.details()).hasSize(1);
            assertThat(response.details().get(0).furnitureId()).isEqualTo(FURNITURE_ID);
            assertThat(response.details().get(0).furnitureName()).isEqualTo(FURNITURE_NAME);
            assertThat(response.details().get(0).quantity()).isEqualTo(QUANTITY);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            assertThat(captor.getValue().getClient().getId()).isEqualTo(CLIENT_ID);
            assertThat(captor.getValue().getOrderDetails()).hasSize(1);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when client not found")
        void shouldThrowException_whenClientNotFound() {
            var request = buildRequest();

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw BadRequestException when details are empty")
        void shouldThrowException_whenDetailsEmpty() {
            var request = new OrderRequest(CLIENT_ID, "Nota", List.of());

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(new Client()));

            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("al menos un detalle");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw BadRequestException when stock is insufficient")
        void shouldThrowException_whenInsufficientStock() {
            var request = new OrderRequest(CLIENT_ID, "Nota",
                    List.of(new OrderRequest.OrderDetailRequest(FURNITURE_ID, 99)));
            var client = buildClient();
            var furniture = buildFurniture();
            furniture.setStockQuantity(5);

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(client));
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));

            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Stock insuficiente");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when furniture not found")
        void shouldThrowException_whenFurnitureNotFound() {
            var request = buildRequest();
            var client = buildClient();

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(client));
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Mueble");

            verify(orderRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return order when order exists")
        void shouldReturnOrder_whenOrderExists() {
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));

            OrderResponse response = orderService.findById(ORDER_ID);

            assertThat(response.id()).isEqualTo(ORDER_ID);
            assertThat(response.clientId()).isEqualTo(CLIENT_ID);
            assertThat(response.clientName()).isEqualTo(CLIENT_NAME);
            assertThat(response.status()).isEqualTo("PENDIENTE");
            assertThat(response.totalAmount()).isEqualByComparingTo(new BigDecimal("500.00"));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when order not found")
        void shouldThrowException_whenOrderNotFound() {
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.findById(ORDER_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Orden")
                    .hasMessageContaining(String.valueOf(ORDER_ID));
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all orders when no status filter")
        void shouldReturnAllOrders_whenNoStatusFilter() {
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);
            var page = new PageImpl<>(List.of(order));

            given(orderRepository.findAll(any(Pageable.class))).willReturn(page);

            PagedResponse<OrderResponse> response = orderService.findAll(0, 10, "id", "asc", null);

            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).id()).isEqualTo(ORDER_ID);
            assertThat(response.totalElements()).isEqualTo(1);
        }

        @Test
        @DisplayName("should return orders filtered by status")
        void shouldReturnOrdersByStatus_whenStatusProvided() {
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);
            var page = new PageImpl<>(List.of(order));

            given(orderRepository.findByStatus(any(EOrderStatus.class), any(Pageable.class))).willReturn(page);

            PagedResponse<OrderResponse> response = orderService.findAll(0, 10, "id", "asc", "PENDIENTE");

            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).status()).isEqualTo("PENDIENTE");
        }

        @Test
        @DisplayName("should throw BadRequestException when status is invalid")
        void shouldThrowException_whenStatusInvalid() {
            assertThatThrownBy(() -> orderService.findAll(0, 10, "id", "asc", "INVALIDO"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Estado inválido");
        }
    }

    @Nested
    @DisplayName("findByClientId")
    class FindByClientId {

        @Test
        @DisplayName("should return orders for existing client")
        void shouldReturnOrders_whenClientExists() {
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);
            var page = new PageImpl<>(List.of(order));

            given(clientRepository.existsById(CLIENT_ID)).willReturn(true);
            given(orderRepository.findByClientId(any(Long.class), any(Pageable.class))).willReturn(page);

            PagedResponse<OrderResponse> response = orderService.findByClientId(CLIENT_ID, 0, 10, "id", "asc");

            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).clientId()).isEqualTo(CLIENT_ID);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when client not found")
        void shouldThrowException_whenClientNotFound() {
            given(clientRepository.existsById(CLIENT_ID)).willReturn(false);

            assertThatThrownBy(() -> orderService.findByClientId(CLIENT_ID, 0, 10, "id", "asc"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Cliente");
        }
    }

    @Nested
    @DisplayName("updateStatus")
    class UpdateStatus {

        @Test
        @DisplayName("should update order status")
        void shouldUpdateStatus() {
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(orderRepository.save(any(Order.class))).willReturn(order);

            OrderResponse response = orderService.updateStatus(ORDER_ID, "COMPLETADO");

            assertThat(response.status()).isEqualTo("COMPLETADO");
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("should throw BadRequestException when status is invalid")
        void shouldThrowException_whenStatusInvalid() {
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.updateStatus(ORDER_ID, "INVALIDO"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Estado inválido");
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when order not found")
        void shouldThrowException_whenOrderNotFound() {
            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.updateStatus(ORDER_ID, "COMPLETADO"))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Orden");
        }
    }

    private OrderRequest buildRequest() {
        return new OrderRequest(CLIENT_ID, "Nota del pedido",
                List.of(new OrderRequest.OrderDetailRequest(FURNITURE_ID, QUANTITY)));
    }

    private Client buildClient() {
        var client = new Client();
        client.setId(CLIENT_ID);
        client.setName(CLIENT_NAME);
        return client;
    }

    private Furniture buildFurniture() {
        var furniture = new Furniture();
        furniture.setId(FURNITURE_ID);
        furniture.setName(FURNITURE_NAME);
        furniture.setPrice(FURNITURE_PRICE);
        furniture.setStockQuantity(FURNITURE_STOCK);
        return furniture;
    }

    private Order buildOrder(Client client, Furniture furniture) {
        var detail = OrderDetail.builder()
                .id(1L)
                .order(null)
                .furniture(furniture)
                .quantity(QUANTITY)
                .unitPrice(FURNITURE_PRICE)
                .subtotal(FURNITURE_PRICE.multiply(BigDecimal.valueOf(QUANTITY)))
                .build();

        var order = Order.builder()
                .id(ORDER_ID)
                .orderNumber("ORD-20250601-0001")
                .status(EOrderStatus.PENDIENTE)
                .totalAmount(FURNITURE_PRICE.multiply(BigDecimal.valueOf(QUANTITY)))
                .notes("Nota del pedido")
                .client(client)
                .orderDetails(List.of(detail))
                .build();

        detail.setOrder(order);
        return order;
    }
}
