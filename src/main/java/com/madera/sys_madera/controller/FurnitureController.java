package com.madera.sys_madera.controller;

import com.madera.sys_madera.dto.request.FurnitureRequest;
import com.madera.sys_madera.dto.response.FurnitureResponse;
import com.madera.sys_madera.dto.response.MessageResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.service.FurnitureService;
import com.madera.sys_madera.util.Constants;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/muebles")
@RequiredArgsConstructor
public class FurnitureController {

    private final FurnitureService furnitureService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<FurnitureResponse> create(@Valid @RequestBody FurnitureRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(furnitureService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'EMPLEADO')")
    public ResponseEntity<FurnitureResponse> update(@PathVariable Long id,
                                                     @Valid @RequestBody FurnitureRequest request) {
        return ResponseEntity.ok(furnitureService.update(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<FurnitureResponse> findById(@PathVariable Long id) {
        return ResponseEntity.ok(furnitureService.findById(id));
    }

    @GetMapping
    public ResponseEntity<PagedResponse<FurnitureResponse>> findAll(
            @RequestParam(defaultValue = Constants.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = Constants.DEFAULT_SIZE) int size,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT) String sort,
            @RequestParam(defaultValue = Constants.DEFAULT_SORT_DIR) String direction,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(furnitureService.findAll(page, size, sort, direction, search, category));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MessageResponse> delete(@PathVariable Long id) {
        furnitureService.delete(id);
        return ResponseEntity.ok(new MessageResponse("Mueble desactivado exitosamente"));
    }

}
