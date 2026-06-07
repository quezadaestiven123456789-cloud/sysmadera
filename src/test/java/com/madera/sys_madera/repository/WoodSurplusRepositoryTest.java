package com.madera.sys_madera.repository;

import com.madera.sys_madera.config.JpaConfig;
import com.madera.sys_madera.model.WoodInventory;
import com.madera.sys_madera.model.WoodSurplus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("WoodSurplusRepository")
class WoodSurplusRepositoryTest {

    @Autowired
    private WoodSurplusRepository woodSurplusRepository;

    @Autowired
    private WoodInventoryRepository woodInventoryRepository;

    private WoodSurplus robleSurplus;
    private WoodSurplus pinoSurplus;
    private WoodSurplus caobaSurplus;
    private WoodInventory inventory;

    @BeforeEach
    void setUp() {
        woodSurplusRepository.deleteAll();
        woodInventoryRepository.deleteAll();

        inventory = WoodInventory.builder()
                .woodType("Roble")
                .quantity(new BigDecimal("200.00"))
                .unit("m³")
                .unitPrice(new BigDecimal("850.00"))
                .build();
        inventory = woodInventoryRepository.save(inventory);

        robleSurplus = WoodSurplus.builder()
                .woodType("Roble")
                .quantity(new BigDecimal("15.50"))
                .unit("m²")
                .dimensions("2.5x1.0")
                .description("Sobrante de roble nacional")
                .available(true)
                .woodInventory(inventory)
                .build();

        pinoSurplus = WoodSurplus.builder()
                .woodType("Pino")
                .quantity(new BigDecimal("8.00"))
                .unit("m²")
                .available(true)
                .build();

        caobaSurplus = WoodSurplus.builder()
                .woodType("Caoba")
                .quantity(new BigDecimal("3.25"))
                .unit("m²")
                .available(false)
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist surplus with generated id and timestamps")
        void shouldSaveWoodSurplus() {
            WoodSurplus saved = woodSurplusRepository.save(robleSurplus);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getWoodType()).isEqualTo("Roble");
            assertThat(saved.getQuantity()).isEqualByComparingTo(new BigDecimal("15.50"));
            assertThat(saved.getUnit()).isEqualTo("m²");
            assertThat(saved.getDimensions()).isEqualTo("2.5x1.0");
            assertThat(saved.getDescription()).isEqualTo("Sobrante de roble nacional");
            assertThat(saved.getAvailable()).isTrue();
            assertThat(saved.getWoodInventory()).isNotNull();
            assertThat(saved.getWoodInventory().getId()).isEqualTo(inventory.getId());
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("should persist surplus with null optional fields")
        void shouldSaveWithNullOptionalFields() {
            WoodSurplus saved = woodSurplusRepository.save(pinoSurplus);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getWoodType()).isEqualTo("Pino");
            assertThat(saved.getDimensions()).isNull();
            assertThat(saved.getDescription()).isNull();
            assertThat(saved.getWoodInventory()).isNull();
            assertThat(saved.getAvailable()).isTrue();
        }

