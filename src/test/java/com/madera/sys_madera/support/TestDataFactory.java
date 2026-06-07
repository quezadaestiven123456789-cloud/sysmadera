package com.madera.sys_madera.support;

import com.madera.sys_madera.dto.request.*;
import com.madera.sys_madera.dto.response.*;
import com.madera.sys_madera.model.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public final class TestDataFactory {

    private TestDataFactory() {}

    // ======================== Common Constants ========================

    public static final Long CLIENT_ID = 1L;
    public static final String CLIENT_NAME = "Juan Pérez";
    public static final String CLIENT_EMAIL = "juan@example.com";
    public static final String CLIENT_PHONE = "555-1234";
    public static final String CLIENT_ADDRESS = "Av. Siempre Viva 742";
    public static final String CLIENT_RFC = "JUPE800101";

    public static final Long FURNITURE_ID = 1L;
    public static final String FURNITURE_NAME = "Mesa Roble";
    public static final BigDecimal FURNITURE_PRICE = new BigDecimal("250.00");
    public static final String WOOD_TYPE = "Roble";
    public static final String CATEGORY = "Mesas";
    public static final String FURNITURE_DIMENSIONS = "100x200x75";
    public static final String FURNITURE_DESCRIPTION = "Mesa de roble macizo";
    public static final Integer FURNITURE_STOCK = 10;

    public static final Long ORDER_ID = 1L;
    public static final String ORDER_NUMBER = "ORD-20250601-0001";
    public static final String ORDER_NOTES = "Nota del pedido";
    public static final BigDecimal ORDER_TOTAL = new BigDecimal("500.00");

    public static final Long INVOICE_ID = 1L;
    public static final String INVOICE_NUMBER = "FAC-001";
    public static final String ORDER_NUMBER_FOR_INVOICE = "ORD-001";
    public static final BigDecimal INVOICE_TOTAL = new BigDecimal("1500.00");
    public static final BigDecimal INVOICE_PAID = new BigDecimal("500.00");
    public static final BigDecimal INVOICE_BALANCE = new BigDecimal("1000.00");

    public static final Long WOOD_INVENTORY_ID = 1L;
    public static final String WOOD_INVENTORY_TYPE = "Roble";
    public static final BigDecimal WOOD_QUANTITY = new BigDecimal("100.00");
    public static final String WOOD_UNIT = "m3";
    public static final BigDecimal WOOD_UNIT_PRICE = new BigDecimal("50.00");
    public static final BigDecimal WOOD_TOTAL_VALUE = new BigDecimal("5000.00");
    public static final BigDecimal WOOD_MINIMUM_STOCK = new BigDecimal("10.00");
    public static final String WOOD_SUPPLIER = "Proveedor A";
    public static final String WOOD_DESCRIPTION = "Roble macizo";

    public static final Long USER_ID = 1L;
    public static final String USERNAME = "jperez";
    public static final String EMAIL = "juan.perez@example.com";
    public static final String PASSWORD = "password123";
    public static final String FIRST_NAME = "Juan";
    public static final String LAST_NAME = "Pérez";

    public static final Long ROLE_ADMIN_ID = 1L;
    public static final Long ROLE_EMPLEADO_ID = 2L;
    public static final Long ROLE_CLIENTE_ID = 3L;

    // ======================== Role ========================

    public static Role buildRole(ERole name) {
        return Role.builder().name(name).build();
    }

    public static Role buildRole(Long id, ERole name) {
        return Role.builder().id(id).name(name).build();
    }

    public static Role buildAdminRole() {
        return buildRole(ROLE_ADMIN_ID, ERole.ROLE_ADMIN);
    }

    public static Role buildEmpleadoRole() {
        return buildRole(ROLE_EMPLEADO_ID, ERole.ROLE_EMPLEADO);
    }

    public static Role buildClienteRole() {
        return buildRole(ROLE_CLIENTE_ID, ERole.ROLE_CLIENTE);
    }

    // ======================== User ========================

    public static User buildUser() {
        return User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .enabled(true)
                .roles(Set.of(buildAdminRole()))
                .build();
    }

    public static User buildUserWithRoles(Set<Role> roles) {
        return User.builder()
                .id(USER_ID)
                .username(USERNAME)
                .email(EMAIL)
                .password(PASSWORD)
                .firstName(FIRST_NAME)
                .lastName(LAST_NAME)
                .enabled(true)
                .roles(roles)
                .build();
    }

    // ======================== Client (Entity) ========================

    public static Client buildClient() {
        return Client.builder()
                .id(CLIENT_ID)
                .name(CLIENT_NAME)
                .email(CLIENT_EMAIL)
                .phone(CLIENT_PHONE)
                .address(CLIENT_ADDRESS)
                .rfc(CLIENT_RFC)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Client buildClient(Long id, String name) {
        return Client.builder()
                .id(id)
                .name(name)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ======================== Furniture (Entity) ========================

    public static Furniture buildFurniture() {
        return Furniture.builder()
                .id(FURNITURE_ID)
                .name(FURNITURE_NAME)
                .description(FURNITURE_DESCRIPTION)
                .price(FURNITURE_PRICE)
                .woodType(WOOD_TYPE)
                .dimensions(FURNITURE_DIMENSIONS)
                .category(CATEGORY)
                .stockQuantity(FURNITURE_STOCK)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static Furniture buildFurniture(Long id, String name, Integer stock) {
        return Furniture.builder()
                .id(id)
                .name(name)
                .price(FURNITURE_PRICE)
                .stockQuantity(stock)
                .active(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ======================== Order & OrderDetail (Entities) ========================

    public static Order buildOrder(Client client, List<OrderDetail> details) {
        var order = Order.builder()
                .id(ORDER_ID)
                .orderNumber(ORDER_NUMBER)
                .status(EOrderStatus.PENDIENTE)
                .totalAmount(ORDER_TOTAL)
                .notes(ORDER_NOTES)
                .client(client)
                .orderDetails(details)
                .build();
        details.forEach(d -> d.setOrder(order));
        return order;
    }

    public static OrderDetail buildOrderDetail(Furniture furniture, int quantity) {
        return OrderDetail.builder()
                .id(1L)
                .furniture(furniture)
                .quantity(quantity)
                .unitPrice(furniture.getPrice())
                .subtotal(furniture.getPrice().multiply(BigDecimal.valueOf(quantity)))
                .build();
    }

    // ======================== Invoice (Entity) ========================

    public static Invoice buildInvoice() {
        return Invoice.builder()
                .id(INVOICE_ID)
                .invoiceNumber(INVOICE_NUMBER)
                .issueDate(LocalDate.now())
                .dueDate(LocalDate.now().plusDays(30))
                .totalAmount(INVOICE_TOTAL)
                .paidAmount(INVOICE_PAID)
                .status(EInvoiceStatus.PENDIENTE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ======================== WoodInventory (Entity) ========================

    public static WoodInventory buildWoodInventory() {
        return WoodInventory.builder()
                .id(WOOD_INVENTORY_ID)
                .woodType(WOOD_INVENTORY_TYPE)
                .quantity(WOOD_QUANTITY)
                .unit(WOOD_UNIT)
                .unitPrice(WOOD_UNIT_PRICE)
                .supplier(WOOD_SUPPLIER)
                .description(WOOD_DESCRIPTION)
                .minimumStock(WOOD_MINIMUM_STOCK)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    public static WoodInventory buildWoodInventory(Long id, String woodType, BigDecimal quantity) {
        return WoodInventory.builder()
                .id(id)
                .woodType(woodType)
                .quantity(quantity)
                .unit(WOOD_UNIT)
                .unitPrice(WOOD_UNIT_PRICE)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // ======================== WoodSurplus (Entity) ========================

    public static WoodSurplus buildWoodSurplus() {
        return WoodSurplus.builder()
                .id(1L)
                .woodType(WOOD_INVENTORY_TYPE)
                .quantity(new BigDecimal("50.00"))
                .unit(WOOD_UNIT)
                .available(true)
                .build();
    }

    // ======================== DTO Responses ========================

    public static ClientResponse buildClientResponse() {
        return new ClientResponse(
                CLIENT_ID, CLIENT_NAME, CLIENT_EMAIL, CLIENT_PHONE,
                CLIENT_ADDRESS, CLIENT_RFC, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static ClientResponse buildClientResponse(Long id, String name) {
        return new ClientResponse(
                id, name, CLIENT_EMAIL, CLIENT_PHONE,
                CLIENT_ADDRESS, CLIENT_RFC, null,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static FurnitureResponse buildFurnitureResponse() {
        return new FurnitureResponse(
                FURNITURE_ID, FURNITURE_NAME, FURNITURE_DESCRIPTION,
                FURNITURE_PRICE, WOOD_TYPE, FURNITURE_DIMENSIONS, CATEGORY, FURNITURE_STOCK,
                true, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public static FurnitureResponse buildFurnitureResponse(Long id, String name) {
        return new FurnitureResponse(
                id, name, FURNITURE_DESCRIPTION,
                FURNITURE_PRICE, WOOD_TYPE, FURNITURE_DIMENSIONS, CATEGORY, FURNITURE_STOCK,
                true, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public static OrderResponse.OrderDetailResponse buildOrderDetailResponse() {
        return new OrderResponse.OrderDetailResponse(
                1L, FURNITURE_ID, FURNITURE_NAME, 2,
                new BigDecimal("250.00"), new BigDecimal("500.00"));
    }

    public static OrderResponse buildOrderResponse() {
        return new OrderResponse(
                ORDER_ID, ORDER_NUMBER, "PENDIENTE", ORDER_TOTAL,
                ORDER_NOTES, CLIENT_NAME, CLIENT_ID,
                List.of(buildOrderDetailResponse()),
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static OrderResponse buildOrderResponse(Long id, String status) {
        return new OrderResponse(
                id, ORDER_NUMBER, status, ORDER_TOTAL,
                ORDER_NOTES, CLIENT_NAME, CLIENT_ID,
                List.of(buildOrderDetailResponse()),
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static InvoiceResponse buildInvoiceResponse() {
        return new InvoiceResponse(
                INVOICE_ID, INVOICE_NUMBER, LocalDate.now(), LocalDate.now().plusDays(30),
                INVOICE_TOTAL, INVOICE_PAID, INVOICE_BALANCE, "PENDIENTE", null,
                ORDER_ID, ORDER_NUMBER_FOR_INVOICE, CLIENT_NAME,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static InvoiceResponse buildInvoiceResponse(Long id, String status) {
        return new InvoiceResponse(
                id, INVOICE_NUMBER, LocalDate.now(), LocalDate.now().plusDays(30),
                INVOICE_TOTAL, INVOICE_PAID, INVOICE_BALANCE, status, null,
                ORDER_ID, ORDER_NUMBER_FOR_INVOICE, CLIENT_NAME,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static WoodInventoryResponse buildWoodInventoryResponse() {
        return new WoodInventoryResponse(
                WOOD_INVENTORY_ID, WOOD_INVENTORY_TYPE, WOOD_QUANTITY, WOOD_UNIT,
                WOOD_UNIT_PRICE, WOOD_TOTAL_VALUE, WOOD_SUPPLIER, WOOD_DESCRIPTION,
                WOOD_MINIMUM_STOCK, false,
                LocalDateTime.now(), LocalDateTime.now());
    }

    public static MessageResponse buildMessageResponse(String message) {
        return new MessageResponse(message);
    }

    // ======================== DTO Requests ========================

    public static ClientRequest buildClientRequest() {
        return new ClientRequest(CLIENT_NAME, CLIENT_EMAIL, CLIENT_PHONE, CLIENT_ADDRESS, CLIENT_RFC);
    }

    public static FurnitureRequest buildFurnitureRequest() {
        return new FurnitureRequest(FURNITURE_NAME, FURNITURE_DESCRIPTION, FURNITURE_PRICE,
                WOOD_TYPE, FURNITURE_DIMENSIONS, CATEGORY, FURNITURE_STOCK, null);
    }

    public static OrderRequest buildOrderRequest(Long clientId, Long furnitureId, int quantity) {
        return new OrderRequest(clientId, ORDER_NOTES,
                List.of(new OrderRequest.OrderDetailRequest(furnitureId, quantity)));
    }

    public static InvoiceRequest buildInvoiceRequest(Long orderId) {
        return new InvoiceRequest(orderId, LocalDate.now(), LocalDate.now().plusDays(30), INVOICE_PAID, "Pago inicial");
    }

    public static WoodInventoryRequest buildWoodInventoryRequest() {
        return new WoodInventoryRequest(WOOD_INVENTORY_TYPE, WOOD_QUANTITY, WOOD_UNIT,
                WOOD_UNIT_PRICE, WOOD_SUPPLIER, WOOD_DESCRIPTION, WOOD_MINIMUM_STOCK);
    }

    // ======================== PagedResponse Helpers ========================

    public static <T> PagedResponse<T> buildEmptyPage() {
        return new PagedResponse<>(List.of(), 0, 10, 0L, 0, true);
    }

    public static <T> PagedResponse<T> buildSinglePage(List<T> content) {
        return new PagedResponse<>(content, 0, 10, (long) content.size(), content.size() > 0 ? 1 : 0, true);
    }

    public static <T> PagedResponse<T> buildPage(List<T> content, int page, int size, long totalElements) {
        int totalPages = size > 0 ? (int) Math.ceil((double) totalElements / size) : 0;
        return new PagedResponse<>(content, page, size, totalElements, totalPages, page >= totalPages - 1);
    }
}
