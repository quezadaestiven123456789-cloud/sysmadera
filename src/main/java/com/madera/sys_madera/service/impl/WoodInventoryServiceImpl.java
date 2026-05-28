package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.WoodInventoryRequest;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.dto.response.WoodInventoryResponse;
import com.madera.sys_madera.exception.DuplicateResourceException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.model.WoodInventory;
import com.madera.sys_madera.repository.WoodInventoryRepository;
import com.madera.sys_madera.service.WoodInventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WoodInventoryServiceImpl implements WoodInventoryService {

    private final WoodInventoryRepository woodInventoryRepository;

    @Override
    @Transactional
    public WoodInventoryResponse create(WoodInventoryRequest request) {
        woodInventoryRepository.findByWoodTypeIgnoreCase(request.woodType())
                .ifPresent(w -> {
                    throw new DuplicateResourceException(
                            "El tipo de madera '" + request.woodType() + "' ya existe en el inventario");
                });

        WoodInventory item = WoodInventory.builder()
                .woodType(request.woodType())
                .quantity(request.quantity())
                .unit(request.unit())
                .unitPrice(request.unitPrice())
                .supplier(request.supplier())
                .description(request.description())
                .minimumStock(request.minimumStock())
                .build();

        item = woodInventoryRepository.save(item);
        return toResponse(item);
    }

    @Override
    @Transactional
    public WoodInventoryResponse update(Long id, WoodInventoryRequest request) {
        WoodInventory item = woodInventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", "id", id));

        woodInventoryRepository.findByWoodTypeIgnoreCase(request.woodType())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new DuplicateResourceException(
                                "El tipo de madera '" + request.woodType() + "' ya existe");
                    }
                });

        item.setWoodType(request.woodType());
        item.setQuantity(request.quantity());
        item.setUnit(request.unit());
        item.setUnitPrice(request.unitPrice());
        item.setSupplier(request.supplier());
        item.setDescription(request.description());
        item.setMinimumStock(request.minimumStock());

        item = woodInventoryRepository.save(item);
        return toResponse(item);
    }

    @Override
    public WoodInventoryResponse findById(Long id) {
        WoodInventory item = woodInventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", "id", id));
        return toResponse(item);
    }

    @Override
    public PagedResponse<WoodInventoryResponse> findAll(int page, int size, String sort, String direction, String search) {
        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<WoodInventory> items;
        if (search != null && !search.isEmpty()) {
            items = woodInventoryRepository.findByWoodTypeContainingIgnoreCase(search, pageable);
        } else {
            items = woodInventoryRepository.findAll(pageable);
        }

        List<WoodInventoryResponse> content = items.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                items.getNumber(),
                items.getSize(),
                items.getTotalElements(),
                items.getTotalPages(),
                items.isLast()
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        WoodInventory item = woodInventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventario", "id", id));
        woodInventoryRepository.delete(item);
    }

    @Override
    public List<WoodInventoryResponse> findLowStock() {
        return woodInventoryRepository.findAll().stream()
                .filter(item -> item.getMinimumStock() != null
                        && item.getQuantity().compareTo(item.getMinimumStock()) < 0)
                .map(this::toResponse)
                .toList();
    }

    private WoodInventoryResponse toResponse(WoodInventory item) {
        BigDecimal totalValue = item.getQuantity().multiply(item.getUnitPrice());
        boolean lowStock = item.getMinimumStock() != null
                && item.getQuantity().compareTo(item.getMinimumStock()) < 0;

        return new WoodInventoryResponse(
                item.getId(),
                item.getWoodType(),
                item.getQuantity(),
                item.getUnit(),
                item.getUnitPrice(),
                totalValue,
                item.getSupplier(),
                item.getDescription(),
                item.getMinimumStock(),
                lowStock,
                item.getCreatedAt(),
                item.getUpdatedAt()
        );
    }

}
