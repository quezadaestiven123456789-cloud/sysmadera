package com.madera.sys_madera.repository;

import com.madera.sys_madera.model.WoodSurplus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WoodSurplusRepository extends JpaRepository<WoodSurplus, Long> {

    List<WoodSurplus> findByAvailableTrue();

    List<WoodSurplus> findByWoodTypeContainingIgnoreCase(String woodType);

    List<WoodSurplus> findByWoodInventoryId(Long woodInventoryId);

}
