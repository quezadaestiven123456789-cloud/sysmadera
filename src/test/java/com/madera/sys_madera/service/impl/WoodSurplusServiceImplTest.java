package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.model.WoodInventory;
import com.madera.sys_madera.model.WoodSurplus;
import com.madera.sys_madera.repository.WoodSurplusRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("WoodSurplusServiceImpl")
class WoodSurplusServiceImplTest {

    @Mock
    private WoodSurplusRepository woodSurplusRepository;

    @InjectMocks
    private WoodSurplusServiceImpl woodSurplusService;

    private static final String WOOD_TYPE = "Roble";
    private static final BigDecimal QUANTITY = new BigDecimal("15.50");
    private static final String UNIT = "m²";

    private WoodSurplus createSurplus(Long id, String woodType, BigDecimal quantity, boolean available) {
        return WoodSurplus.builder()
                .id(id)
                .woodType(woodType)
                .quantity(quantity)
                .unit(UNIT)
                .available(available)
                .build();
    }

    @Nested
    @DisplayName("create")
    class Create {

        @Test
        @DisplayName("should save and return surplus when request is valid")
        void shouldCreate_whenValid() {
            var surplus = WoodSurplus.builder()
                    .woodType(WOOD_TYPE)
                    .quantity(QUANTITY)
                    .unit(UNIT)
                    .dimensions("2.5x1.0")
                    .description("Sobrante de roble")
                    .available(true)
                    .woodInventory(WoodInventory.builder().id(1L).build())
                    .build();

            var savedSurplus = createSurplus(1L, WOOD_TYPE, QUANTITY, true);

            given(woodSurplusRepository.save(any(WoodSurplus.class))).willReturn(savedSurplus);

            WoodSurplus result = woodSurplusService.create(surplus);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getWoodType()).isEqualTo(WOOD_TYPE);
            assertThat(result.getQuantity()).isEqualByComparingTo(QUANTITY);
            assertThat(result.getAvailable()).isTrue();

            ArgumentCaptor<WoodSurplus> captor = ArgumentCaptor.forClass(WoodSurplus.class);
            verify(woodSurplusRepository).save(captor.capture());
            assertThat(captor.getValue().getWoodType()).isEqualTo(WOOD_TYPE);
        }

        @Test
        @DisplayName("should save surplus with minimal fields")
        void shouldCreate_withMinimalFields() {
            var surplus = WoodSurplus.builder()
                    .woodType("Pino")
                    .quantity(new BigDecimal("8.00"))
                    .unit("m²")
                    .build();

            var savedSurplus = WoodSurplus.builder()
                    .id(2L)
                    .woodType("Pino")
                    .quantity(new BigDecimal("8.00"))
                    .unit("m²")
                    .available(true)
                    .build();

            given(woodSurplusRepository.save(any(WoodSurplus.class))).willReturn(savedSurplus);

            WoodSurplus result = woodSurplusService.create(surplus);

            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getWoodType()).isEqualTo("Pino");
            assertThat(result.getDimensions()).isNull();
            assertThat(result.getDescription()).isNull();
            assertThat(result.getWoodInventory()).isNull();
            assertThat(result.getAvailable()).isTrue();
        }

        @Test
        @DisplayName("should save surplus with zero quantity")
        void shouldCreate_withZeroQuantity() {
            var surplus = WoodSurplus.builder()
                    .woodType("Cedro")
                    .quantity(BigDecimal.ZERO)
                    .unit("m²")
                    .build();

            var savedSurplus = WoodSurplus.builder()
                    .id(3L)
                    .woodType("Cedro")
                    .quantity(BigDecimal.ZERO)
                    .unit("m²")
                    .available(true)
                    .build();

            given(woodSurplusRepository.save(any(WoodSurplus.class))).willReturn(savedSurplus);

            WoodSurplus result = woodSurplusService.create(surplus);

            assertThat(result.getQuantity()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("should save surplus as unavailable")
        void shouldCreate_unavailable() {
            var surplus = WoodSurplus.builder()
                    .woodType("Caoba")
                    .quantity(new BigDecimal("3.25"))
                    .unit("m²")
                    .available(false)
                    .build();

            var savedSurplus = WoodSurplus.builder()
                    .id(4L)
                    .woodType("Caoba")
                    .quantity(new BigDecimal("3.25"))
                    .unit("m²")
                    .available(false)
                    .build();

            given(woodSurplusRepository.save(any(WoodSurplus.class))).willReturn(savedSurplus);

            WoodSurplus result = woodSurplusService.create(surplus);

            assertThat(result.getAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("findAllAvailable")
    class FindAllAvailable {

        @Test
        @DisplayName("should return all available surpluses")
        void shouldReturnAvailableSurpluses() {
            var roble = createSurplus(1L, "Roble", QUANTITY, true);
            var pino = createSurplus(2L, "Pino", new BigDecimal("8.00"), true);

            given(woodSurplusRepository.findByAvailableTrue()).willReturn(List.of(roble, pino));

            List<WoodSurplus> result = woodSurplusService.findAllAvailable();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(WoodSurplus::getWoodType)
                    .containsExactly("Roble", "Pino");
            verify(woodSurplusRepository).findByAvailableTrue();
        }

        @Test
        @DisplayName("should return empty when no available surpluses")
        void shouldReturnEmpty_whenNoneAvailable() {
            given(woodSurplusRepository.findByAvailableTrue()).willReturn(List.of());

            List<WoodSurplus> result = woodSurplusService.findAllAvailable();

            assertThat(result).isEmpty();
            verify(woodSurplusRepository).findByAvailableTrue();
        }
    }

    @Nested
    @DisplayName("findByWoodType")
    class FindByWoodType {

        @Test
        @DisplayName("should return surpluses matching wood type")
        void shouldFindByWoodType() {
            var roble = createSurplus(1L, "Roble", QUANTITY, true);

            given(woodSurplusRepository.findByWoodTypeContainingIgnoreCase("Roble"))
                    .willReturn(List.of(roble));

            List<WoodSurplus> result = woodSurplusService.findByWoodType("Roble");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getWoodType()).isEqualTo("Roble");
            verify(woodSurplusRepository).findByWoodTypeContainingIgnoreCase("Roble");
        }

        @Test
        @DisplayName("should be case insensitive")
        void shouldBeCaseInsensitive() {
            var roble = createSurplus(1L, "Roble", QUANTITY, true);

            given(woodSurplusRepository.findByWoodTypeContainingIgnoreCase("roble"))
                    .willReturn(List.of(roble));

            List<WoodSurplus> result = woodSurplusService.findByWoodType("roble");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmpty_whenNoMatch() {
            given(woodSurplusRepository.findByWoodTypeContainingIgnoreCase("Inexistente"))
                    .willReturn(List.of());

            List<WoodSurplus> result = woodSurplusService.findByWoodType("Inexistente");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return all when search term is empty")
        void shouldReturnAll_whenEmptySearch() {
            var roble = createSurplus(1L, "Roble", QUANTITY, true);
            var pino = createSurplus(2L, "Pino", new BigDecimal("8.00"), true);

            given(woodSurplusRepository.findByWoodTypeContainingIgnoreCase(""))
                    .willReturn(List.of(roble, pino));

            List<WoodSurplus> result = woodSurplusService.findByWoodType("");

            assertThat(result).hasSize(2);
        }
    }
}
