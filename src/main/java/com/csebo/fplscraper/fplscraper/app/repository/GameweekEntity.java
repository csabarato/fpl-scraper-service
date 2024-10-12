package com.csebo.fplscraper.fplscraper.app.repository;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "gameweek")
public class GameweekEntity {

    @Id
    @Column(name = "id")
    private int gameweekNum;

    @Column(name = "deadline_time")
    private LocalDateTime deadlineTime;
}
