package com.madera.sys_madera.dto.response;

import java.math.BigDecimal;

public record TopSellingFurnitureItem(
                Long furnitureId,
                String furnitureName,
                Long totalSold,
                BigDecimal totalRevenue) {
}
