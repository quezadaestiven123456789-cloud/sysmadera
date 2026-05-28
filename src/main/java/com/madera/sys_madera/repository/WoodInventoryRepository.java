package com.madera.sys_madera.repository;

import com.madera.sys_madera.model.WoodInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WoodInventoryRepository extends JpaRepository<WoodInventory, Long> {

    Optional<WoodInventory> findByWoodTypeIgnoreCase(String woodType);

    Page<WoodInventory> findByWoodTypeContainingIgnoreCase(String woodType, Pageable pageable);

    List<WoodInventory> findByQuantityLessThan(BigDecimal minimumStock);

    List<WoodInventory> findBySupplierContainingIgnoreCase(String supplier);

}
