package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.FurnitureRequest;
import com.madera.sys_madera.dto.response.FurnitureResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.DuplicateResourceException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.model.Furniture;
import com.madera.sys_madera.repository.FurnitureRepository;
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
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("FurnitureServiceImpl")
class FurnitureServiceImplTest {

    @Mock
    private FurnitureRepository furnitureRepository;

    @InjectMocks
    private FurnitureServiceImpl furnitureService;

    private static final Long FURNITURE_ID = 1L;
    private static final String FURNITURE_NAME = "Mesa Roble";
    private static final String DESCRIPTION = "Mesa de roble macizo de 2m";
    private static final BigDecimal PRICE = new BigDecimal("2500.00");
    private static final String WOOD_TYPE = "Roble";
    private static final String DIMENSIONS = "200x90x75 cm";
    private static final String CATEGORY = "Mesas";
    private static final Integer STOCK = 15;
    private static final String IMAGE_URL = "/images/mesa-roble.jpg";

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should create furniture when request is valid")
        void shouldCreate_whenValidRequest() {
            var request = buildRequest();
            var savedFurniture = buildFurniture();

            given(furnitureRepository.existsByName(FURNITURE_NAME)).willReturn(false);
            given(furnitureRepository.save(any(Furniture.class))).willReturn(savedFurniture);

            FurnitureResponse response = furnitureService.create(request);

            assertThat(response.id()).isEqualTo(FURNITURE_ID);
            assertThat(response.name()).isEqualTo(FURNITURE_NAME);
            assertThat(response.price()).isEqualByComparingTo(PRICE);
            assertThat(response.stockQuantity()).isEqualTo(STOCK);
            assertThat(response.category()).isEqualTo(CATEGORY);
            assertThat(response.woodType()).isEqualTo(WOOD_TYPE);
            assertThat(response.dimensions()).isEqualTo(DIMENSIONS);
            assertThat(response.active()).isTrue();

            ArgumentCaptor<Furniture> captor = ArgumentCaptor.forClass(Furniture.class);
            verify(furnitureRepository).save(captor.capture());
            assertThat(captor.getValue().getName()).isEqualTo(FURNITURE_NAME);
            assertThat(captor.getValue().getPrice()).isEqualByComparingTo(PRICE);
        }

