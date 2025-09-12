package com.csebo.fplscraper.fplscraper.app.repository;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ManagerPickId implements Serializable {

    private Integer managerId;
    private Integer gameweek;
    private Integer player;
}
