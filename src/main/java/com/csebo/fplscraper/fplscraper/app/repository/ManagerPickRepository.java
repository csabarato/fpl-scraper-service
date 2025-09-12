package com.csebo.fplscraper.fplscraper.app.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ManagerPickRepository extends CrudRepository<ManagerPickEntity, Long> {

    List<ManagerPickEntity> findAllByManagerIdIsInAndGameweek(List<Integer> managerId, Integer gameweek);

}
