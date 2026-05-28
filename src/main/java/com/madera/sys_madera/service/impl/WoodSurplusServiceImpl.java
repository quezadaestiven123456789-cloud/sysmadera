package com.madera.sys_madera.service.impl;

import com.madera.sys_madera.model.WoodSurplus;
import com.madera.sys_madera.repository.WoodSurplusRepository;
import com.madera.sys_madera.service.WoodSurplusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class WoodSurplusServiceImpl implements WoodSurplusService {

    private final WoodSurplusRepository woodSurplusRepository;

    @Override
    @Transactional
    public WoodSurplus create(WoodSurplus surplus) {
        return woodSurplusRepository.save(surplus);
    }

    @Override
    public List<WoodSurplus> findAllAvailable() {
        return woodSurplusRepository.findByAvailableTrue();
    }

    @Override
    public List<WoodSurplus> findByWoodType(String woodType) {
        return woodSurplusRepository.findByWoodTypeContainingIgnoreCase(woodType);
    }

}
