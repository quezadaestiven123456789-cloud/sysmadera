package com.madera.sys_madera.dto.response;

import java.math.BigDecimal;

public record WoodInventoryReportItem(
                String woodType,
                BigDecimal totalQuantity,
                String unit,
                BigDecimal totalValue) {
}
