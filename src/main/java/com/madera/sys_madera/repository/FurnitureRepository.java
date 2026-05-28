package com.madera.sys_madera.repository;

import com.madera.sys_madera.model.Furniture;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FurnitureRepository extends JpaRepository<Furniture, Long> {

    Page<Furniture> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Furniture> findByCategoryIgnoreCase(String category, Pageable pageable);

    Page<Furniture> findByActiveTrue(Pageable pageable);

    List<Furniture> findByStockQuantityLessThan(Integer threshold);

    boolean existsByName(String name);

}
