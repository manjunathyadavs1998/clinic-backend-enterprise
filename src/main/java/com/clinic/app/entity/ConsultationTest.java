package com.clinic.app.entity;

import com.clinic.app.enums.ConsultationTestStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "consultation_tests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "consultation_id", nullable = false)
    private Consultation consultation;

    @ManyToOne(optional = false)
    @JoinColumn(name = "lab_test_id", nullable = false)
    private LabTest labTest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ConsultationTestStatus status;

    @Column(length = 500)
    private String remarks;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = ConsultationTestStatus.ORDERED;
    }
}
