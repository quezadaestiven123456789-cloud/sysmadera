package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.WoodInventoryRequest;
import com.madera.sys_madera.dto.response.WoodInventoryResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.DuplicateResourceException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.model.WoodInventory;
import com.madera.sys_madera.repository.WoodInventoryRepository;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WoodInventoryServiceImpl")
class WoodInventoryServiceImplTest {

    @Mock
    private WoodInventoryRepository woodInventoryRepository;

    @InjectMocks
    private WoodInventoryServiceImpl woodInventoryService;

    private static final Long INVENTORY_ID = 1L;
    private static final String WOOD_TYPE = "Caoba";
    private static final BigDecimal QUANTITY = new BigDecimal("50.00");
    private static final String UNIT = "m³";
    private static final BigDecimal UNIT_PRICE = new BigDecimal("1200.00");
    private static final String SUPPLIER = "Maderas del Sur";
    private static final String DESCRIPTION = "Caoba de primera calidad";
    private static final BigDecimal MINIMUM_STOCK = new BigDecimal("10.00");

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create inventory item when request is valid")
        void shouldCreate_whenValidRequest() {
            var request = buildRequest();
            var savedItem = buildInventory();

            given(woodInventoryRepository.findByWoodTypeIgnoreCase(WOOD_TYPE)).willReturn(Optional.empty());
            given(woodInventoryRepository.save(any(WoodInventory.class))).willReturn(savedItem);

            WoodInventoryResponse response = woodInventoryService.create(request);

            assertThat(response.id()).isEqualTo(INVENTORY_ID);
            assertThat(response.woodType()).isEqualTo(WOOD_TYPE);
            assertThat(response.quantity()).isEqualByComparingTo(QUANTITY);
            assertThat(response.unit()).isEqualTo(UNIT);
            assertThat(response.unitPrice()).isEqualByComparingTo(UNIT_PRICE);
            assertThat(response.totalValue()).isEqualByComparingTo(QUANTITY.multiply(UNIT_PRICE));
            assertThat(response.supplier()).isEqualTo(SUPPLIER);
            assertThat(response.lowStock()).isFalse();

            ArgumentCaptor<WoodInventory> captor = ArgumentCaptor.forClass(WoodInventory.class);
            verify(woodInventoryRepository).save(captor.capture());
            assertThat(captor.getValue().getWoodType()).isEqualTo(WOOD_TYPE);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when wood type already exists")
        void shouldThrowException_whenDuplicateWoodType() {
            var request = buildRequest();

            given(woodInventoryRepository.findByWoodTypeIgnoreCase(WOOD_TYPE))
                    .willReturn(Optional.of(new WoodInventory()));

            assertThatThrownBy(() -> woodInventoryService.create(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining(WOOD_TYPE);

            verify(woodInventoryRepository, never()).save(any());
        }

        @ParameterizedTest
        @CsvSource({
            "-50.00, quantity",
            "0, quantity",
            "-10.00, unitPrice"
        })
        @DisplayName("should create with edge values for quantity/unitPrice")
        void shouldCreateWithEdgeValues(String valueStr, String field) {
            BigDecimal value = new BigDecimal(valueStr);
            BigDecimal quantity = "quantity".equals(field) ? value : QUANTITY;
            BigDecimal unitPrice = "unitPrice".equals(field) ? value : UNIT_PRICE;

            var request = new WoodInventoryRequest("Pino", quantity, "m3", unitPrice, "Prov", null, MINIMUM_STOCK);
            var saved = WoodInventory.builder()
                    .id(2L).woodType("Pino")
                    .quantity(quantity)
                    .unit("m3")
                    .unitPrice(unitPrice)
                    .minimumStock(MINIMUM_STOCK)
                    .build();

            given(woodInventoryRepository.findByWoodTypeIgnoreCase("Pino")).willReturn(Optional.empty());
            given(woodInventoryRepository.save(any(WoodInventory.class))).willReturn(saved);

            WoodInventoryResponse response = woodInventoryService.create(request);

            if ("quantity".equals(field)) {
                assertThat(response.quantity()).isEqualByComparingTo(value);
            } else {
                assertThat(response.unitPrice()).isEqualByComparingTo(value);
            }
        }

        @Test
        @DisplayName("should create with null optional fields")
        void shouldCreateWithNullOptionalFields() {
            var request = new WoodInventoryRequest(
                    "Pino", QUANTITY, "m3", UNIT_PRICE, null, null, null);
            var saved = WoodInventory.builder()
                    .id(2L).woodType("Pino").quantity(QUANTITY)
                    .unit("m3").unitPrice(UNIT_PRICE).build();

            given(woodInventoryRepository.findByWoodTypeIgnoreCase("Pino")).willReturn(Optional.empty());
            given(woodInventoryRepository.save(any(WoodInventory.class))).willReturn(saved);

            WoodInventoryResponse response = woodInventoryService.create(request);

            assertThat(response.supplier()).isNull();
            assertThat(response.description()).isNull();
            assertThat(response.minimumStock()).isNull();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update inventory item when request is valid")
        void shouldUpdate_whenValidRequest() {
            var request = buildRequest();
            var existingItem = buildInventory();

            given(woodInventoryRepository.findById(INVENTORY_ID)).willReturn(Optional.of(existingItem));
            given(woodInventoryRepository.findByWoodTypeIgnoreCase(WOOD_TYPE)).willReturn(Optional.of(existingItem));
            given(woodInventoryRepository.save(any(WoodInventory.class))).willReturn(existingItem);

            WoodInventoryResponse response = woodInventoryService.update(INVENTORY_ID, request);

            assertThat(response.woodType()).isEqualTo(WOOD_TYPE);
            verify(woodInventoryRepository).save(any(WoodInventory.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when item not found")
        void shouldThrowException_whenNotFound() {
            var request = buildRequest();

            given(woodInventoryRepository.findById(INVENTORY_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> woodInventoryService.update(INVENTORY_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Inventario");
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when new wood type conflicts with another item")
        void shouldThrowException_whenWoodTypeConflicts() {
            var request = new WoodInventoryRequest("Pino", QUANTITY, UNIT, UNIT_PRICE, SUPPLIER, DESCRIPTION, MINIMUM_STOCK);
            var existingItem = buildInventory();
            var otherItem = WoodInventory.builder().id(2L).woodType("Pino").build();

            given(woodInventoryRepository.findById(INVENTORY_ID)).willReturn(Optional.of(existingItem));
            given(woodInventoryRepository.findByWoodTypeIgnoreCase("Pino")).willReturn(Optional.of(otherItem));

            assertThatThrownBy(() -> woodInventoryService.update(INVENTORY_ID, request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining("Pino");

            verify(woodInventoryRepository, never()).save(any());
        }

        @ParameterizedTest
        @CsvSource({
            "-50.00, quantity",
            "0, quantity",
            "-10.00, unitPrice",
            "0, unitPrice"
        })
        @DisplayName("should update with edge values for quantity/unitPrice")
        void shouldUpdateWithEdgeValues(String valueStr, String field) {
            BigDecimal value = new BigDecimal(valueStr);
            BigDecimal quantity = "quantity".equals(field) ? value : QUANTITY;
            BigDecimal unitPrice = "unitPrice".equals(field) ? value : UNIT_PRICE;

            var request = new WoodInventoryRequest(
                    WOOD_TYPE, quantity, UNIT, unitPrice,
                    SUPPLIER, DESCRIPTION, MINIMUM_STOCK);
            var existingItem = buildInventory();

            given(woodInventoryRepository.findById(INVENTORY_ID)).willReturn(Optional.of(existingItem));
            given(woodInventoryRepository.findByWoodTypeIgnoreCase(WOOD_TYPE)).willReturn(Optional.of(existingItem));
            given(woodInventoryRepository.save(any(WoodInventory.class))).willReturn(existingItem);

            WoodInventoryResponse response = woodInventoryService.update(INVENTORY_ID, request);

            if ("quantity".equals(field)) {
                assertThat(response.quantity()).isEqualByComparingTo(value);
                assertThat(response.totalValue()).isEqualByComparingTo(value.multiply(UNIT_PRICE));
            } else {
                assertThat(response.unitPrice()).isEqualByComparingTo(value);
                assertThat(response.totalValue()).isEqualByComparingTo(QUANTITY.multiply(value));
            }
            verify(woodInventoryRepository).save(any(WoodInventory.class));
        }

        @Test
        @DisplayName("should update with null optional fields")
        void shouldUpdateWithNullOptionalFields() {
            var request = new WoodInventoryRequest(
                    WOOD_TYPE, QUANTITY, UNIT, UNIT_PRICE,
                    null, null, null);
            var existingItem = buildInventory();

            given(woodInventoryRepository.findById(INVENTORY_ID)).willReturn(Optional.of(existingItem));
            given(woodInventoryRepository.findByWoodTypeIgnoreCase(WOOD_TYPE)).willReturn(Optional.of(existingItem));
            given(woodInventoryRepository.save(any(WoodInventory.class))).willReturn(existingItem);

            WoodInventoryResponse response = woodInventoryService.update(INVENTORY_ID, request);

            assertThat(response.supplier()).isNull();
            assertThat(response.description()).isNull();
            assertThat(response.minimumStock()).isNull();
            assertThat(response.lowStock()).isFalse();
            verify(woodInventoryRepository).save(any(WoodInventory.class));
        }

        @Test
        @DisplayName("should update when wood type is unchanged (same id)")
        void shouldUpdate_whenSameWoodType() {
            var request = new WoodInventoryRequest(
                    WOOD_TYPE, QUANTITY, UNIT, UNIT_PRICE,
                    SUPPLIER, DESCRIPTION, MINIMUM_STOCK);
            var existingItem = buildInventory();

            given(woodInventoryRepository.findById(INVENTORY_ID)).willReturn(Optional.of(existingItem));
            given(woodInventoryRepository.findByWoodTypeIgnoreCase(WOOD_TYPE)).willReturn(Optional.of(existingItem));
            given(woodInventoryRepository.save(any(WoodInventory.class))).willReturn(existingItem);

            WoodInventoryResponse response = woodInventoryService.update(INVENTORY_ID, request);

            assertThat(response.woodType()).isEqualTo(WOOD_TYPE);
            assertThat(response.quantity()).isEqualByComparingTo(QUANTITY);
            assertThat(response.unitPrice()).isEqualByComparingTo(UNIT_PRICE);
            verify(woodInventoryRepository).save(any(WoodInventory.class));
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return item when exists")
        void shouldReturn_whenExists() {
            var item = buildInventory();

            given(woodInventoryRepository.findById(INVENTORY_ID)).willReturn(Optional.of(item));

            WoodInventoryResponse response = woodInventoryService.findById(INVENTORY_ID);

            assertThat(response.id()).isEqualTo(INVENTORY_ID);
            assertThat(response.woodType()).isEqualTo(WOOD_TYPE);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowException_whenNotFound() {
            given(woodInventoryRepository.findById(INVENTORY_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> woodInventoryService.findById(INVENTORY_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Inventario");
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return paginated items when no search filter")
        void shouldReturnAll_whenNoSearch() {
            var item = buildInventory();
            var page = new PageImpl<>(List.of(item));

            given(woodInventoryRepository.findAll(any(Pageable.class))).willReturn(page);

            PagedResponse<WoodInventoryResponse> response = woodInventoryService.findAll(0, 10, "id", "asc", null);

            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).woodType()).isEqualTo(WOOD_TYPE);
        }

        @Test
        @DisplayName("should filter items by wood type search")
        void shouldFilterBySearch() {
            var item = buildInventory();
            var page = new PageImpl<>(List.of(item));

            given(woodInventoryRepository.findByWoodTypeContainingIgnoreCase(anyString(), any(Pageable.class)))
                    .willReturn(page);

            PagedResponse<WoodInventoryResponse> response = woodInventoryService.findAll(0, 10, "id", "asc", "Caoba");

            assertThat(response.content()).hasSize(1);
        }

        @Test
        @DisplayName("should return empty page when no items match")
        void shouldReturnEmpty_whenNoMatch() {
            given(woodInventoryRepository.findByWoodTypeContainingIgnoreCase(anyString(), any(Pageable.class)))
                    .willReturn(Page.empty());

            PagedResponse<WoodInventoryResponse> response = woodInventoryService.findAll(0, 10, "id", "asc", "NoExiste");

            assertThat(response.content()).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should delete item when exists")
        void shouldDelete_whenExists() {
            var item = buildInventory();

            given(woodInventoryRepository.findById(INVENTORY_ID)).willReturn(Optional.of(item));

            woodInventoryService.delete(INVENTORY_ID);

            verify(woodInventoryRepository).delete(item);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when item not found")
        void shouldThrowException_whenNotFound() {
            given(woodInventoryRepository.findById(INVENTORY_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> woodInventoryService.delete(INVENTORY_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Inventario");
        }
    }

    @Nested
    @DisplayName("findLowStock")
    class FindLowStock {

        @Test
        @DisplayName("should return items below minimum stock")
        void shouldReturnLowStockItems() {
            var lowItem = WoodInventory.builder()
                    .id(1L).woodType("Caoba")
                    .quantity(new BigDecimal("5.00")).unit("m³")
                    .unitPrice(new BigDecimal("1200.00"))
                    .minimumStock(new BigDecimal("10.00"))
                    .build();

            given(woodInventoryRepository.findAll()).willReturn(List.of(lowItem));

            List<WoodInventoryResponse> result = woodInventoryService.findLowStock();

            assertThat(result).hasSize(1);
            assertThat(result.get(0).lowStock()).isTrue();
        }

        @Test
        @DisplayName("should return empty list when all items have sufficient stock")
        void shouldReturnEmpty_whenAllSufficient() {
            var item = buildInventory();

            given(woodInventoryRepository.findAll()).willReturn(List.of(item));

            List<WoodInventoryResponse> result = woodInventoryService.findLowStock();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should ignore items with null minimum stock")
        void shouldIgnoreItemsWithNullMinimum() {
            var item = WoodInventory.builder()
                    .id(1L).woodType("Caoba")
                    .quantity(new BigDecimal("5.00")).unit("m³")
                    .unitPrice(new BigDecimal("1200.00"))
                    .minimumStock(null)
                    .build();

            given(woodInventoryRepository.findAll()).willReturn(List.of(item));

            List<WoodInventoryResponse> result = woodInventoryService.findLowStock();

            assertThat(result).isEmpty();
        }
    }

    private WoodInventoryRequest buildRequest() {
        return new WoodInventoryRequest(WOOD_TYPE, QUANTITY, UNIT, UNIT_PRICE, SUPPLIER, DESCRIPTION, MINIMUM_STOCK);
    }

    private WoodInventory buildInventory() {
        return WoodInventory.builder()
                .id(INVENTORY_ID)
                .woodType(WOOD_TYPE)
                .quantity(QUANTITY)
                .unit(UNIT)
                .unitPrice(UNIT_PRICE)
                .supplier(SUPPLIER)
                .description(DESCRIPTION)
                .minimumStock(MINIMUM_STOCK)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
