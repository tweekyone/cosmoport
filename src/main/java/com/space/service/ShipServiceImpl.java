package com.space.service;

import com.space.exceptions.ShipBadRequestException;
import com.space.exceptions.ShipNotFoundException;
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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

@Service
public class ShipServiceImpl implements ShipService{
    @Autowired
    private ShipRepository shipRepository;

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Ship create(Ship ship) {
        if(ship.getName() == null || ship.getPlanet() == null || ship.getName().isEmpty() || ship.getPlanet().isEmpty()){
            return null;
        } else if (ship.getName().length() > 50 || ship.getPlanet().length() > 50){
            return null;
        }
        if(!isValidSpeed(ship.getSpeed()) || !isValidCrew(ship.getCrewSize()) || !isValidDate(ship.getProdDate().getTime())){
            return null;
        }

        if(ship.isUsed() == null){
            ship.setUsed(false);
        }

        ship.setRating(ratingCounter(ship.isUsed(), ship.getSpeed(), ship.getProdDate()));

        return shipRepository.saveAndFlush(ship);
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
            predicates.add(cb.equal(root.get("shipType"), ship.getShipType().get()));
        }

        if(ship.getAfter().isPresent() && isValidDate(ship.getAfter().get()) && ship.getBefore().isPresent()
            && isValidDate(ship.getBefore().get())){
            predicates.add(cb.between(root.get("prodDate"), new Date(ship.getAfter().get()), new Date(ship.getBefore().get())));
        } else if (ship.getAfter().isPresent() && isValidDate(ship.getAfter().get())){
            predicates.add(cb.greaterThanOrEqualTo(root.get("prodDate"), new Date(ship.getAfter().get())));
        } else if (ship.getBefore().isPresent() && isValidDate(ship.getBefore().get())){
            predicates.add(cb.lessThanOrEqualTo(root.get("prodDate"), new Date(ship.getBefore().get())));
        }

        if(ship.getIsUsed().isPresent()){
            predicates.add(cb.equal(root.get("isUsed"), ship.getIsUsed().get()));
        }

        if(ship.getMinSpeed().isPresent() && isValidSpeed(ship.getMinSpeed().get()) && ship.getMaxSpeed().isPresent()
                && isValidSpeed(ship.getMaxSpeed().get())){
            predicates.add(cb.between(root.get("speed"), ship.getMinSpeed().get(), ship.getMaxSpeed().get()));
        } else if (ship.getMinSpeed().isPresent() && isValidSpeed(ship.getMinSpeed().get())){
            predicates.add(cb.greaterThanOrEqualTo(root.get("speed"), ship.getMinSpeed().get()));
        } else if (ship.getMaxSpeed().isPresent() && isValidSpeed(ship.getMaxSpeed().get())){
            predicates.add(cb.lessThanOrEqualTo(root.get("speed"), ship.getMaxSpeed().get()));
        }

        if(ship.getMinCrewSize().isPresent() && isValidCrew(ship.getMinCrewSize().get()) && ship.getMaxCrewSize().isPresent()
                && isValidCrew(ship.getMaxCrewSize().get())){
            predicates.add(cb.between(root.get("crewSize"), ship.getMinCrewSize().get(), ship.getMaxCrewSize().get()));
        } else if (ship.getMinCrewSize().isPresent() && isValidCrew(ship.getMinCrewSize().get())){
            predicates.add(cb.greaterThanOrEqualTo(root.get("crewSize"), ship.getMinCrewSize().get()));
        } else if (ship.getMaxCrewSize().isPresent() && isValidCrew(ship.getMaxCrewSize().get())){
            predicates.add(cb.lessThanOrEqualTo(root.get("crewSize"), ship.getMaxCrewSize().get()));
        }

