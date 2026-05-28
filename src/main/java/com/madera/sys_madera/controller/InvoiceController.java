package com.madera.sys_madera.controller;

import com.madera.sys_madera.dto.request.InvoiceRequest;
import com.madera.sys_madera.dto.response.InvoiceResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.service.InvoiceService;
import com.madera.sys_madera.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/facturas")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<InvoiceResponse> create(@Valid @RequestBody InvoiceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(invoiceService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<InvoiceResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(invoiceService.findById(id));
    }

    @GetMapping("/orden/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<InvoiceResponse> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(invoiceService.findByOrderId(orderId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<PagedResponse<InvoiceResponse>> findAll(
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT) String sort,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_DIR) String direction,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(invoiceService.findAll(page, size, sort, direction, status));
    }

    @PostMapping("/{id}/pago")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<InvoiceResponse> registerPayment(@PathVariable Long id,
                                                            @RequestParam BigDecimal amount) {
        return ResponseEntity.ok(invoiceService.registerPayment(id, amount));
    }

}
