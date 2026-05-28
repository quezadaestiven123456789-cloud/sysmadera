package com.madera.sys_madera.service;

import com.madera.sys_madera.model.WoodSurplus;
import java.util.List;

public interface WoodSurplusService {

    WoodSurplus create(WoodSurplus surplus);

    List<WoodSurplus> findAllAvailable();

    List<WoodSurplus> findByWoodType(String woodType);

}
