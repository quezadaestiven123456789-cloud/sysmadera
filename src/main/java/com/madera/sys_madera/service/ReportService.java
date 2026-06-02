package com.madera.sys_madera.service;

import java.time.LocalDate;
import java.util.Map;

public interface ReportService {

    Map<String, Object> getSalesReport(LocalDate startDate, LocalDate endDate);

    Map<String, Object> getInventoryReport();

    Map<String, Object> getTopSellingFurniture();

}
