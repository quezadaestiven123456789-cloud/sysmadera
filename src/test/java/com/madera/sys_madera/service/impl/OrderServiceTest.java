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
import com.madera.sys_madera.service.SequenceGeneratorService;
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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
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

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

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
    private static final int EXPECTED_STOCK_AFTER = FURNITURE_STOCK - QUANTITY;

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
            given(sequenceGeneratorService.nextValue("ORDER_SEQ")).willReturn(1L);
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
            Order savedOrder = captor.getValue();
            assertThat(savedOrder.getClient().getId()).isEqualTo(CLIENT_ID);
            assertThat(savedOrder.getOrderDetails()).hasSize(1);

            Furniture savedFurniture = savedOrder.getOrderDetails().get(0).getFurniture();
            assertThat(savedFurniture.getStockQuantity()).isEqualTo(EXPECTED_STOCK_AFTER);
        }

        @Test
        @DisplayName("should reduce stock by the ordered quantity")
        void shouldReduceStock_whenCreatingOrder() {
            var request = buildRequest();
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(client));
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));
            given(sequenceGeneratorService.nextValue("ORDER_SEQ")).willReturn(1L);
            given(orderRepository.save(any(Order.class))).willReturn(order);

            orderService.create(request);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());

            OrderDetail savedDetail = captor.getValue().getOrderDetails().get(0);
            Furniture resultFurniture = savedDetail.getFurniture();

            assertEquals(EXPECTED_STOCK_AFTER, resultFurniture.getStockQuantity(),
                    "El stock del mueble debe reducirse en la cantidad ordenada");
        }

        @Test
        @DisplayName("should not save order nor furniture when stock is insufficient")
        void shouldNotSave_whenInsufficientStock() {
            var request = new OrderRequest(CLIENT_ID, "Nota",
                    List.of(new OrderRequest.OrderDetailRequest(FURNITURE_ID, 99)));
            var client = buildClient();
            var furniture = buildFurniture();
            furniture.setStockQuantity(5);

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(client));
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));

            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("Stock insuficiente")
                    .hasMessageContaining("5")
                    .hasMessageContaining("99");

            verify(orderRepository, never()).save(any());
            verify(furnitureRepository, never()).save(any());
        }

        @Test
        @DisplayName("should throw exception without saving when stock is exactly insufficient")
        void shouldThrowException_whenStockExactlyOneLess() {
            var request = new OrderRequest(CLIENT_ID, "Nota",
                    List.of(new OrderRequest.OrderDetailRequest(FURNITURE_ID, FURNITURE_STOCK + 1)));
            var client = buildClient();
            var furniture = buildFurniture();

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(client));
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));

            assertThatThrownBy(() -> orderService.create(request))
                    .isInstanceOf(BadRequestException.class);

            verify(orderRepository, never()).save(any());
            verify(furnitureRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reduce stock to zero when ordering exact available quantity")
        void shouldReduceStockToZero_whenOrderingFullStock() {
            var request = new OrderRequest(CLIENT_ID, "Nota",
                    List.of(new OrderRequest.OrderDetailRequest(FURNITURE_ID, FURNITURE_STOCK)));
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(client));
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));
            given(sequenceGeneratorService.nextValue("ORDER_SEQ")).willReturn(1L);
            given(orderRepository.save(any(Order.class))).willReturn(order);

            orderService.create(request);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());

            Furniture resultFurniture = captor.getValue().getOrderDetails().get(0).getFurniture();
            assertEquals(0, resultFurniture.getStockQuantity(),
                    "El stock debe quedar en cero cuando se ordena la cantidad exacta disponible");
        }

        @Test
        @DisplayName("should reduce stock correctly when ordering multiple furniture items")
        void shouldReduceStock_whenMultipleFurnitureItems() {
            Long secondFurnitureId = 2L;
            String secondFurnitureName = "Silla Pino";
            BigDecimal secondFurniturePrice = new BigDecimal("85.00");
            int secondFurnitureStock = 25;
            int secondQuantity = 5;

            var request = new OrderRequest(CLIENT_ID, "Nota",
                    List.of(
                            new OrderRequest.OrderDetailRequest(FURNITURE_ID, QUANTITY),
                            new OrderRequest.OrderDetailRequest(secondFurnitureId, secondQuantity)
                    ));
            var client = buildClient();
            var firstFurniture = buildFurniture();
            var secondFurniture = Furniture.builder()
                    .id(secondFurnitureId)
                    .name(secondFurnitureName)
                    .price(secondFurniturePrice)
                    .stockQuantity(secondFurnitureStock)
                    .build();

            var detail1 = OrderDetail.builder()
                    .id(1L)
                    .furniture(firstFurniture)
                    .quantity(QUANTITY)
                    .unitPrice(FURNITURE_PRICE)
                    .subtotal(FURNITURE_PRICE.multiply(BigDecimal.valueOf(QUANTITY)))
                    .build();
            var detail2 = OrderDetail.builder()
                    .id(2L)
                    .furniture(secondFurniture)
                    .quantity(secondQuantity)
                    .unitPrice(secondFurniturePrice)
                    .subtotal(secondFurniturePrice.multiply(BigDecimal.valueOf(secondQuantity)))
                    .build();

            var order = Order.builder()
                    .id(ORDER_ID)
                    .orderNumber("ORD-20250601-0001")
                    .status(EOrderStatus.PENDIENTE)
                    .totalAmount(FURNITURE_PRICE.multiply(BigDecimal.valueOf(QUANTITY))
                            .add(secondFurniturePrice.multiply(BigDecimal.valueOf(secondQuantity))))
                    .notes("Nota del pedido")
                    .client(client)
                    .orderDetails(List.of(detail1, detail2))
                    .build();
            detail1.setOrder(order);
            detail2.setOrder(order);

            given(clientRepository.findById(CLIENT_ID)).willReturn(Optional.of(client));
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(firstFurniture));
            given(furnitureRepository.findById(secondFurnitureId)).willReturn(Optional.of(secondFurniture));
            given(sequenceGeneratorService.nextValue("ORDER_SEQ")).willReturn(1L);
            given(orderRepository.save(any(Order.class))).willReturn(order);

            orderService.create(request);

            ArgumentCaptor<Order> captor = ArgumentCaptor.forClass(Order.class);
            verify(orderRepository).save(captor.capture());
            verify(furnitureRepository, times(2)).findById(any());

            List<OrderDetail> savedDetails = captor.getValue().getOrderDetails();
            assertThat(savedDetails).hasSize(2);

            Furniture savedFirst = savedDetails.get(0).getFurniture();
            Furniture savedSecond = savedDetails.get(1).getFurniture();

            assertEquals(FURNITURE_STOCK - QUANTITY, savedFirst.getStockQuantity(),
                    "Stock del primer mueble debe reducirse");
            assertEquals(secondFurnitureStock - secondQuantity, savedSecond.getStockQuantity(),
                    "Stock del segundo mueble debe reducirse");
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
            verify(furnitureRepository, never()).save(any());
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
            verify(furnitureRepository, never()).save(any());
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
            verify(furnitureRepository, never()).save(any());
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
            verify(furnitureRepository, never()).save(any());
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
        @DisplayName("should restore stock when cancelling order")
        void shouldRestoreStock_whenCancelling() {
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));
            given(orderRepository.save(any(Order.class))).willReturn(order);

            OrderResponse response = orderService.updateStatus(ORDER_ID, "CANCELADO");

            assertThat(response.status()).isEqualTo("CANCELADO");
            assertThat(furniture.getStockQuantity()).isEqualTo(FURNITURE_STOCK + QUANTITY);
            verify(furnitureRepository).findById(FURNITURE_ID);
            verify(orderRepository).save(any(Order.class));
        }

        @Test
        @DisplayName("should be idempotent when cancelling an already cancelled order")
        void shouldBeIdempotent_whenAlreadyCancelled() {
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);
            order.setStatus(EOrderStatus.CANCELADO);

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));

            OrderResponse response = orderService.updateStatus(ORDER_ID, "CANCELADO");

            assertThat(response.status()).isEqualTo("CANCELADO");
            assertThat(furniture.getStockQuantity()).isEqualTo(FURNITURE_STOCK);
            verify(furnitureRepository, never()).findById(any());
            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reject status change for delivered order")
        void shouldReject_whenOrderDelivered() {
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);
            order.setStatus(EOrderStatus.ENTREGADO);

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.updateStatus(ORDER_ID, "CANCELADO"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("ENTREGADO");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should reject status change for cancelled order")
        void shouldReject_whenOrderCancelled() {
            var client = buildClient();
            var furniture = buildFurniture();
            var order = buildOrder(client, furniture);
            order.setStatus(EOrderStatus.CANCELADO);

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.updateStatus(ORDER_ID, "PENDIENTE"))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessageContaining("CANCELADO");

            verify(orderRepository, never()).save(any());
        }

        @Test
        @DisplayName("should restore stock for all furniture items when cancelling")
        void shouldRestoreAllStock_whenCancelling() {
            var client = buildClient();
            var firstFurniture = buildFurniture();
            var secondFurniture = Furniture.builder()
                    .id(2L)
                    .name("Silla Pino")
                    .price(new BigDecimal("85.00"))
                    .stockQuantity(25)
                    .build();

            var detail1 = OrderDetail.builder()
                    .id(1L)
                    .furniture(firstFurniture)
                    .quantity(QUANTITY)
                    .unitPrice(FURNITURE_PRICE)
                    .subtotal(FURNITURE_PRICE.multiply(BigDecimal.valueOf(QUANTITY)))
                    .build();
            var detail2 = OrderDetail.builder()
                    .id(2L)
                    .furniture(secondFurniture)
                    .quantity(5)
                    .unitPrice(new BigDecimal("85.00"))
                    .subtotal(new BigDecimal("425.00"))
                    .build();

            var order = Order.builder()
                    .id(ORDER_ID)
                    .orderNumber("ORD-20250601-0001")
                    .status(EOrderStatus.PENDIENTE)
                    .totalAmount(new BigDecimal("500.00"))
                    .notes("Nota")
                    .client(client)
                    .orderDetails(List.of(detail1, detail2))
                    .build();
            detail1.setOrder(order);
            detail2.setOrder(order);

            given(orderRepository.findById(ORDER_ID)).willReturn(Optional.of(order));
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(firstFurniture));
            given(furnitureRepository.findById(2L)).willReturn(Optional.of(secondFurniture));
            given(orderRepository.save(any(Order.class))).willReturn(order);

            orderService.updateStatus(ORDER_ID, "CANCELADO");

            assertThat(firstFurniture.getStockQuantity()).isEqualTo(FURNITURE_STOCK + QUANTITY);
            assertThat(secondFurniture.getStockQuantity()).isEqualTo(25 + 5);
            verify(furnitureRepository, times(2)).findById(any());
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
