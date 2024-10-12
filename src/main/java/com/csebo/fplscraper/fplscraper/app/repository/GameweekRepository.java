package com.csebo.fplscraper.fplscraper.app.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

@Repository
public interface GameweekRepository extends CrudRepository<GameweekEntity, Long> {

    GameweekEntity findFirstByDeadlineTimeBeforeOrderByDeadlineTimeDesc(LocalDateTime currentDateTime);
}
