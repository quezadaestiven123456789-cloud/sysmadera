package com.madera.sys_madera.repository;

import com.madera.sys_madera.dto.response.TopSellingFurnitureItem;
import com.madera.sys_madera.model.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    List<OrderDetail> findByOrderId(Long orderId);

    List<OrderDetail> findByFurnitureId(Long furnitureId);

    @Query("SELECT new com.madera.sys_madera.dto.response.TopSellingFurnitureItem(" +
            "od.furniture.id, od.furniture.name, SUM(od.quantity), SUM(od.subtotal)) " +
            "FROM OrderDetail od " +
            "GROUP BY od.furniture.id, od.furniture.name " +
            "ORDER BY SUM(od.quantity) DESC")
    List<TopSellingFurnitureItem> findTopSellingFurniture();

}
