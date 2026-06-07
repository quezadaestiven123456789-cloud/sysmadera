package com.madera.sys_madera.repository;

import com.madera.sys_madera.config.JpaConfig;
import com.madera.sys_madera.dto.response.WoodInventoryReportItem;
import com.madera.sys_madera.model.WoodInventory;
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
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("WoodInventoryRepository")
class WoodInventoryRepositoryTest {

    @Autowired
    private WoodInventoryRepository woodInventoryRepository;

    private WoodInventory roble;
    private WoodInventory pino;
    private WoodInventory caoba;

    @BeforeEach
    void setUp() {
        woodInventoryRepository.deleteAll();

        roble = WoodInventory.builder()
                .woodType("Roble")
                .quantity(new BigDecimal("150.00"))
                .unit("m³")
                .unitPrice(new BigDecimal("850.00"))
                .supplier("Maderas del Norte")
                .description("Roble nacional de primera calidad")
                .minimumStock(new BigDecimal("20.00"))
                .build();

        pino = WoodInventory.builder()
                .woodType("Pino")
                .quantity(new BigDecimal("300.00"))
                .unit("m³")
                .unitPrice(new BigDecimal("350.00"))
                .supplier("Maderas del Sur")
                .description("Pino tratado para interiores")
                .minimumStock(new BigDecimal("50.00"))
                .build();

        caoba = WoodInventory.builder()
                .woodType("Caoba")
                .quantity(new BigDecimal("10.00"))
                .unit("m³")
                .unitPrice(new BigDecimal("1500.00"))
                .supplier("Maderas del Norte")
                .description("Caoba centroamericana")
                .minimumStock(new BigDecimal("15.00"))
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist wood inventory with generated id and timestamps")
        void shouldSaveWoodInventory() {
            WoodInventory saved = woodInventoryRepository.save(roble);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getWoodType()).isEqualTo("Roble");
            assertThat(saved.getQuantity()).isEqualByComparingTo(new BigDecimal("150.00"));
            assertThat(saved.getUnit()).isEqualTo("m³");
            assertThat(saved.getUnitPrice()).isEqualByComparingTo(new BigDecimal("850.00"));
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return wood inventory when exists")
        void shouldFindById() {
            WoodInventory saved = woodInventoryRepository.save(roble);

            Optional<WoodInventory> found = woodInventoryRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getWoodType()).isEqualTo("Roble");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty_whenNotFound() {
            Optional<WoodInventory> found = woodInventoryRepository.findById(999L);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByWoodTypeIgnoreCase")
    class FindByWoodTypeIgnoreCase {

        @Test
        @DisplayName("should return wood inventory when wood type exists")
        void shouldFindByWoodTypeIgnoreCase() {
            woodInventoryRepository.save(roble);

            Optional<WoodInventory> found = woodInventoryRepository.findByWoodTypeIgnoreCase("roble");

            assertThat(found).isPresent();
            assertThat(found.get().getSupplier()).isEqualTo("Maderas del Norte");
        }

        @Test
        @DisplayName("should return empty when wood type does not exist")
        void shouldReturnEmpty_whenWoodTypeNotFound() {
            Optional<WoodInventory> found = woodInventoryRepository.findByWoodTypeIgnoreCase("Cedro");

            assertThat(found).isEmpty();
        }

        @Test
        @DisplayName("should match regardless of case")
        void shouldMatchCaseInsensitive() {
            woodInventoryRepository.save(roble);

            Optional<WoodInventory> found = woodInventoryRepository.findByWoodTypeIgnoreCase("ROBLE");

            assertThat(found).isPresent();
        }
    }

    @Nested
    @DisplayName("findByWoodTypeContainingIgnoreCase")
    class FindByWoodTypeContainingIgnoreCase {

        @Test
        @DisplayName("should return matching wood types ignoring case")
        void shouldFindByWoodTypeContainingIgnoreCase() {
            woodInventoryRepository.save(roble);
            woodInventoryRepository.save(pino);

            Page<WoodInventory> result = woodInventoryRepository
                    .findByWoodTypeContainingIgnoreCase("ob", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getWoodType()).isEqualTo("Roble");
        }

        @Test
        @DisplayName("should return empty page when no match")
        void shouldReturnEmpty_whenNoMatch() {
            woodInventoryRepository.save(roble);

            Page<WoodInventory> result = woodInventoryRepository
                    .findByWoodTypeContainingIgnoreCase("xyz", PageRequest.of(0, 10));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should support pagination")
        void shouldSupportPagination() {
            woodInventoryRepository.save(roble);
            woodInventoryRepository.save(pino);
            woodInventoryRepository.save(caoba);

            Page<WoodInventory> result = woodInventoryRepository
                    .findByWoodTypeContainingIgnoreCase("o", PageRequest.of(0, 2));

            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("findByQuantityLessThan")
    class FindByQuantityLessThan {

        @Test
        @DisplayName("should return inventory below minimum stock")
        void shouldFindByQuantityLessThan() {
            woodInventoryRepository.save(roble);
            woodInventoryRepository.save(caoba);

            List<WoodInventory> result = woodInventoryRepository.findByQuantityLessThan(new BigDecimal("50.00"));

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getWoodType()).isEqualTo("Caoba");
        }

        @Test
        @DisplayName("should return empty list when none below threshold")
        void shouldReturnEmpty_whenNoneBelowThreshold() {
            woodInventoryRepository.save(roble);
            woodInventoryRepository.save(pino);

            List<WoodInventory> result = woodInventoryRepository.findByQuantityLessThan(new BigDecimal("5.00"));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findBySupplierContainingIgnoreCase")
    class FindBySupplierContainingIgnoreCase {

        @Test
        @DisplayName("should return inventory filtered by supplier")
        void shouldFindBySupplierContainingIgnoreCase() {
            woodInventoryRepository.save(roble);
            woodInventoryRepository.save(pino);
            woodInventoryRepository.save(caoba);

            List<WoodInventory> result = woodInventoryRepository.findBySupplierContainingIgnoreCase("Norte");

            assertThat(result).hasSize(2);
            assertThat(result).extracting(WoodInventory::getWoodType)
                    .containsExactlyInAnyOrder("Roble", "Caoba");
        }

        @Test
        @DisplayName("should return empty list when supplier does not exist")
        void shouldReturnEmpty_whenSupplierNotFound() {
            woodInventoryRepository.save(roble);

            List<WoodInventory> result = woodInventoryRepository.findBySupplierContainingIgnoreCase("Inexistente");

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should match regardless of case")
        void shouldMatchCaseInsensitive() {
            woodInventoryRepository.save(roble);

            List<WoodInventory> result = woodInventoryRepository.findBySupplierContainingIgnoreCase("norte");

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findInventoryGroupedByType")
    class FindInventoryGroupedByType {

        @Test
        @DisplayName("should return inventory grouped by wood type")
        void shouldFindInventoryGroupedByType() {
            woodInventoryRepository.save(roble);
            woodInventoryRepository.save(pino);

            var segundoRoble = WoodInventory.builder()
                    .woodType("Roble")
                    .quantity(new BigDecimal("50.00"))
                    .unit("m³")
                    .unitPrice(new BigDecimal("900.00"))
                    .supplier("Otro Proveedor")
                    .build();
            woodInventoryRepository.save(segundoRoble);

            List<WoodInventoryReportItem> result = woodInventoryRepository.findInventoryGroupedByType();

            assertThat(result).hasSize(2);
            assertThat(result).extracting(WoodInventoryReportItem::woodType)
                    .containsExactly("Pino", "Roble");
            assertThat(result.get(1).totalQuantity())
                    .isEqualByComparingTo(new BigDecimal("200.00"));
        }

        @Test
        @DisplayName("should return empty list when no inventory")
        void shouldReturnEmpty_whenNoInventory() {
            List<WoodInventoryReportItem> result = woodInventoryRepository.findInventoryGroupedByType();

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should remove wood inventory from database")
        void shouldDeleteWoodInventory() {
            WoodInventory saved = woodInventoryRepository.save(roble);

            woodInventoryRepository.deleteById(saved.getId());

            assertThat(woodInventoryRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should reflect changes after save with same id")
        void shouldUpdateWoodInventory() {
            WoodInventory saved = woodInventoryRepository.save(roble);

            saved.setQuantity(new BigDecimal("200.00"));
            saved.setUnitPrice(new BigDecimal("900.00"));
            woodInventoryRepository.save(saved);

            WoodInventory updated = woodInventoryRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getQuantity()).isEqualByComparingTo(new BigDecimal("200.00"));
            assertThat(updated.getUnitPrice()).isEqualByComparingTo(new BigDecimal("900.00"));
        }
    }
}