        if(ship.getMinRating().isPresent() && ship.getMinRating().get() != null && ship.getMaxRating().isPresent()
                && ship.getMaxRating().get() != null){
            predicates.add(cb.between(root.get("rating"), ship.getMinRating().get(), ship.getMaxRating().get()));
        } else if (ship.getMinRating().isPresent() && ship.getMinRating().get() != null){
            predicates.add(cb.greaterThanOrEqualTo(root.get("rating"), ship.getMinRating().get()));
        } else if (ship.getMaxRating().isPresent() && ship.getMaxRating().get() != null){
            predicates.add(cb.lessThanOrEqualTo(root.get("rating"), ship.getMaxRating().get()));
        }

        if(ship.getOrder().isPresent() && !ship.getOrder().get().getFieldName().isEmpty()){
            cq.orderBy(cb.asc(root.get(ship.getOrder().get().getFieldName())));
        }

        cq.where(predicates.toArray(new Predicate[0]));

        if(ship.getPageSize().isPresent() && ship.getPageNumber().isPresent() && ship.getPageNumber().get() == 0){
            return entityManager.createQuery(cq).setFirstResult(ship.getPageNumber().get()).
                    setMaxResults(ship.getPageSize().get()).getResultList();
        }else if(ship.getPageSize().isPresent() && ship.getPageNumber().isPresent()){
            return entityManager.createQuery(cq).setFirstResult(ship.getPageNumber().get() *
                    ship.getPageSize().get()).setMaxResults(ship.getPageSize().get()).getResultList();
        }else return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public Ship get(Long id) {
        return shipRepository.findById(id).orElseThrow(ShipNotFoundException::new);
    }

    @Override
    public Ship update(Ship shipById, Ship newShip) {
        if(newShip.getName() != null && !newShip.getName().equals("")){
            shipById.setName(newShip.getName());
        }
        if(newShip.getPlanet() != null && !newShip.getPlanet().equals("")){
            shipById.setPlanet(newShip.getPlanet());
        }
        if(newShip.getShipType() != null && !newShip.getShipType().equals("")){
            shipById.setShipType(newShip.getShipType());
        }
        if(newShip.getProdDate() != null && isValidDate(newShip.getProdDate().getTime())){
            shipById.setProdDate(newShip.getProdDate());
        }
        if(newShip.isUsed() != null){
            shipById.setUsed(newShip.isUsed());
        }
        if(newShip.getSpeed() != null && isValidSpeed(newShip.getSpeed())){
            shipById.setSpeed(newShip.getSpeed());
        }
        if(newShip.getCrewSize() != null && isValidCrew(newShip.getCrewSize())){
            shipById.setCrewSize(newShip.getCrewSize());
        }

        Double rating = ratingCounter(shipById.isUsed(), shipById.getSpeed(), shipById.getProdDate());

        shipById.setRating(rating);

        return shipRepository.saveAndFlush(shipById);
    }

    @Override
    public boolean delete(Long id) {
        if(id > 0 && id <= Long.MAX_VALUE){
            shipRepository.findById(id).orElseThrow(ShipNotFoundException::new);
            shipRepository.deleteById(id);
            return true;
        }else{
            throw new ShipBadRequestException();
        }
    }

    private boolean isValidDate(Long date){
        if(date == null || date > Long.MAX_VALUE || date < 0){
            return false;
        }
        return new GregorianCalendar(2800, 1, 1).getTimeInMillis() <= date
                                && new GregorianCalendar(3019, 1, 1).getTimeInMillis() >= date;
    }

    private boolean isValidSpeed(Double speed){
        if(speed == null || speed > Double.MAX_VALUE){
            return false;
        }
        double scale = Math.pow(10, 3);
        double actualSpeed = Math.ceil(speed * scale) / scale;
        return actualSpeed >= 0.01d && actualSpeed <= 0.99d;
    }

    private boolean isValidCrew(Integer crew){
        return crew != null && (crew >= 1 && crew <= 9999);
    }

    private Double ratingCounter(Boolean isUsed, Double speed, Date prodDate){
        double k = isUsed ? 0.5d : 1d;
        double rating = (80*speed*k) / (new GregorianCalendar(3019, 1, 1).getTime().getYear()
                - prodDate.getYear() + 1);
        double scale = Math.pow(10, 3);
        return Math.ceil(rating * scale) / scale;
    }
}
