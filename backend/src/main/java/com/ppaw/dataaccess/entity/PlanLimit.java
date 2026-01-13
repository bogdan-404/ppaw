package com.ppaw.dataaccess.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "plan_limits", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"plan_id", "key"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanLimit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Column(nullable = false, length = 255)
    private String key;

    @Column(nullable = false, length = 255)
    private String value;
}
