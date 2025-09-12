package com.csebo.fplscraper.fplscraper.app.repository;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@AllArgsConstructor
@Table(name = "manger_pick")
@NoArgsConstructor
@IdClass(ManagerPickId.class)
public class ManagerPickEntity {

    @Id
    @Column(name = "manager_id", nullable = false)
    private Integer managerId;

    @Id
    @Column(name = "gameweek", nullable = false)
    private Integer gameweek;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", referencedColumnName = "id", insertable = false, updatable = false)
    private PlayerEntity player;

    @Column(name = "multiplier", nullable = false)
    private Integer multiplier;

    @Column(name = "is_captain")
    private Boolean isCaptain;

}
