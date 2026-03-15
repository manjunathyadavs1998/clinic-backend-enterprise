package com.clinic.app.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "lab_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LabTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false, unique = true, length = 100)
    private String name;

    @Column(nullable=false, precision = 10, scale = 2)
    private BigDecimal cost;

    @Column(nullable = false)
    private Boolean active = true;
}
