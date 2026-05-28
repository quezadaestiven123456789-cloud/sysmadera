package com.madera.sys_madera.controller;

import com.madera.sys_madera.dto.request.WoodInventoryRequest;
import com.madera.sys_madera.dto.response.MessageResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.dto.response.WoodInventoryResponse;
import com.madera.sys_madera.service.WoodInventoryService;
import com.madera.sys_madera.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/inventario-madera")
@RequiredArgsConstructor
public class WoodInventoryController {

    private final WoodInventoryService woodInventoryService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<WoodInventoryResponse> create(@Valid @RequestBody WoodInventoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(woodInventoryService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<WoodInventoryResponse> update(@PathVariable Long id,
                                                         @Valid @RequestBody WoodInventoryRequest request) {
        return ResponseEntity.ok(woodInventoryService.update(id, request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<WoodInventoryResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(woodInventoryService.findById(id));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<PagedResponse<WoodInventoryResponse>> findAll(
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT) String sort,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_DIR) String direction,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(woodInventoryService.findAll(page, size, sort, direction, search));
    }

    @GetMapping("/bajo-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<List<WoodInventoryResponse>> findLowStock() {
        return ResponseEntity.ok(woodInventoryService.findLowStock());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        woodInventoryService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Registro de inventario eliminado exitosamente"));
    }

}
