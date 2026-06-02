package com.madera.sys_madera.repository;

import com.madera.sys_madera.dto.response.WoodInventoryReportItem;
import com.madera.sys_madera.model.WoodInventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface WoodInventoryRepository extends JpaRepository<WoodInventory, Long> {

    Optional<WoodInventory> findByWoodTypeIgnoreCase(String woodType);

    Page<WoodInventory> findByWoodTypeContainingIgnoreCase(String woodType, Pageable pageable);

    List<WoodInventory> findByQuantityLessThan(BigDecimal minimumStock);

    List<WoodInventory> findBySupplierContainingIgnoreCase(String supplier);

    @Query("SELECT new com.madera.sys_madera.dto.response.WoodInventoryReportItem(" +
            "wi.woodType, SUM(wi.quantity), wi.unit, SUM(wi.quantity * wi.unitPrice)) " +
            "FROM WoodInventory wi " +
            "GROUP BY wi.woodType, wi.unit " +
            "ORDER BY wi.woodType ASC")
    List<WoodInventoryReportItem> findInventoryGroupedByType();

}
