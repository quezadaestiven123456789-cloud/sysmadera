package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.dto.request.FurnitureRequest;
import com.madera.sys_madera.dto.response.FurnitureResponse;
import com.madera.sys_madera.dto.response.PagedResponse;
import com.madera.sys_madera.exception.DuplicateResourceException;
import com.madera.sys_madera.exception.ResourceNotFoundException;
import com.madera.sys_madera.model.Furniture;
import com.madera.sys_madera.repository.FurnitureRepository;
import com.madera.sys_madera.service.FurnitureService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FurnitureServiceImpl implements FurnitureService {

    private final FurnitureRepository furnitureRepository;

    @Override
    @Transactional
    public FurnitureResponse create(FurnitureRequest request) {
        if (furnitureRepository.existsByName(request.name())) {
            throw new DuplicateResourceException(
                    "El mueble '" + request.name() + "' ya existe");
        }

        Furniture furniture = Furniture.builder()
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .woodType(request.woodType())
                .dimensions(request.dimensions())
                .category(request.category())
                .stockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0)
                .imageUrl(request.imageUrl())
                .build();

        furniture = furnitureRepository.save(furniture);
        return toResponse(furniture);
    }

    @Override
    @Transactional
    public FurnitureResponse update(Long id, FurnitureRequest request) {
        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mueble", "id", id));

        furniture.setName(request.name());
        furniture.setDescription(request.description());
        furniture.setPrice(request.price());
        furniture.setWoodType(request.woodType());
        furniture.setDimensions(request.dimensions());
        furniture.setCategory(request.category());
        furniture.setStockQuantity(request.stockQuantity() != null ? request.stockQuantity() : 0);
        furniture.setImageUrl(request.imageUrl());

        furniture = furnitureRepository.save(furniture);
        return toResponse(furniture);
    }

    @Override
    public FurnitureResponse findById(Long id) {
        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mueble", "id", id));
        return toResponse(furniture);
    }

    @Override
    public PagedResponse<FurnitureResponse> findAll(int page, int size, String sort, String direction, String search, String category) {
        Sort.Direction dir = direction.equalsIgnoreCase("desc") ? Sort.Direction.DESC : Sort.Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));

        Page<Furniture> furniturePage;
        if (category != null && !category.isEmpty()) {
            furniturePage = furnitureRepository.findByCategoryIgnoreCase(category, pageable);
        } else if (search != null && !search.isEmpty()) {
            furniturePage = furnitureRepository.findByNameContainingIgnoreCase(search, pageable);
        } else {
            furniturePage = furnitureRepository.findAll(pageable);
        }

        List<FurnitureResponse> content = furniturePage.getContent().stream()
                .map(this::toResponse)
                .toList();

        return new PagedResponse<>(
                content,
                furniturePage.getNumber(),
                furniturePage.getSize(),
                furniturePage.getTotalElements(),
                furniturePage.getTotalPages(),
                furniturePage.isLast()
        );
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Furniture furniture = furnitureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Mueble", "id", id));
        furniture.setActive(false);
        furnitureRepository.save(furniture);
    }

    private FurnitureResponse toResponse(Furniture furniture) {
        return new FurnitureResponse(
                furniture.getId(),
                furniture.getName(),
                furniture.getDescription(),
                furniture.getPrice(),
                furniture.getWoodType(),
                furniture.getDimensions(),
                furniture.getCategory(),
                furniture.getStockQuantity(),
                furniture.getActive(),
                furniture.getImageUrl(),
                furniture.getCreatedAt(),
                furniture.getUpdatedAt()
        );
    }

}
