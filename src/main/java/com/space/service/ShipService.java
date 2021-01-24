package com.space.service;

import com.space.model.Ship;

import java.util.List;

public interface ShipService {
    void create(Ship ship);

    List<Ship> readAll(ShipOptional ship);

    Ship get(Long id);

    Ship update(Ship ship);

    boolean delete(Long id);
}
