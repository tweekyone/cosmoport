package com.space.service;

import com.space.model.Ship;

import java.util.List;

public interface ShipService {
    Ship create(Ship ship);

    List<Ship> readAll(ShipOptional ship);

    Ship get(Long id);

    Ship update(Long id, Ship shipOptional);

    boolean delete(Long id);
}