        @Test
        @DisplayName("should persist surplus as unavailable")
        void shouldSaveUnavailableSurplus() {
            WoodSurplus saved = woodSurplusRepository.save(caobaSurplus);

            assertThat(saved.getAvailable()).isFalse();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return surplus when exists")
        void shouldFindById() {
            WoodSurplus saved = woodSurplusRepository.save(robleSurplus);

            Optional<WoodSurplus> found = woodSurplusRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getWoodType()).isEqualTo("Roble");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty_whenNotFound() {
            Optional<WoodSurplus> found = woodSurplusRepository.findById(999L);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByAvailableTrue")
    class FindByAvailableTrue {

        @Test
        @DisplayName("should return only available surpluses")
        void shouldFindByAvailableTrue() {
            woodSurplusRepository.save(robleSurplus);
            woodSurplusRepository.save(pinoSurplus);
            woodSurplusRepository.save(caobaSurplus);

            List<WoodSurplus> result = woodSurplusRepository.findByAvailableTrue();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(WoodSurplus::getWoodType)
                    .containsExactlyInAnyOrder("Roble", "Pino");
        }

        @Test
        @DisplayName("should return empty when no available surpluses")
        void shouldReturnEmpty_whenNoneAvailable() {
            woodSurplusRepository.save(caobaSurplus);

            List<WoodSurplus> result = woodSurplusRepository.findByAvailableTrue();

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when no surpluses exist")
        void shouldReturnEmpty_whenNoData() {
            List<WoodSurplus> result = woodSurplusRepository.findByAvailableTrue();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByWoodTypeContainingIgnoreCase")
    class FindByWoodTypeContainingIgnoreCase {

        @Test
        @DisplayName("should match partial wood type ignoring case")
        void shouldFindByWoodTypeContainingIgnoreCase() {
            woodSurplusRepository.save(robleSurplus);
            woodSurplusRepository.save(pinoSurplus);

            List<WoodSurplus> result = woodSurplusRepository.findByWoodTypeContainingIgnoreCase("ob");

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getWoodType()).isEqualTo("Roble");
        }

        @Test
        @DisplayName("should be case insensitive")
        void shouldMatchCaseInsensitive() {
            woodSurplusRepository.save(robleSurplus);

            List<WoodSurplus> result = woodSurplusRepository.findByWoodTypeContainingIgnoreCase("ROBLE");

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should return empty when no match")
        void shouldReturnEmpty_whenNoMatch() {
            woodSurplusRepository.save(robleSurplus);

            List<WoodSurplus> result = woodSurplusRepository.findByWoodTypeContainingIgnoreCase("xyz");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return all when empty search term")
        void shouldReturnAll_whenEmptySearch() {
            woodSurplusRepository.save(robleSurplus);
            woodSurplusRepository.save(pinoSurplus);

            List<WoodSurplus> result = woodSurplusRepository.findByWoodTypeContainingIgnoreCase("");

            assertThat(result).hasSize(2);
        }
    }

    @Nested
    @DisplayName("findByWoodInventoryId")
    class FindByWoodInventoryId {

        @Test
        @DisplayName("should return surpluses for a given inventory")
        void shouldFindByWoodInventoryId() {
            woodSurplusRepository.save(robleSurplus);
            woodSurplusRepository.save(pinoSurplus);

            List<WoodSurplus> result = woodSurplusRepository.findByWoodInventoryId(inventory.getId());

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getWoodType()).isEqualTo("Roble");
        }

        @Test
        @DisplayName("should return empty when inventory has no surpluses")
        void shouldReturnEmpty_whenNoSurpluses() {
            woodSurplusRepository.save(pinoSurplus);

            List<WoodSurplus> result = woodSurplusRepository.findByWoodInventoryId(inventory.getId());

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should return empty when inventory does not exist")
        void shouldReturnEmpty_whenInventoryNotFound() {
            List<WoodSurplus> result = woodSurplusRepository.findByWoodInventoryId(999L);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should remove surplus from database")
        void shouldDeleteWoodSurplus() {
            WoodSurplus saved = woodSurplusRepository.save(robleSurplus);

            woodSurplusRepository.deleteById(saved.getId());

            assertThat(woodSurplusRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should reflect changes after save with same id")
        void shouldUpdateWoodSurplus() {
            WoodSurplus saved = woodSurplusRepository.save(robleSurplus);

            saved.setQuantity(new BigDecimal("20.00"));
            saved.setDescription("Actualizado");
            saved.setAvailable(false);
            woodSurplusRepository.save(saved);

            WoodSurplus updated = woodSurplusRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getQuantity()).isEqualByComparingTo(new BigDecimal("20.00"));
            assertThat(updated.getDescription()).isEqualTo("Actualizado");
            assertThat(updated.getAvailable()).isFalse();
        }
    }
}
