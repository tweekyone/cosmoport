package com.space.service;

import com.space.model.Ship;
import com.space.repository.ShipRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class ShipServiceImpl implements ShipService{
    @Autowired
    private ShipRepository shipRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public void create(Ship ship) {
        shipRepository.save(ship);
    }

    @Override
    public List<Ship> readAll(ShipOptional ship) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Ship> cq = cb.createQuery(Ship.class);

        Root<Ship> root = cq.from(Ship.class);
        List<Predicate> predicates = new ArrayList<>();

        if(ship.getName().isPresent() && !ship.getName().get().isEmpty() && ship.getName().get().length() <= 50){
            predicates.add(cb.like(root.get("name"), "%" + ship.getName().get() + "%"));
        }

        if(ship.getPlanet().isPresent() && !ship.getPlanet().get().isEmpty() && ship.getPlanet().get().length() <= 50){
            predicates.add(cb.like(root.get("planet"), "%" + ship.getPlanet().get() + "%"));
        }

        if(ship.getShipType().isPresent()){
            predicates.add(cb.equal(root.get("shipType"), ship.getShipType().get().name()));
        }

        if(ship.getAfter().isPresent() && isValidDate(ship.getAfter().get()) && ship.getBefore().isPresent()
            && isValidDate(ship.getBefore().get())){
            predicates.add(cb.between(root.get("prodDate"), ship.getAfter().get(), ship.getBefore().get()));
        } else if (ship.getAfter().isPresent() && isValidDate(ship.getAfter().get())){
            predicates.add(cb.greaterThanOrEqualTo(root.get("prodDate"), ship.getAfter().get()));
        } else if (ship.getBefore().isPresent() && isValidDate(ship.getBefore().get())){
            predicates.add(cb.lessThanOrEqualTo(root.get("prodDate"), ship.getBefore().get()));
        }

        if(ship.getIsUsed().isPresent()){
            predicates.add(cb.equal(root.get("isUsed"), ship.getIsUsed().get()));
        }

        if(ship.getMinSpeed().isPresent() && isValidSpeed(ship.getMinSpeed().get()) && ship.getMaxSpeed().isPresent()
                && isValidSpeed(ship.getMaxRating().get())){
            predicates.add(cb.between(root.get("speed"), ship.getMinSpeed().get(), ship.getMaxSpeed().get()));
        } else if (ship.getMinSpeed().isPresent() && isValidSpeed(ship.getMinSpeed().get())){
            predicates.add(cb.greaterThanOrEqualTo(root.get("speed"), ship.getMinSpeed().get()));
        } else if (ship.getMaxSpeed().isPresent() && isValidSpeed(ship.getMaxSpeed().get())){
            predicates.add(cb.lessThanOrEqualTo(root.get("speed"), ship.getMaxSpeed().get()));
        }



        if(ship.getOrder().isPresent() && !ship.getOrder().get().getFieldName().isEmpty()){
            cq.orderBy(cb.asc(root.get(ship.getOrder().get().getFieldName())));
        }

        cq.where(predicates.toArray(new Predicate[0]));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public Ship get(Long id) {
        return null;
    }

    @Override
    public Ship update(Ship ship) {
        return ship;
    }

    @Override
    public boolean delete(Long id) {
        return false;
    }

    private boolean isValidDate(Long date){
        return date != null && (new GregorianCalendar(2800, 1, 1).getTimeInMillis() <= date
                                && new GregorianCalendar(3019, 1, 1).getTimeInMillis() >= date);
    }

    private boolean isValidSpeed(Double speed){
        double scale = Math.pow(10, 3);
        double actualSpeed = Math.ceil(speed * scale) / scale;
        return speed != null && (actualSpeed >= 0.01d && actualSpeed <= 0.99d);
    }
}
