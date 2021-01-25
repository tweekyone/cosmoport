package com.space.controller;

import com.space.model.Ship;
import com.space.model.ShipType;
import com.space.service.ShipOptional;
import com.space.service.ShipService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
public class Controller {
    @Autowired
    private ShipService shipService;

    @GetMapping("/rest/ships")
    public ResponseEntity<List<Ship>> getShips(@RequestParam(name = "name") Optional<String> name,
                                   @RequestParam(name = "planet") Optional<String> planet,
                                   @RequestParam(name = "shipType") Optional<ShipType> shipType,
                                   @RequestParam(name = "after") Optional<Long> after,
                                   @RequestParam(name = "before") Optional<Long> before,
                                   @RequestParam(name = "isUsed") Optional<Boolean> isUsed,
                                   @RequestParam(name = "minSpeed") Optional<Double> minSpeed,
                                   @RequestParam(name = "maxSpeed") Optional<Double> maxSpeed,
                                   @RequestParam(name = "minCrewSize") Optional<Integer> minCrewSize,
                                   @RequestParam(name = "maxCrewSize") Optional<Integer> maxCrewSize,
                                   @RequestParam(name = "minRating") Optional<Double> minRating,
                                   @RequestParam(name = "maxRating") Optional<Double> maxRating,
                                   @RequestParam(name = "order", defaultValue = "ID") Optional<ShipOrder> order,
                                   @RequestParam(name = "pageNumber", defaultValue = "0") Optional<Integer> pageNumber,
                                   @RequestParam(name = "pageSize", defaultValue = "3") Optional<Integer> pageSize){

        ShipOptional shipOptional = new ShipOptional(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating, order, pageNumber, pageSize);

        List<Ship> response = shipService.readAll(shipOptional);

        if(response == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/rest/ships/count")
    public ResponseEntity<Integer> getShipsCount( @RequestParam(name = "name") Optional<String> name,
                                  @RequestParam(name = "planet") Optional<String> planet,
                                  @RequestParam(name = "shipType") Optional<ShipType> shipType,
                                  @RequestParam(name = "after") Optional<Long> after,
                                  @RequestParam(name = "before") Optional<Long> before,
                                  @RequestParam(name = "isUsed") Optional<Boolean> isUsed,
                                  @RequestParam(name = "minSpeed") Optional<Double> minSpeed,
                                  @RequestParam(name = "maxSpeed") Optional<Double> maxSpeed,
                                  @RequestParam(name = "minCrewSize") Optional<Integer> minCrewSize,
                                  @RequestParam(name = "maxCrewSize") Optional<Integer> maxCrewSize,
                                  @RequestParam(name = "minRating") Optional<Double> minRating,
                                  @RequestParam(name = "maxRating") Optional<Double> maxRating,
                                  @RequestParam(name = "order", defaultValue = "") Optional<ShipOrder> shipOrder){

        Integer response = shipService.readAll(new ShipOptional(name, planet, shipType, after, before, isUsed, minSpeed, maxSpeed,
                minCrewSize, maxCrewSize, minRating, maxRating, shipOrder, Optional.of(Integer.MAX_VALUE),
                Optional.of(Integer.MAX_VALUE))).size() + 1;

        if (response == null){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/rest/ships")
    public ResponseEntity<Ship> createShip(){

    }
}