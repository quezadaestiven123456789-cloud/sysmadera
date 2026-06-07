package com.madera.sys_madera.repository;

import com.madera.sys_madera.config.JpaConfig;
import com.madera.sys_madera.model.Furniture;
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
@DisplayName("FurnitureRepository")
class FurnitureRepositoryTest {

    @Autowired
    private FurnitureRepository furnitureRepository;

    private Furniture mesaRoble;
    private Furniture sillaPino;
    private Furniture armarioCaoba;

    @BeforeEach
    void setUp() {
        furnitureRepository.deleteAll();

        mesaRoble = Furniture.builder()
                .name("Mesa Roble")
                .description("Mesa de comedor de roble macizo")
                .price(new BigDecimal("350.00"))
                .woodType("Roble")
                .dimensions("200x90x75")
                .category("Mesas")
                .stockQuantity(10)
                .active(true)
                .build();

        sillaPino = Furniture.builder()
                .name("Silla Pino")
                .description("Silla rústica de pino")
                .price(new BigDecimal("85.00"))
                .woodType("Pino")
                .dimensions("45x45x90")
                .category("Sillas")
                .stockQuantity(25)
                .active(true)
                .build();

        armarioCaoba = Furniture.builder()
                .name("Armario Caoba")
                .description("Armario clásico de caoba")
                .price(new BigDecimal("1200.00"))
                .woodType("Caoba")
                .dimensions("120x60x200")
                .category("Armarios")
                .stockQuantity(0)
                .active(false)
                .build();
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("should persist furniture with generated id and timestamps")
        void shouldSaveFurniture() {
            Furniture saved = furnitureRepository.save(mesaRoble);

            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("Mesa Roble");
            assertThat(saved.getPrice()).isEqualByComparingTo(new BigDecimal("350.00"));
            assertThat(saved.getStockQuantity()).isEqualTo(10);
            assertThat(saved.getActive()).isTrue();
            assertThat(saved.getCreatedAt()).isNotNull();
            assertThat(saved.getUpdatedAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("should return furniture when exists")
        void shouldFindById() {
            Furniture saved = furnitureRepository.save(mesaRoble);

            Optional<Furniture> found = furnitureRepository.findById(saved.getId());

            assertThat(found).isPresent();
            assertThat(found.get().getName()).isEqualTo("Mesa Roble");
        }

        @Test
        @DisplayName("should return empty when not found")
        void shouldReturnEmpty_whenNotFound() {
            Optional<Furniture> found = furnitureRepository.findById(999L);

            assertThat(found).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByNameContainingIgnoreCase")
    class FindByNameContainingIgnoreCase {

        @Test
        @DisplayName("should return matching furniture ignoring case")
        void shouldFindByNameContainingIgnoreCase() {
            furnitureRepository.save(mesaRoble);
            furnitureRepository.save(sillaPino);

            Page<Furniture> result = furnitureRepository
                    .findByNameContainingIgnoreCase("mesa", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Mesa Roble");
        }

        @Test
        @DisplayName("should return empty page when no match")
        void shouldReturnEmpty_whenNoMatch() {
            furnitureRepository.save(mesaRoble);

            Page<Furniture> result = furnitureRepository
                    .findByNameContainingIgnoreCase("xyz", PageRequest.of(0, 10));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should match regardless of case")
        void shouldMatchCaseInsensitive() {
            furnitureRepository.save(mesaRoble);

            Page<Furniture> result = furnitureRepository
                    .findByNameContainingIgnoreCase("MESA", PageRequest.of(0, 10));

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("should support pagination")
        void shouldSupportPagination() {
            furnitureRepository.save(mesaRoble);
            furnitureRepository.save(sillaPino);
            furnitureRepository.save(armarioCaoba);

            Page<Furniture> firstPage = furnitureRepository
                    .findByNameContainingIgnoreCase("a", PageRequest.of(0, 2));

            assertThat(firstPage.getContent()).hasSize(2);
            assertThat(firstPage.getTotalElements()).isEqualTo(3);
            assertThat(firstPage.getTotalPages()).isEqualTo(2);
        }
    }

    @Nested
    @DisplayName("findByCategoryIgnoreCase")
    class FindByCategoryIgnoreCase {

        @Test
        @DisplayName("should return furniture filtered by category ignoring case")
        void shouldFindByCategoryIgnoreCase() {
            furnitureRepository.save(mesaRoble);
            furnitureRepository.save(sillaPino);

            Page<Furniture> result = furnitureRepository
                    .findByCategoryIgnoreCase("sillas", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Silla Pino");
        }

        @Test
        @DisplayName("should return empty page when category does not exist")
        void shouldReturnEmpty_whenCategoryNotFound() {
            furnitureRepository.save(mesaRoble);

            Page<Furniture> result = furnitureRepository
                    .findByCategoryIgnoreCase("inexistente", PageRequest.of(0, 10));

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should match category regardless of case")
        void shouldMatchCaseInsensitive() {
            furnitureRepository.save(mesaRoble);

            Page<Furniture> result = furnitureRepository
                    .findByCategoryIgnoreCase("MESAS", PageRequest.of(0, 10));

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("findByActiveTrue")
    class FindByActiveTrue {

        @Test
        @DisplayName("should return only active furniture")
        void shouldFindByActiveTrue() {
            furnitureRepository.save(mesaRoble);
            furnitureRepository.save(armarioCaoba);

            Page<Furniture> result = furnitureRepository
                    .findByActiveTrue(PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Mesa Roble");
            assertThat(result.getContent().get(0).getActive()).isTrue();
        }

        @Test
        @DisplayName("should return empty page when no active furniture")
        void shouldReturnEmpty_whenNoneActive() {
            furnitureRepository.save(armarioCaoba);

            Page<Furniture> result = furnitureRepository
                    .findByActiveTrue(PageRequest.of(0, 10));

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findByStockQuantityLessThan")
    class FindByStockQuantityLessThan {

        @Test
        @DisplayName("should return furniture with low stock")
        void shouldFindByStockQuantityLessThan() {
            furnitureRepository.save(mesaRoble);
            furnitureRepository.save(sillaPino);

            List<Furniture> result = furnitureRepository.findByStockQuantityLessThan(15);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getName()).isEqualTo("Mesa Roble");
        }

        @Test
        @DisplayName("should return empty list when no furniture below threshold")
        void shouldReturnEmpty_whenNoneBelowThreshold() {
            furnitureRepository.save(mesaRoble);
            furnitureRepository.save(sillaPino);

            List<Furniture> result = furnitureRepository.findByStockQuantityLessThan(5);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("should include items with zero stock")
        void shouldIncludeZeroStock() {
            furnitureRepository.save(mesaRoble);
            furnitureRepository.save(armarioCaoba);

            List<Furniture> result = furnitureRepository.findByStockQuantityLessThan(5);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStockQuantity()).isZero();
        }
    }

    @Nested
    @DisplayName("existsByName")
    class ExistsByName {

        @Test
        @DisplayName("should return true when name exists")
        void shouldReturnTrue_whenNameExists() {
            furnitureRepository.save(mesaRoble);

            boolean exists = furnitureRepository.existsByName("Mesa Roble");

            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("should return false when name does not exist")
        void shouldReturnFalse_whenNameNotExists() {
            boolean exists = furnitureRepository.existsByName("No Existe");

            assertThat(exists).isFalse();
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {

        @Test
        @DisplayName("should remove furniture from database")
        void shouldDeleteFurniture() {
            Furniture saved = furnitureRepository.save(mesaRoble);

            furnitureRepository.deleteById(saved.getId());

            assertThat(furnitureRepository.findById(saved.getId())).isEmpty();
        }
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        @DisplayName("should reflect changes after save with same id")
        void shouldUpdateFurniture() {
            Furniture saved = furnitureRepository.save(mesaRoble);

            saved.setName("Mesa Roble Premium");
            saved.setPrice(new BigDecimal("450.00"));
            furnitureRepository.save(saved);

            Furniture updated = furnitureRepository.findById(saved.getId()).orElseThrow();
            assertThat(updated.getName()).isEqualTo("Mesa Roble Premium");
            assertThat(updated.getPrice()).isEqualByComparingTo(new BigDecimal("450.00"));
        }
    }

    @Nested
    @DisplayName("softDelete")
    class SoftDelete {

        @Test
        @DisplayName("should keep record physically in database")
        void shouldKeepRecordInDatabase() {
            Furniture saved = furnitureRepository.save(mesaRoble);
            Long id = saved.getId();

            saved.setActive(false);
            furnitureRepository.save(saved);

            assertThat(furnitureRepository.findById(id)).isPresent();
        }

        @Test
        @DisplayName("findAll should include soft-deleted records")
        void findAllShouldIncludeInactive() {
            furnitureRepository.save(mesaRoble);
            furnitureRepository.save(armarioCaoba);

            List<Furniture> all = furnitureRepository.findAll();

            assertThat(all).hasSize(2);
        }

        @Test
        @DisplayName("findByNameContainingIgnoreCase should include inactive records")
        void searchShouldIncludeInactive() {
            furnitureRepository.save(mesaRoble);
            furnitureRepository.save(armarioCaoba);

            Page<Furniture> result = furnitureRepository
                    .findByNameContainingIgnoreCase("a", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(2);
        }

        @Test
        @DisplayName("findByCategoryIgnoreCase should include inactive records")
        void categoryFilterShouldIncludeInactive() {
            furnitureRepository.save(armarioCaoba);

            Page<Furniture> result = furnitureRepository
                    .findByCategoryIgnoreCase("armarios", PageRequest.of(0, 10));

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getActive()).isFalse();
        }
    }
}