        @Test
        @DisplayName("should throw DuplicateResourceException when name already exists")
        void shouldThrowException_whenDuplicateName() {
            var request = buildRequest();

            given(furnitureRepository.existsByName(FURNITURE_NAME)).willReturn(true);

            assertThatThrownBy(() -> furnitureService.create(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessageContaining(FURNITURE_NAME);

            verify(furnitureRepository, never()).save(any());
        }

        @Test
        @DisplayName("should default stock to 0 when null")
        void shouldDefaultStockToZero_whenNull() {
            var request = new FurnitureRequest(
                    "Silla", "Silla de madera", new BigDecimal("500.00"),
                    "Pino", "40x40x90 cm", "Sillas", null, null);
            var saved = Furniture.builder()
                    .id(2L).name("Silla").description("Silla de madera")
                    .price(new BigDecimal("500.00")).woodType("Pino")
                    .dimensions("40x40x90 cm").category("Sillas")
                    .stockQuantity(0).active(true)
                    .build();

            given(furnitureRepository.existsByName("Silla")).willReturn(false);
            given(furnitureRepository.save(any(Furniture.class))).willReturn(saved);

            FurnitureResponse response = furnitureService.create(request);

            assertThat(response.stockQuantity()).isZero();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should update furniture when request is valid")
        void shouldUpdate_whenValidRequest() {
            var request = buildRequest();
            var existing = buildFurniture();

            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(existing));
            given(furnitureRepository.save(any(Furniture.class))).willReturn(existing);

            FurnitureResponse response = furnitureService.update(FURNITURE_ID, request);

            assertThat(response.name()).isEqualTo(FURNITURE_NAME);
            assertThat(response.price()).isEqualByComparingTo(PRICE);
            verify(furnitureRepository).save(any(Furniture.class));
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when furniture not found")
        void shouldThrowException_whenNotFound() {
            var request = buildRequest();

            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> furnitureService.update(FURNITURE_ID, request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Mueble");
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return furniture when exists")
        void shouldReturn_whenExists() {
            var furniture = buildFurniture();

            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));

            FurnitureResponse response = furnitureService.findById(FURNITURE_ID);

            assertThat(response.id()).isEqualTo(FURNITURE_ID);
            assertThat(response.name()).isEqualTo(FURNITURE_NAME);
            assertThat(response.price()).isEqualByComparingTo(PRICE);
            assertThat(response.stockQuantity()).isEqualTo(STOCK);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when not found")
        void shouldThrowException_whenNotFound() {
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> furnitureService.findById(FURNITURE_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Mueble");
        }

        @Test
        @DisplayName("should return inactive furniture")
        void shouldReturnInactiveRecord() {
            var furniture = buildFurniture();
            furniture.setActive(false);

            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));

            FurnitureResponse response = furnitureService.findById(FURNITURE_ID);

            assertThat(response.active()).isFalse();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("should return all furniture when no filters")
        void shouldReturnAll_whenNoFilters() {
            var furniture = buildFurniture();
            var page = new PageImpl<>(List.of(furniture));

            given(furnitureRepository.findAll(any(Pageable.class))).willReturn(page);

            PagedResponse<FurnitureResponse> response = furnitureService
                    .findAll(0, 10, "id", "asc", null, null);

            assertThat(response.content()).hasSize(1);
            assertThat(response.content().get(0).name()).isEqualTo(FURNITURE_NAME);
        }

        @Test
        @DisplayName("should filter by category")
        void shouldFilterByCategory() {
            var furniture = buildFurniture();
            var page = new PageImpl<>(List.of(furniture));

            given(furnitureRepository.findByCategoryIgnoreCase(anyString(), any(Pageable.class)))
                    .willReturn(page);

            PagedResponse<FurnitureResponse> response = furnitureService
                    .findAll(0, 10, "id", "asc", null, "Mesas");

            assertThat(response.content()).hasSize(1);
        }

        @Test
        @DisplayName("should search by name")
        void shouldSearchByName() {
            var furniture = buildFurniture();
            var page = new PageImpl<>(List.of(furniture));

            given(furnitureRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                    .willReturn(page);

            PagedResponse<FurnitureResponse> response = furnitureService
                    .findAll(0, 10, "id", "asc", "Mesa", null);

            assertThat(response.content()).hasSize(1);
        }

        @Test
        @DisplayName("category filter takes precedence over search")
        void categoryTakesPrecedenceOverSearch() {
            var furniture = buildFurniture();
            var page = new PageImpl<>(List.of(furniture));

            given(furnitureRepository.findByCategoryIgnoreCase(anyString(), any(Pageable.class)))
                    .willReturn(page);

            PagedResponse<FurnitureResponse> response = furnitureService
                    .findAll(0, 10, "id", "asc", "Mesa", "Mesas");

            assertThat(response.content()).hasSize(1);
        }

        @Test
        @DisplayName("should include inactive records when no filters")
        void shouldIncludeInactiveRecords() {
            var activeFurniture = buildFurniture();
            var inactiveFurniture = buildFurniture();
            inactiveFurniture.setId(2L);
            inactiveFurniture.setActive(false);
            var page = new PageImpl<>(List.of(activeFurniture, inactiveFurniture));

            given(furnitureRepository.findAll(any(Pageable.class))).willReturn(page);

            PagedResponse<FurnitureResponse> response = furnitureService
                    .findAll(0, 10, "id", "asc", null, null);

            assertThat(response.content()).hasSize(2);
            assertThat(response.content().get(0).active()).isTrue();
            assertThat(response.content().get(1).active()).isFalse();
        }

        @Test
        @DisplayName("should include inactive records in search results")
        void shouldIncludeInactiveInSearch() {
            var activeFurniture = buildFurniture();
            var inactiveFurniture = buildFurniture();
            inactiveFurniture.setId(2L);
            inactiveFurniture.setActive(false);
            var page = new PageImpl<>(List.of(activeFurniture, inactiveFurniture));

            given(furnitureRepository.findByNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                    .willReturn(page);

            PagedResponse<FurnitureResponse> response = furnitureService
                    .findAll(0, 10, "id", "asc", "Mesa", null);

            assertThat(response.content()).hasSize(2);
        }

        @Test
        @DisplayName("should include inactive records in category filter results")
        void shouldIncludeInactiveInCategoryFilter() {
            var activeFurniture = buildFurniture();
            var inactiveFurniture = buildFurniture();
            inactiveFurniture.setId(2L);
            inactiveFurniture.setActive(false);
            var page = new PageImpl<>(List.of(activeFurniture, inactiveFurniture));

            given(furnitureRepository.findByCategoryIgnoreCase(anyString(), any(Pageable.class)))
                    .willReturn(page);

            PagedResponse<FurnitureResponse> response = furnitureService
                    .findAll(0, 10, "id", "asc", null, "Mesas");

            assertThat(response.content()).hasSize(2);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should soft-delete furniture by setting active to false")
        void shouldSoftDelete() {
            var furniture = buildFurniture();
            furniture.setActive(true);

            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));
            given(furnitureRepository.save(any(Furniture.class))).willReturn(furniture);

            furnitureService.delete(FURNITURE_ID);

            assertThat(furniture.getActive()).isFalse();
            verify(furnitureRepository).save(furniture);
        }

        @Test
        @DisplayName("should throw ResourceNotFoundException when furniture not found")
        void shouldThrowException_whenNotFound() {
            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.empty());

            assertThatThrownBy(() -> furnitureService.delete(FURNITURE_ID))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Mueble");
        }

        @Test
        @DisplayName("should not physically delete record")
        void shouldNotPhysicallyDeleteRecord() {
            var furniture = buildFurniture();
            furniture.setActive(true);

            given(furnitureRepository.findById(FURNITURE_ID)).willReturn(Optional.of(furniture));
            given(furnitureRepository.save(any(Furniture.class))).willReturn(furniture);

            furnitureService.delete(FURNITURE_ID);

            verify(furnitureRepository, never()).delete(any(Furniture.class));
            verify(furnitureRepository, never()).deleteById(anyLong());
        }
    }

    private FurnitureRequest buildRequest() {
        return new FurnitureRequest(
                FURNITURE_NAME, DESCRIPTION, PRICE, WOOD_TYPE,
                DIMENSIONS, CATEGORY, STOCK, IMAGE_URL);
    }

    private Furniture buildFurniture() {
        return Furniture.builder()
                .id(FURNITURE_ID)
                .name(FURNITURE_NAME)
                .description(DESCRIPTION)
                .price(PRICE)
                .woodType(WOOD_TYPE)
                .dimensions(DIMENSIONS)
                .category(CATEGORY)
                .stockQuantity(STOCK)
                .active(true)
                .imageUrl(IMAGE_URL)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
